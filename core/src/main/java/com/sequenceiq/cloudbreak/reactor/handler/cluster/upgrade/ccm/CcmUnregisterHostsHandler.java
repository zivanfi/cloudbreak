package com.sequenceiq.cloudbreak.reactor.handler.cluster.upgrade.ccm;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.cloudbreak.core.flow2.cluster.ccm.upgrade.CcmUpgradeService;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.ccm.CcmUnregisterHostsRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.ccm.CcmUnregisterHostsResult;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.ccm.CcmUpgradeFailed;
import com.sequenceiq.flow.event.EventSelectorUtil;
import com.sequenceiq.flow.reactor.api.handler.EventHandler;

import reactor.bus.Event;
import reactor.bus.EventBus;

@Component
public class CcmUnregisterHostsHandler implements EventHandler<CcmUnregisterHostsRequest> {

    @Inject
    private EventBus eventBus;

    @Inject
    private CcmUpgradeService ccmUpgradeService;

    @Override
    public String selector() {
        return EventSelectorUtil.selector(CcmUnregisterHostsRequest.class);
    }

    @Override
    public void accept(Event<CcmUnregisterHostsRequest> event) {
        CcmUnregisterHostsRequest request = event.getData();
        Long stackId = request.getResourceId();
        Selectable result;
        try {
            ccmUpgradeService.unregister(stackId);
            result = new CcmUnregisterHostsResult(stackId);
        } catch (Exception e) {
            result = new CcmUpgradeFailed(stackId, e);
        }
        eventBus.notify(result.selector(), new Event<>(event.getHeaders(), result));
    }
}
