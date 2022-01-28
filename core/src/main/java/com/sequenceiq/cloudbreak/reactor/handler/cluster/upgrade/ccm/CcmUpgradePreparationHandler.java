package com.sequenceiq.cloudbreak.reactor.handler.cluster.upgrade.ccm;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.cloudbreak.core.flow2.cluster.ccm.upgrade.CcmUpgradeService;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.ccm.CcmUpgradePreparationFailed;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.ccm.CcmUpgradePreparationRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.ccm.CcmUpgradePreparationResult;
import com.sequenceiq.flow.event.EventSelectorUtil;
import com.sequenceiq.flow.reactor.api.handler.EventHandler;

import reactor.bus.Event;
import reactor.bus.EventBus;

@Component
public class CcmUpgradePreparationHandler implements EventHandler<CcmUpgradePreparationRequest> {

    @Inject
    private EventBus eventBus;

    @Inject
    private CcmUpgradeService ccmUpgradeService;

    @Override
    public String selector() {
        return EventSelectorUtil.selector(CcmUpgradePreparationRequest.class);
    }

    @Override
    public void accept(Event<CcmUpgradePreparationRequest> event) {
        Selectable result;
        CcmUpgradePreparationRequest request = event.getData();
        Long stackId = request.getResourceId();
        try {
            ccmUpgradeService.prepare(stackId);
            result = new CcmUpgradePreparationResult(stackId);
        } catch (Exception e) {
            result = new CcmUpgradePreparationFailed(stackId, e);
        }
        eventBus.notify(result.selector(), new Event<>(event.getHeaders(), result));
    }
}
