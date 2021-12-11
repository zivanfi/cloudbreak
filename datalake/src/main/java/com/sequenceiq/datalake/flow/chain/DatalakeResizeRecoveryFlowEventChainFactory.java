package com.sequenceiq.datalake.flow.chain;

import static com.sequenceiq.datalake.flow.delete.SdxDeleteEvent.SDX_DELETE_EVENT;
import static com.sequenceiq.datalake.flow.detach.SdxDetachEvent.SDX_DETACH_RECOVERY_EVENT;
import static com.sequenceiq.datalake.flow.detach.event.DatalakeResizeRecoveryFlowChainStartEvent.SDX_RESIZE_RECOVERY_FLOW_CHAIN_START_EVENT;
import static com.sequenceiq.datalake.flow.start.SdxStartEvent.SDX_START_EVENT;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.datalake.flow.delete.event.SdxDeleteStartEvent;
import com.sequenceiq.datalake.flow.detach.event.DatalakeResizeRecoveryFlowChainStartEvent;
import com.sequenceiq.datalake.flow.detach.event.SdxStartDetachRecoveryEvent;
import com.sequenceiq.datalake.flow.start.event.SdxStartStartEvent;
import com.sequenceiq.flow.core.chain.FlowEventChainFactory;
import com.sequenceiq.flow.core.chain.config.FlowTriggerEventQueue;

@Component
public class DatalakeResizeRecoveryFlowEventChainFactory implements FlowEventChainFactory<DatalakeResizeRecoveryFlowChainStartEvent> {
    @Override
    public String initEvent() {
        return SDX_RESIZE_RECOVERY_FLOW_CHAIN_START_EVENT;
    }

    @Override
    public FlowTriggerEventQueue createFlowTriggerEventQueue(DatalakeResizeRecoveryFlowChainStartEvent event) {
        Queue<Selectable> flowChain = new ConcurrentLinkedQueue<>();

        // TODO: We could add validation here to select which flows are added to chain if desired.
        // Forced deletion of the resized datalake.
        flowChain.add(new SdxDeleteStartEvent(
                SDX_DELETE_EVENT.event(), event.getNewCluster().getId(), event.getUserId(), true
        ));

        // Reattach of the old datalake.
        flowChain.add(new SdxStartDetachRecoveryEvent(
                SDX_DETACH_RECOVERY_EVENT.event(), event.getOldCluster().getId(), event.getUserId()
        ));

        // Restart of the old datalake.
        flowChain.add(new SdxStartStartEvent(
                SDX_START_EVENT.event(), event.getOldCluster().getId(), event.getUserId()
        ));

        return new FlowTriggerEventQueue(getName(), event, flowChain);
    }
}
