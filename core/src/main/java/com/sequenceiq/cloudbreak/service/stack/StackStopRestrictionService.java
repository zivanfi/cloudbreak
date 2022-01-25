package com.sequenceiq.cloudbreak.service.stack;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.sequenceiq.cloudbreak.cloud.VersionComparator;
import com.sequenceiq.cloudbreak.cloud.model.CloudbreakDetails;
import com.sequenceiq.cloudbreak.cluster.service.ClusterComponentConfigProvider;
import com.sequenceiq.cloudbreak.cmtemplate.CmTemplateProcessorFactory;
import com.sequenceiq.cloudbreak.common.type.TemporaryStorage;
import com.sequenceiq.cloudbreak.domain.StopRestrictionReason;
import com.sequenceiq.cloudbreak.domain.VolumeUsageType;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceGroup;
import com.sequenceiq.cloudbreak.service.ComponentConfigProviderService;
import com.sequenceiq.cloudbreak.service.stack.StackStopRestrictionConfiguration.ServiceRoleGroup;
import com.sequenceiq.cloudbreak.template.model.ServiceComponent;
import com.sequenceiq.common.api.type.InstanceGroupType;
import com.sequenceiq.common.model.AwsDiskType;

@Service
public class StackStopRestrictionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StackStopRestrictionService.class);

    private final StackStopRestrictionConfiguration config;

    @Inject
    private ComponentConfigProviderService componentConfigProviderService;

    @Inject
    private CmTemplateProcessorFactory cmTemplateProcessorFactory;

    @Inject
    private ClusterComponentConfigProvider clusterComponentProvider;

    public StackStopRestrictionService(StackStopRestrictionConfiguration config) {
        this.config = config;
    }

    public StopRestrictionReason isInfrastructureStoppable(Stack stack) {
        Map<String, Set<ServiceComponent>> serviceCompontnsByInstanceGroup = null;
        StopRestrictionReason reason = StopRestrictionReason.NONE;
        if (config.getRestrictedCloudPlatform().equals(stack.getCloudPlatform())) {
            for (InstanceGroup instanceGroup : stack.getInstanceGroups()) {
                if (isInstanceGroupEphemeralVolumesOnly(instanceGroup)) {
                    if (!isCbVersionBeforeStopSupport(stack, config.getEphemeralOnlyMinVersion())
                            || !isSaltComponentCbVersionBeforeStopSupport(stack.getCluster().getId())) {
                        if (serviceCompontnsByInstanceGroup == null) {
                            serviceCompontnsByInstanceGroup = cmTemplateProcessorFactory
                                    .get(stack.getCluster().getBlueprint().getBlueprintText()).getServiceComponentsByHostGroup();
                        }
                        Set<ServiceComponent> serviceComponents = serviceCompontnsByInstanceGroup.get(instanceGroup.getGroupName());
                        if (!isEphemeralInstanceGroupStoppable(serviceComponents)) {
                            reason = StopRestrictionReason.EPHEMERAL_VOLUMES;
                            LOGGER.info("Infrastructure cannot be stopped. Instances in group [{}] have ephemeral storage only " +
                                    "and one or more services on the host group do not support stopping on ephemeral volumes.", instanceGroup.getGroupName());
                            return reason;
                        }
                    } else {
                        reason = StopRestrictionReason.EPHEMERAL_VOLUMES;
                        LOGGER.info("Infrastructure cannot be stopped. Instances in group [{}] have ephemeral storage only. " +
                                "Stopping clusters with ephemeral volume based host groups are only available in clusters " +
                                "created at least with Cloudbreak version [{}] or upgraded.",
                                instanceGroup.getGroupName(), config.getEphemeralOnlyMinVersion());
                        return reason;
                    }
                } else if (isCbVersionBeforeStopSupport(stack, config.getEphemeralCachingMinVersion())) {
                    TemporaryStorage temporaryStorage = instanceGroup.getTemplate().getTemporaryStorage();
                    if (TemporaryStorage.EPHEMERAL_VOLUMES.equals(temporaryStorage)) {
                        reason = StopRestrictionReason.EPHEMERAL_VOLUME_CACHING;
                        LOGGER.info("Infrastructure cannot be stopped. Group [{}] has ephemeral volume caching enabled. " +
                                "Stopping clusters with ephemeral volume caching enabled are only available in clusters " +
                                "created at least with Cloudbreak version [{}].", instanceGroup.getGroupName(), config.getEphemeralCachingMinVersion());
                        return reason;
                    }
                    if (hasInstanceGroupAwsEphemeralStorage(instanceGroup)) {
                        reason = StopRestrictionReason.EPHEMERAL_VOLUMES;
                        LOGGER.info("Infrastructure cannot be stopped. Instances in group [{}] have ephemeral storage." +
                                "Stopping clusters with ephemeral storage instances are only available in clusters " +
                                "created at least with Cloudbreak version [{}].", instanceGroup.getGroupName(), config.getEphemeralCachingMinVersion());
                        return reason;
                    }
                }
            }
        }
        return reason;
    }

    @VisibleForTesting
    boolean isEphemeralInstanceGroupStoppable(Set<ServiceComponent> serviceComponents) {
        for (ServiceRoleGroup serviceRoleGroup: config.getPermittedServiceRoleGroups()) {
            if (areServiceComponentsPermitted(serviceRoleGroup, serviceComponents) && areRequiredServiceRolesPresent(serviceRoleGroup, serviceComponents)) {
                return true;
            }
        }
        return false;
    }

    private boolean areServiceComponentsPermitted(ServiceRoleGroup serviceRoleGroup, Set<ServiceComponent> serviceComponents) {
        Set<ServiceComponent> permittedServiceComponents = serviceRolesToServiceComponents(serviceRoleGroup.getServiceRoles());
        Set<ServiceComponent> permittedComponents = serviceRolesToServiceComponents(serviceRoleGroup.getRoles());
        for (ServiceComponent serviceComponent: serviceComponents) {
            if (!permittedServiceComponents.contains(serviceComponent) &&
                !permittedComponents.contains(ServiceComponent.of(null, serviceComponent.getComponent()))) {
                return false;
            }
        }
        return true;
    }

    private boolean areRequiredServiceRolesPresent(ServiceRoleGroup serviceRoleGroup, Set<ServiceComponent> serviceComponents) {
        boolean requiredServiceRolesPresent = serviceComponents
                .containsAll(serviceRolesToServiceComponents(serviceRoleGroup.getRequiredServiceRoles()));
        boolean requiredRolesPresent = serviceComponents.stream()
                .map(x -> ServiceComponent.of(null, x.getComponent()))
                .collect(Collectors.toSet())
                .containsAll(serviceRolesToServiceComponents(serviceRoleGroup.getRequiredRoles()));
        return requiredServiceRolesPresent && requiredRolesPresent;
    }

    private Set<ServiceComponent> serviceRolesToServiceComponents (Set<ServiceRoleGroup.ServiceRole> serviceRoles) {
        return serviceRoles.stream()
                .map(x -> ServiceComponent.of(x.getService(), x.getRole()))
                .collect(Collectors.toSet());
    }

    private boolean hasInstanceGroupAwsEphemeralStorage(InstanceGroup instanceGroup) {
        return instanceGroup.getTemplate().getVolumeTemplates().stream().anyMatch(volume -> AwsDiskType.Ephemeral.value().equals(volume.getVolumeType()));
    }

    @VisibleForTesting
    boolean isInstanceGroupEphemeralVolumesOnly (InstanceGroup ig) {
        long ephemeralVolumeCount = ig.getTemplate().getVolumeTemplates().stream()
                .filter(volumeTemplate -> AwsDiskType.Ephemeral.value().equalsIgnoreCase(volumeTemplate.getVolumeType())).count();
        long embeddedDbVolumeCount = ig.getTemplate().getVolumeTemplates().stream()
                .filter(volumeTemplate -> VolumeUsageType.DATABASE.equals(volumeTemplate.getUsageType())).count();
        return ephemeralVolumeCount == ig.getTemplate().getVolumeTemplates().size() ||
                (InstanceGroupType.isGateway(ig.getInstanceGroupType()) &&
                        ephemeralVolumeCount > 0 &&
                        ephemeralVolumeCount + embeddedDbVolumeCount == ig.getTemplate().getVolumeTemplates().size());
    }

    private boolean isCbVersionBeforeStopSupport(Stack stack, String minVersion) {
        CloudbreakDetails cloudbreakDetails = componentConfigProviderService.getCloudbreakDetails(stack.getId());
        return isCbVersionBeforeMinVersion(cloudbreakDetails.getVersion(), minVersion);
    }

    private boolean isSaltComponentCbVersionBeforeStopSupport(Long clusterId) {
        String saltCbVersion = clusterComponentProvider.getSaltStateComponentCbVersion(clusterId);
        return saltCbVersion == null || isCbVersionBeforeMinVersion(saltCbVersion, config.getEphemeralOnlyMinVersion());
    }

    private boolean isCbVersionBeforeMinVersion(String cbVersion, String minVersion) {
        VersionComparator versionComparator = new VersionComparator();
        String version = StringUtils.substringBefore(cbVersion, "-");
        int compare = versionComparator.compare(() -> version, () -> minVersion);
        return compare < 0;
    }
}
