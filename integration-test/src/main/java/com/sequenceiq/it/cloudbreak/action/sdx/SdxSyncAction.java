package com.sequenceiq.it.cloudbreak.action.sdx;

import static java.lang.String.format;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.flow.api.model.FlowIdentifier;
import com.sequenceiq.it.cloudbreak.SdxClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.sdx.SdxTestDto;
import com.sequenceiq.it.cloudbreak.log.Log;
import com.sequenceiq.sdx.api.model.SdxClusterDetailResponse;

public class SdxSyncAction implements Action<SdxTestDto, SdxClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SdxSyncAction.class);

    @Override
    public SdxTestDto action(TestContext testContext, SdxTestDto testDto, SdxClient client) throws Exception {
        String sdxName = testDto.getName();

        Log.when(LOGGER, format(" SDX '%s' sync has been started... ", sdxName));
        Log.whenJson(LOGGER, " SDX sync request: ", testDto.getRequest());
        LOGGER.info(format(" SDX '%s' sync has been started... ", sdxName));
        FlowIdentifier flowIdentifier = client.getDefaultClient()
                .sdxEndpoint()
                .sync(sdxName);
        testDto.setFlow("SDX sync", flowIdentifier);
        SdxClusterDetailResponse detailedResponse = client.getDefaultClient()
                .sdxEndpoint()
                .getDetail(sdxName, Collections.emptySet());
        testDto.setResponse(detailedResponse);
        Log.whenJson(LOGGER, " SDX sync response: ", client.getDefaultClient().sdxEndpoint().get(sdxName));
        return testDto;
    }
}
