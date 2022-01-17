package com.sequenceiq.cloudbreak.core.flow2.event;

import java.util.Map;
import java.util.Set;

import com.sequenceiq.cloudbreak.common.event.AcceptResult;
import com.sequenceiq.cloudbreak.common.type.ClusterManagerType;
import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;

import reactor.rx.Promise;

public class ClusterScaleTriggerEvent extends StackEvent {

    private final Map<String, Set<Long>> hostGroupWithPrivateIds;

    private final Map<String, Integer> hostGroupWithAdjustment;

    private final Map<String, Set<String>> hostGroupWithHostNames;

    private final boolean singlePrimaryGateway;

    private final boolean kerberosSecured;

    private final boolean singleNodeCluster;

    private final boolean restartServices;

    private String primaryGatewayHostName;

    private final ClusterManagerType clusterManagerType;

    public ClusterScaleTriggerEvent(String selector, Long stackId, Map<String, Integer> hostGroupWithAdjustment, Map<String, Set<Long>> hostGroupWithPrivateIds,
            Map<String, Set<String>> hostGroupWithHostNames, boolean singlePrimaryGateway, boolean kerberosSecured, boolean singleNodeCluster,
            boolean restartServices, ClusterManagerType clusterManagerType) {
        super(selector, stackId);
        this.hostGroupWithAdjustment = hostGroupWithAdjustment;
        this.hostGroupWithPrivateIds = hostGroupWithPrivateIds;
        this.hostGroupWithHostNames = hostGroupWithHostNames;
        this.singlePrimaryGateway = singlePrimaryGateway;
        this.kerberosSecured = kerberosSecured;
        this.singleNodeCluster = singleNodeCluster;
        this.restartServices = restartServices;
        this.clusterManagerType = clusterManagerType;
    }

    public ClusterScaleTriggerEvent(String selector, Long stackId, Map<String, Integer> hostGroupWithAdjustment,
            Map<String, Set<Long>> hostGroupWithPrivateIds, Map<String, Set<String>> hostGroupWithHostNames) {
        this(selector, stackId, hostGroupWithAdjustment, hostGroupWithPrivateIds, hostGroupWithHostNames, false, false, false, false,
                ClusterManagerType.CLOUDERA_MANAGER);
    }

    public ClusterScaleTriggerEvent(String selector, Long stackId, Map<String, Integer> hostGroupWithAdjustment, Map<String, Set<Long>> hostGroupWithPrivateIds,
            Map<String, Set<String>> hostGroupWithHostNames, Promise<AcceptResult> accepted) {
        super(selector, stackId, accepted);
        this.hostGroupWithAdjustment = hostGroupWithAdjustment;
        this.hostGroupWithPrivateIds = hostGroupWithPrivateIds;
        this.hostGroupWithHostNames = hostGroupWithHostNames;
        singlePrimaryGateway = false;
        kerberosSecured = false;
        singleNodeCluster = false;
        restartServices = false;
        clusterManagerType = ClusterManagerType.CLOUDERA_MANAGER;
    }

    public Map<String, Set<Long>> getHostGroupWithPrivateIds() {
        return hostGroupWithPrivateIds;
    }

    public Map<String, Integer> getHostGroupWithAdjustment() {
        return hostGroupWithAdjustment;
    }

    public Map<String, Set<String>> getHostGroupWithHostNames() {
        return hostGroupWithHostNames;
    }

    public Set<String> getHostGroups() {
        return hostGroupWithAdjustment.keySet();
    }

    public boolean isKerberosSecured() {
        return kerberosSecured;
    }

    public boolean isSinglePrimaryGateway() {
        return singlePrimaryGateway;
    }

    public boolean isSingleNodeCluster() {
        return singleNodeCluster;
    }

    public boolean isRestartServices() {
        return restartServices;
    }

    public ClusterManagerType getClusterManagerType() {
        return clusterManagerType;
    }
}
