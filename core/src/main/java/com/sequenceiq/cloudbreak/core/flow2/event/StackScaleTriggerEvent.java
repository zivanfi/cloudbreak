package com.sequenceiq.cloudbreak.core.flow2.event;

import java.util.Map;
import java.util.Set;

import com.sequenceiq.cloudbreak.common.event.AcceptResult;
import com.sequenceiq.cloudbreak.core.flow2.dto.NetworkScaleDetails;
import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;
import com.sequenceiq.common.api.adjustment.AdjustmentTypeWithThreshold;

import reactor.rx.Promise;

public class StackScaleTriggerEvent extends StackEvent {

    private final Map<String, Set<Long>> hostGroupWithPrivateIds;

    private final Map<String, Integer> hostGroupWithAdjustment;

    private final Map<String, Set<String>> hostGroupWithHostnames;

    private final String triggeredStackVariant;

    private boolean repair;

    private NetworkScaleDetails networkScaleDetails;

    private final AdjustmentTypeWithThreshold adjustmentTypeWithThreshold;

    public StackScaleTriggerEvent(String selector, Long stackId, Map<String, Integer> hostGroupWithAdjustment, Map<String, Set<Long>> hostGroupWithPrivateIds,
            Map<String, Set<String>> hostGroupWithHostnames, AdjustmentTypeWithThreshold adjustmentTypeWithThreshold, String triggeredStackVariant) {
        this(selector, stackId, hostGroupWithAdjustment, hostGroupWithPrivateIds, hostGroupWithHostnames, NetworkScaleDetails.getEmpty(),
                adjustmentTypeWithThreshold, triggeredStackVariant);
    }

    public StackScaleTriggerEvent(String selector, Long stackId, Map<String, Integer> hostGroupWithAdjustment, Map<String, Set<Long>> hostGroupWithPrivateIds,
            Map<String, Set<String>> hostGroupWithHostnames, NetworkScaleDetails networkScaleDetails, AdjustmentTypeWithThreshold adjustmentTypeWithThreshold,
            String triggeredStackVariant) {
        super(selector, stackId);
        this.hostGroupWithAdjustment = hostGroupWithAdjustment;
        this.hostGroupWithPrivateIds = hostGroupWithPrivateIds;
        this.hostGroupWithHostnames = hostGroupWithHostnames;
        this.networkScaleDetails = networkScaleDetails;
        this.adjustmentTypeWithThreshold = adjustmentTypeWithThreshold;
        this.triggeredStackVariant = triggeredStackVariant;
    }

    public StackScaleTriggerEvent(String selector, Long stackId, Map<String, Integer> hostGroupWithAdjustment, Map<String, Set<Long>> hostGroupWithPrivateIds,
            Map<String, Set<String>> hostGroupWithHostnames, AdjustmentTypeWithThreshold adjustmentTypeWithThreshold, String triggeredStackVariant,
            Promise<AcceptResult> accepted) {
        super(selector, stackId, accepted);
        this.hostGroupWithAdjustment = hostGroupWithAdjustment;
        this.hostGroupWithPrivateIds = hostGroupWithPrivateIds;
        this.hostGroupWithHostnames = hostGroupWithHostnames;
        this.networkScaleDetails = new NetworkScaleDetails();
        this.adjustmentTypeWithThreshold = adjustmentTypeWithThreshold;
        this.triggeredStackVariant = triggeredStackVariant;
    }

    public StackScaleTriggerEvent setRepair() {
        repair = true;
        return this;
    }

    public Map<String, Set<Long>> getHostGroupWithPrivateIds() {
        return hostGroupWithPrivateIds;
    }

    public Map<String, Integer> getHostGroupWithAdjustment() {
        return hostGroupWithAdjustment;
    }

    public Map<String, Set<String>> getHostGroupWithHostnames() {
        return hostGroupWithHostnames;
    }

    public boolean isRepair() {
        return repair;
    }

    public AdjustmentTypeWithThreshold getAdjustmentTypeWithThreshold() {
        return adjustmentTypeWithThreshold;
    }

    public NetworkScaleDetails getNetworkScaleDetails() {
        return networkScaleDetails;
    }

    public String getTriggeredStackVariant() {
        return triggeredStackVariant;
    }
}
