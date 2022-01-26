package com.sequenceiq.datalake.service.resize.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.response.recovery.RecoveryStatus;
import com.sequenceiq.cloudbreak.auth.ThreadBasedUserCrnProvider;
import com.sequenceiq.cloudbreak.auth.altus.EntitlementService;
import com.sequenceiq.datalake.entity.DatalakeStatusEnum;
import com.sequenceiq.datalake.entity.SdxCluster;
import com.sequenceiq.datalake.entity.SdxStatusEntity;
import com.sequenceiq.datalake.flow.SdxReactorFlowManager;
import com.sequenceiq.datalake.flow.chain.DatalakeResizeFlowEventChainFactory;
import com.sequenceiq.datalake.service.sdx.SdxService;
import com.sequenceiq.datalake.service.sdx.status.SdxStatusService;
import com.sequenceiq.flow.api.model.FlowIdentifier;
import com.sequenceiq.flow.api.model.FlowType;
import com.sequenceiq.flow.core.Flow2Handler;
import com.sequenceiq.flow.domain.FlowLog;
import com.sequenceiq.flow.service.flowlog.FlowChainLogService;
import com.sequenceiq.sdx.api.model.SdxRecoverableResponse;
import com.sequenceiq.sdx.api.model.SdxRecoveryRequest;
import com.sequenceiq.sdx.api.model.SdxRecoveryResponse;
import com.sequenceiq.sdx.api.model.SdxRecoveryType;

@ExtendWith(MockitoExtension.class)

public class ResizeRecoveryServiceTest {

    private static final String USER_CRN = "crn:cdp:iam:us-west-1:cloudera:user:bob@cloudera.com";

    private static final Long CLUSTER_ID = 1L;

    private static final String FLOWCHAIN_ID = "SOME_FLOW";

    private static final long WORKSPACE_ID = 0L;

    @Mock
    private SdxService sdxService;

    @Mock
    private SdxCluster cluster;

    @Mock
    private SdxStatusService sdxStatusService;

    @Mock
    private SdxReactorFlowManager sdxReactorFlowManager;

    @Mock
    private Flow2Handler flow2Handler;

    @Mock
    private EntitlementService entitlementService;

    @Mock
    private FlowChainLogService flowChainLogService;

    @InjectMocks
    private ResizeRecoveryService underTest;

    private SdxRecoveryRequest request;

    private SdxStatusEntity sdxStatusEntity = new SdxStatusEntity();

    @BeforeEach
    public void setup() {
        request = new SdxRecoveryRequest();
        request.setType(SdxRecoveryType.RECOVER_WITHOUT_DATA);
        lenient().when(sdxStatusService.getActualStatusForSdx(cluster)).thenReturn(sdxStatusEntity);
        lenient().when(entitlementService.isDatalakeResizeRecoveryEnabled(anyString())).thenReturn(true);
    }

    @Test
    public void testCanRecoverCheckFlowChainStatusNoChain() {

        when(cluster.getId()).thenReturn(CLUSTER_ID);
        FlowLog flowLog = new FlowLog();
        flowLog.setFlowChainId(FLOWCHAIN_ID);
        when(flow2Handler.getFirstStateLogfromLatestFlow(CLUSTER_ID)).thenReturn(flowLog);
        sdxStatusEntity.setStatus(DatalakeStatusEnum.RUNNING);
        when(flowChainLogService.getFlowChainType(FLOWCHAIN_ID)).thenReturn(null);
        Boolean sdxRecoverableResponse =
                ThreadBasedUserCrnProvider.doAs(USER_CRN, () -> underTest.canRecover(cluster));
        assertFalse(sdxRecoverableResponse);
    }

    @Test
    public void testCanRecoverCheckFlowChainRecoverable() {

        when(cluster.getId()).thenReturn(CLUSTER_ID);
        FlowLog flowLog = new FlowLog();
        flowLog.setFlowChainId(FLOWCHAIN_ID);
        when(flow2Handler.getFirstStateLogfromLatestFlow(CLUSTER_ID)).thenReturn(flowLog);
        sdxStatusEntity.setStatus(DatalakeStatusEnum.STOP_FAILED);
        when(flowChainLogService.getFlowChainType(FLOWCHAIN_ID)).thenReturn(DatalakeResizeFlowEventChainFactory.class.getSimpleName());
        Boolean sdxRecoverableResponse =
                ThreadBasedUserCrnProvider.doAs(USER_CRN, () -> underTest.canRecover(cluster));
        assertTrue(sdxRecoverableResponse);
    }

    @Test
    public void testGetClusterRecoverableForSuccessNotRecoverable() {

        sdxStatusEntity.setStatus(DatalakeStatusEnum.STOPPED);
        SdxRecoverableResponse sdxRecoverableResponse =
                ThreadBasedUserCrnProvider.doAs(USER_CRN, () -> underTest.validateRecovery(cluster));
        assertEquals(sdxRecoverableResponse.getStatus(), RecoveryStatus.NON_RECOVERABLE);
    }

    @Test
    public void testGetClusterRecoverableForStopFailedRecoverable() {
        sdxStatusEntity.setStatus(DatalakeStatusEnum.STOP_FAILED);
        SdxRecoverableResponse sdxRecoverableResponse =
                ThreadBasedUserCrnProvider.doAs(USER_CRN, () -> underTest.validateRecovery(cluster));
        assertEquals(sdxRecoverableResponse.getStatus(), RecoveryStatus.RECOVERABLE);
    }

    @Test
    public void testTriggerRecovertShouldStartFlow() {
        sdxStatusEntity.setStatus(DatalakeStatusEnum.STOP_FAILED);

        FlowIdentifier flowId = new FlowIdentifier(FlowType.FLOW, "FLOW_ID");
        when(sdxReactorFlowManager.triggerSdxStartFlow(cluster))
                .thenReturn(flowId);
        SdxRecoveryResponse sdxRecoveryResponse =
                ThreadBasedUserCrnProvider.doAs(USER_CRN, () -> underTest.triggerRecovery(cluster, request));

        verify(sdxReactorFlowManager).triggerSdxStartFlow(cluster);
        assertEquals(sdxRecoveryResponse.getFlowIdentifier(), flowId);

    }
}
