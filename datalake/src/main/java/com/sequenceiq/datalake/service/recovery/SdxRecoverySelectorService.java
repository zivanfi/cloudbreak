package com.sequenceiq.datalake.service.recovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.response.recovery.RecoveryStatus;
import com.sequenceiq.cloudbreak.common.exception.BadRequestException;
import com.sequenceiq.datalake.entity.SdxCluster;
import com.sequenceiq.sdx.api.model.SdxRecoverableResponse;
import com.sequenceiq.sdx.api.model.SdxRecoveryRequest;
import com.sequenceiq.sdx.api.model.SdxRecoveryResponse;

@Service
/*
 * Recovers an SDX cluster from a failure during Resize or Upgrade.
 *
 * Chooses the appropriate RecoveryService to use based on the state of the SDX cluster.
 */
public class SdxRecoverySelectorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SdxRecoverySelectorService.class);

    private  final List<RecoveryService> services;

    @Autowired
    public SdxRecoverySelectorService(List<RecoveryService> services) {
        this.services = services;
    }

    public SdxRecoveryResponse triggerRecovery(SdxCluster sdxCluster, SdxRecoveryRequest sdxRecoveryRequest) {

        List<String> responseReasons = new ArrayList<>();

        for (RecoveryService recoveryService : services) {
            SdxRecoverableResponse recoverableResponse = recoveryService.validateRecovery(sdxCluster);
            responseReasons.add(recoverableResponse.getReason());
            if (recoverableResponse.getStatus().recoverable()) {
                return recoveryService.triggerRecovery(sdxCluster, sdxRecoveryRequest);
            }
        }

        String reasons = String.join(",", responseReasons);
        LOGGER.debug("Cluster is not in a recoverable state with message(s): {}", reasons);
        throw new BadRequestException(reasons);

    }

    public SdxRecoverableResponse validateRecovery(SdxCluster sdxCluster) {

        SdxRecoverableResponse sdxRecoverableResponse = null;
        for (RecoveryService recoveryService : services) {
            if (recoveryService.canRecover(sdxCluster)) {
                sdxRecoverableResponse = recoveryService.validateRecovery(sdxCluster);
                if (sdxRecoverableResponse.getStatus().recoverable()) {
                    return sdxRecoverableResponse;
                }
            }
        }
        return Objects.requireNonNullElseGet(sdxRecoverableResponse,
                () -> new SdxRecoverableResponse("No valid Recovery Options avaiable", RecoveryStatus.NON_RECOVERABLE));
    }
}
