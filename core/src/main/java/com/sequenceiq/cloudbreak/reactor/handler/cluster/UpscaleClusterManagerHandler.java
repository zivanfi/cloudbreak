package com.sequenceiq.cloudbreak.reactor.handler.cluster;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.core.cluster.ClusterUpscaleService;
import com.sequenceiq.cloudbreak.reactor.api.event.orchestration.UpscaleClusterManagerRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.orchestration.UpscaleClusterManagerResult;
import com.sequenceiq.flow.event.EventSelectorUtil;
import com.sequenceiq.flow.reactor.api.handler.EventHandler;

import reactor.bus.Event;
import reactor.bus.EventBus;

@Component
public class UpscaleClusterManagerHandler implements EventHandler<UpscaleClusterManagerRequest> {

    @Inject
    private EventBus eventBus;

    @Inject
    private ClusterUpscaleService clusterUpscaleService;

    @Override
    public String selector() {
        return EventSelectorUtil.selector(UpscaleClusterManagerRequest.class);
    }

        private static final Logger PERF_LOGGER = LoggerFactory.getLogger("PERFORMANCE");


    @Override
    public void accept(Event<UpscaleClusterManagerRequest> event) {
        UpscaleClusterManagerRequest request = event.getData();
        UpscaleClusterManagerResult result;

        try {
          long timeStart = System.currentTimeMillis();

            clusterUpscaleService.upscaleClusterManager(request.getResourceId(), request.getHostGroupName(),
                    request.getScalingAdjustment(), request.isPrimaryGatewayChanged());
            long timeEnd = System.currentTimeMillis();;

            PERF_LOGGER.error(" Class '{}' : upscaleClusterManager : '{}'  \n\n\n", this.getClass().getName(), ((timeEnd-timeStart) / 1000));

            result = new UpscaleClusterManagerResult(request);
        } catch (Exception e) {
            result = new UpscaleClusterManagerResult(e.getMessage(), e, request);
        }
        eventBus.notify(result.selector(), new Event<>(event.getHeaders(), result));
    }
}
