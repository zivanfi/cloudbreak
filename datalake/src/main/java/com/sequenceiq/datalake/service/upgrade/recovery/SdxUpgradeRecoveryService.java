package com.sequenceiq.datalake.service.upgrade.recovery;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.StackV4Endpoint;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.response.recovery.RecoveryValidationV4Response;
import com.sequenceiq.cloudbreak.auth.ThreadBasedUserCrnProvider;
import com.sequenceiq.cloudbreak.common.exception.WebApplicationExceptionMessageExtractor;
import com.sequenceiq.cloudbreak.exception.CloudbreakApiException;
import com.sequenceiq.cloudbreak.logger.MDCBuilder;
import com.sequenceiq.cloudbreak.message.CloudbreakMessagesService;
import com.sequenceiq.datalake.entity.SdxCluster;
import com.sequenceiq.datalake.flow.SdxReactorFlowManager;
import com.sequenceiq.datalake.flow.chain.DatalakeUpgradeFlowEventChainFactory;
import com.sequenceiq.datalake.service.FreeipaService;
import com.sequenceiq.datalake.service.recovery.RecoveryService;
import com.sequenceiq.datalake.service.sdx.SdxService;
import com.sequenceiq.flow.api.model.FlowIdentifier;
import com.sequenceiq.flow.core.Flow2Handler;
import com.sequenceiq.flow.domain.FlowLog;
import com.sequenceiq.flow.service.flowlog.FlowChainLogService;
import com.sequenceiq.sdx.api.model.SdxRecoverableResponse;
import com.sequenceiq.sdx.api.model.SdxRecoveryRequest;
import com.sequenceiq.sdx.api.model.SdxRecoveryResponse;

@Component
public class SdxUpgradeRecoveryService implements RecoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SdxUpgradeRecoveryService.class);

    @Inject
    private SdxService sdxService;

    @Inject
    private SdxReactorFlowManager sdxReactorFlowManager;

    @Inject
    private CloudbreakMessagesService messagesService;

    @Inject
    private FreeipaService freeipaService;

    @Inject
    private StackV4Endpoint stackV4Endpoint;

    @Inject
    private WebApplicationExceptionMessageExtractor exceptionMessageExtractor;

    @Inject
    private Flow2Handler flow2Handler;

    @Inject
    private FlowChainLogService flowChainLogService;

    public SdxRecoveryResponse triggerRecovery(SdxCluster sdxCluster, SdxRecoveryRequest recoverRequest) {
        MDCBuilder.buildMdcContext(sdxCluster);

        FlowIdentifier flowIdentifier = sdxReactorFlowManager.triggerDatalakeRuntimeRecoveryFlow(sdxCluster, recoverRequest.getType());
        return new SdxRecoveryResponse(flowIdentifier);
    }

    public SdxRecoverableResponse validateRecovery(SdxCluster sdxCluster) {
        MDCBuilder.buildMdcContext(sdxCluster);
        try {
            String initiatorUserCrn = ThreadBasedUserCrnProvider.getUserCrn();
            RecoveryValidationV4Response response = ThreadBasedUserCrnProvider.doAsInternalActor(() ->
                    stackV4Endpoint.getClusterRecoverableByNameInternal(0L, sdxCluster.getClusterName(), initiatorUserCrn));
            return new SdxRecoverableResponse(response.getReason(), response.getStatus());
        } catch (WebApplicationException e) {
            String exceptionMessage = exceptionMessageExtractor.getErrorMessage(e);
            String message = String.format("Stack recovery validation failed on cluster: [%s]. Message: [%s]",
                    sdxCluster.getClusterName(), exceptionMessage);
            throw new CloudbreakApiException(message, e);
        }
    }

    public boolean canRecover(SdxCluster sdxCluster) {
        FlowLog flowLog = flow2Handler.getFirstStateLogfromLatestFlow(sdxCluster.getId());
        return DatalakeUpgradeFlowEventChainFactory.class.getSimpleName()
                .equals(flowChainLogService.getFlowChainType(flowLog.getFlowChainId()));
    }

}
