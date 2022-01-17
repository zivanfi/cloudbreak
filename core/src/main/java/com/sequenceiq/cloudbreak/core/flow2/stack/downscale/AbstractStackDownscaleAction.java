package com.sequenceiq.cloudbreak.core.flow2.stack.downscale;

import static com.sequenceiq.cloudbreak.cloud.model.AvailabilityZone.availabilityZone;
import static com.sequenceiq.cloudbreak.cloud.model.Location.location;
import static com.sequenceiq.cloudbreak.cloud.model.Region.region;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;

import com.sequenceiq.cloudbreak.cloud.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.Location;
import com.sequenceiq.cloudbreak.common.event.Payload;
import com.sequenceiq.cloudbreak.converter.spi.StackToCloudStackConverter;
import com.sequenceiq.cloudbreak.core.flow2.AbstractStackAction;
import com.sequenceiq.cloudbreak.core.flow2.event.StackDownscaleTriggerEvent;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.logger.MDCBuilder;
import com.sequenceiq.cloudbreak.reactor.api.event.StackFailureEvent;
import com.sequenceiq.cloudbreak.service.resource.ResourceService;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.cloudbreak.util.StackUtil;
import com.sequenceiq.common.api.adjustment.AdjustmentTypeWithThreshold;
import com.sequenceiq.common.api.type.AdjustmentType;
import com.sequenceiq.flow.core.FlowParameters;

public abstract class AbstractStackDownscaleAction<P extends Payload>
        extends AbstractStackAction<StackDownscaleState, StackDownscaleEvent, StackScalingFlowContext, P> {

    private static final String REPAIR = "REPAIR";

    private static final String HOST_GROUP_WITH_ADJUSTMENT = "HOST_GROUP_WITH_ADJUSTMENT";

    private static final String HOST_GROUP_WITH_PRIVATE_IDS = "HOST_GROUP_WITH_PRIVATE_IDS";

    private static final String HOST_GROUP_WITH_HOSTNAMES = "HOST_GROUP_WITH_HOSTNAMES";

    @Inject
    private StackService stackService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private StackToCloudStackConverter cloudStackConverter;

    @Inject
    private StackUtil stackUtil;

    protected AbstractStackDownscaleAction(Class<P> payloadClass) {
        super(payloadClass);
    }

    @Override
    protected StackScalingFlowContext createFlowContext(FlowParameters flowParameters, StateContext<StackDownscaleState, StackDownscaleEvent> stateContext,
            P payload) {
        Map<Object, Object> variables = stateContext.getExtendedState().getVariables();
        Stack stack = stackService.getByIdWithListsInTransaction(payload.getResourceId());
        stack.setResources(new HashSet<>(resourceService.getAllByStackId(payload.getResourceId())));
        MDCBuilder.buildMdcContext(stack);
        Location location = location(region(stack.getRegion()), availabilityZone(stack.getAvailabilityZone()));
        CloudContext cloudContext = CloudContext.Builder.builder()
                .withId(stack.getId())
                .withName(stack.getName())
                .withCrn(stack.getResourceCrn())
                .withPlatform(stack.getCloudPlatform())
                .withVariant(stack.getPlatformVariant())
                .withLocation(location)
                .withWorkspaceId(stack.getWorkspace().getId())
                .withAccountId(stack.getTenant().getId())
                .build();
        CloudCredential cloudCredential = stackUtil.getCloudCredential(stack);
        CloudStack cloudStack = cloudStackConverter.convert(stack);
        if (payload instanceof StackDownscaleTriggerEvent) {
            StackDownscaleTriggerEvent stackDownscaleTriggerEvent = (StackDownscaleTriggerEvent) payload;
            boolean repair = stackDownscaleTriggerEvent.isRepair();
            Map<String, Integer> hostGroupWithAdjustment = stackDownscaleTriggerEvent.getHostGroupWithAdjustment();
            Map<String, Set<Long>> hostGroupWithPrivateIds = stackDownscaleTriggerEvent.getHostGroupWithPrivateIds();
            Map<String, Set<String>> hostgroupWithHostnames = stackDownscaleTriggerEvent.getHostGroupWithHostnames();
            variables.put(REPAIR, repair);
            variables.put(HOST_GROUP_WITH_ADJUSTMENT, hostGroupWithAdjustment);
            variables.put(HOST_GROUP_WITH_PRIVATE_IDS, hostGroupWithPrivateIds);
            variables.put(HOST_GROUP_WITH_HOSTNAMES, hostgroupWithHostnames);
            return new StackScalingFlowContext(flowParameters, stack, cloudContext, cloudCredential, cloudStack,
                    hostGroupWithAdjustment, hostGroupWithPrivateIds, hostgroupWithHostnames, repair,
                    new AdjustmentTypeWithThreshold(AdjustmentType.BEST_EFFORT, null));
        } else {
            Map<String, Integer> hostGroupWithAdjustment = getHostGroupWithAdjustment(variables);
            Map<String, Set<Long>> hostGroupWithPrivateIds = getHostGroupWithPrivateIds(variables);
            Map<String, Set<String>> hostgroupWithHostnames = getHostGroupWithHostnames(variables);
            return new StackScalingFlowContext(flowParameters, stack, cloudContext, cloudCredential, cloudStack,
                    hostGroupWithAdjustment, hostGroupWithPrivateIds, hostgroupWithHostnames, isRepair(variables),
                    new AdjustmentTypeWithThreshold(AdjustmentType.BEST_EFFORT, null));
        }

    }
    @Override
    protected Object getFailurePayload(P payload, Optional<StackScalingFlowContext> flowContext, Exception ex) {
        return new StackFailureEvent(payload.getResourceId(), ex);
    }

    protected boolean isRepair(Map<Object, Object> variables) {
        return variables.get(REPAIR) != null && (Boolean) variables.get(REPAIR);
    }

    protected Map<String, Integer> getHostGroupWithAdjustment(Map<Object, Object> variables) {
        return (Map<String, Integer>) variables.get(HOST_GROUP_WITH_ADJUSTMENT);
    }

    protected Map<String, Set<Long>> getHostGroupWithPrivateIds(Map<Object, Object> variables) {
        return (Map<String, Set<Long>>) variables.get(HOST_GROUP_WITH_PRIVATE_IDS);
    }

    protected Map<String, Set<String>> getHostGroupWithHostnames(Map<Object, Object> variables) {
        return (Map<String, Set<String>>) variables.get(HOST_GROUP_WITH_HOSTNAMES);
    }
}
