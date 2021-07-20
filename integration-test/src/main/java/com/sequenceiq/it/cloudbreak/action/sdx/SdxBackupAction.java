package com.sequenceiq.it.cloudbreak.action.sdx;

import static java.lang.String.format;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.it.cloudbreak.SdxClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.sdx.SdxTestDto;
import com.sequenceiq.it.cloudbreak.log.Log;
import com.sequenceiq.sdx.api.model.SdxBackupResponse;
import com.sequenceiq.sdx.api.model.SdxClusterDetailResponse;

public class SdxBackupAction implements Action<SdxTestDto, SdxClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SdxBackupAction.class);

    private final String backupLocation;

    private final String backupName;

    public SdxBackupAction(String backupLocation, String backupName) {
        this.backupLocation = backupLocation;
        this.backupName = backupName;
    }

    @Override
    public SdxTestDto action(TestContext testContext, SdxTestDto testDto, SdxClient client) throws Exception {
        String sdxName = testDto.getName();

        Log.when(LOGGER, format(" SDX '%s' backup has been started to '%s' as '%s' ", sdxName, backupLocation, backupName));
        Log.whenJson(LOGGER, " SDX backup request: ", testDto.getRequest());
        LOGGER.info(format(" SDX '%s' backup has been started to '%s' as '%s'... ", sdxName, backupLocation, backupName));

        try {
            TimeUnit.MINUTES.sleep(3);
        } catch (InterruptedException ignored) {
            LOGGER.warn("Waiting for Cloudera Manager services has been interrupted at resource {}, because of: {}", sdxName,
                    ignored.getMessage(), ignored);
        }

        SdxBackupResponse sdxBackupResponse = client.getDefaultClient()
                .sdxEndpoint()
                .backupDatalakeByName(sdxName, backupLocation, backupName);
        testDto.setFlow("SDX backup", sdxBackupResponse.getFlowIdentifier());

        SdxClusterDetailResponse detailedResponse = client.getDefaultClient()
                .sdxEndpoint()
                .getDetail(sdxName, Collections.emptySet());
        testDto.setResponse(detailedResponse);
        Log.whenJson(LOGGER, " SDX backup response: ", client.getDefaultClient().sdxEndpoint().get(sdxName));
        return testDto;
    }
}
