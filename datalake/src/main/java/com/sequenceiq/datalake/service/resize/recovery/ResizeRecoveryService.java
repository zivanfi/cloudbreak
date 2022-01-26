package com.sequenceiq.datalake.service.resize.recovery;

import javax.inject.Inject;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.response.recovery.RecoveryStatus;
import com.sequenceiq.cloudbreak.auth.ThreadBasedUserCrnProvider;
import com.sequenceiq.cloudbreak.auth.altus.EntitlementService;
import com.sequenceiq.datalake.entity.DatalakeStatusEnum;
import com.sequenceiq.datalake.entity.SdxCluster;
import com.sequenceiq.datalake.entity.SdxStatusEntity;
import com.sequenceiq.datalake.flow.SdxReactorFlowManager;
import com.sequenceiq.datalake.flow.chain.DatalakeResizeFlowEventChainFactory;
import com.sequenceiq.datalake.service.recovery.RecoveryService;
import com.sequenceiq.datalake.service.sdx.SdxService;
import com.sequenceiq.datalake.service.sdx.status.SdxStatusService;
import com.sequenceiq.flow.core.Flow2Handler;
import com.sequenceiq.flow.domain.FlowLog;
import com.sequenceiq.flow.service.flowlog.FlowChainLogService;
import com.sequenceiq.sdx.api.model.SdxRecoverableResponse;
import com.sequenceiq.sdx.api.model.SdxRecoveryRequest;
import com.sequenceiq.sdx.api.model.SdxRecoveryResponse;

@Component
/**
 * Provides entrypoint for recovery of failed SDX resize.
 *
 * The main entry point is {@code triggerRecovery}, which starts a cloudbreak Flow to recover the Data Lake.
 * ensure a Resize recovery is appropriate using {@code canRecover}
 *
 */
public class ResizeRecoveryService implements RecoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResizeRecoveryService.class);

    @Inject
    private EntitlementService entitlementService;

    @Inject
    private SdxService sdxService;

    @Inject
    private SdxStatusService sdxStatusService;

    @Inject
    private SdxReactorFlowManager sdxReactorFlowManager;

    @Inject
    private Flow2Handler flow2Handler;

    @Inject
    private FlowChainLogService flowChainLogService;

    public boolean canRecover(SdxCluster sdxCluster) {
        FlowLog flowLog = flow2Handler.getFirstStateLogfromLatestFlow(sdxCluster.getId());
        if (entitlementService.isDatalakeResizeRecoveryEnabled(ThreadBasedUserCrnProvider.getAccountId())) {
            if (DatalakeResizeFlowEventChainFactory.class.getSimpleName().equals(flowChainLogService.getFlowChainType(flowLog.getFlowChainId()))) {
                SdxStatusEntity actualStatusForSdx = sdxStatusService.getActualStatusForSdx(sdxCluster);
                if (DatalakeStatusEnum.STOP_FAILED.equals(actualStatusForSdx.getStatus())) {
                    return true;
                }
            }
        }
        return false;
    }

    public SdxRecoverableResponse validateRecovery(SdxCluster sdxCluster) {
        SdxStatusEntity actualStatusForSdx = sdxStatusService.getActualStatusForSdx(sdxCluster);
        if (DatalakeStatusEnum.STOP_FAILED.equals(actualStatusForSdx.getStatus())) {
            return new SdxRecoverableResponse("Resize can be recovered from a failed stop", RecoveryStatus.RECOVERABLE);
        }
        return new SdxRecoverableResponse("Resize can not be recovered from this point", RecoveryStatus.NON_RECOVERABLE);
    }

    public SdxRecoveryResponse triggerRecovery(SdxCluster sdxCluster, SdxRecoveryRequest sdxRecoveryRequest) {
        SdxStatusEntity actualStatusForSdx = sdxStatusService.getActualStatusForSdx(sdxCluster);
        if (entitlementService.isDatalakeResizeRecoveryEnabled(ThreadBasedUserCrnProvider.getAccountId())) {
            if (DatalakeStatusEnum.STOP_FAILED.equals(actualStatusForSdx.getStatus())) {
                return new SdxRecoveryResponse(sdxReactorFlowManager.triggerSdxStartFlow(sdxCluster));
            }
        }
        throw new NotImplementedException("Cluster is currently in an unrecoverable state");

    }

}
