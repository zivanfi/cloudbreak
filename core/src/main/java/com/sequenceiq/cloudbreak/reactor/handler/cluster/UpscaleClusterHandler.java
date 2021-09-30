package com.sequenceiq.cloudbreak.reactor.handler.cluster;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.core.cluster.ClusterUpscaleService;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.UpscaleClusterRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.UpscaleClusterResult;
import com.sequenceiq.flow.event.EventSelectorUtil;
import com.sequenceiq.flow.reactor.api.handler.EventHandler;

import reactor.bus.Event;
import reactor.bus.EventBus;

@Component
public class UpscaleClusterHandler implements EventHandler<UpscaleClusterRequest> {

    private static final Logger PERF_LOGGER = LoggerFactory.getLogger("PERFORMANCE");

    @Inject
    private EventBus eventBus;

    @Inject
    private ClusterUpscaleService clusterUpscaleService;

    @Override
    public String selector() {
        return EventSelectorUtil.selector(UpscaleClusterRequest.class);
    }


    @Override
    public void accept(Event<UpscaleClusterRequest> event) {
        UpscaleClusterRequest request = event.getData();
        UpscaleClusterResult result;
        try {
            long timeStart = System.currentTimeMillis();


            clusterUpscaleService.installServicesOnNewHosts(request.getResourceId(), request.getHostGroupName(),
                    request.isRepair(), request.isRestartServices());

            long timeEnd = System.currentTimeMillis();
            PERF_LOGGER.error("'{}' : installServicesOnNewHosts : '{}'  \n\n\n ", this.getClass().getName(), ((timeEnd-timeStart) / 1000));

            result = new UpscaleClusterResult(request);
        } catch (Exception e) {
            result = new UpscaleClusterResult(e.getMessage(), e, request);
        }
        eventBus.notify(result.selector(), new Event<>(event.getHeaders(), result));
    }
}
