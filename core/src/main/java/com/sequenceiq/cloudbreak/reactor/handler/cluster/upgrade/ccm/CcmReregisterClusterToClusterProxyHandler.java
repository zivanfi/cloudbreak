package com.sequenceiq.cloudbreak.reactor.handler.cluster.upgrade.ccm;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.cloudbreak.core.flow2.cluster.ccm.upgrade.CcmUpgradeService;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.ccm.CcmReregisterToClusterProxyRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.ccm.CcmReregisterToClusterProxyResult;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.ccm.CcmUpgradeFailed;
import com.sequenceiq.flow.event.EventSelectorUtil;
import com.sequenceiq.flow.reactor.api.handler.EventHandler;

import reactor.bus.Event;
import reactor.bus.EventBus;

@Component
public class CcmReregisterClusterToClusterProxyHandler implements EventHandler<CcmReregisterToClusterProxyRequest> {

    @Inject
    private EventBus eventBus;

    @Inject
    private CcmUpgradeService ccmUpgradeService;

    @Override
    public String selector() {
        return EventSelectorUtil.selector(CcmReregisterToClusterProxyRequest.class);
    }

    @Override
    public void accept(Event<CcmReregisterToClusterProxyRequest> event) {
        CcmReregisterToClusterProxyRequest request = event.getData();
        Long stackId = request.getResourceId();
        Selectable result;
        try {
            ccmUpgradeService.reregister(request.getResourceId());
            result = new CcmReregisterToClusterProxyResult(request.getResourceId());
        } catch (Exception e) {
            result = new CcmUpgradeFailed(stackId, e);
        }
        eventBus.notify(result.selector(), new Event<>(event.getHeaders(), result));
    }

}
