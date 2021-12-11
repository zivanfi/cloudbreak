package com.sequenceiq.datalake.flow.detach;

import static com.sequenceiq.datalake.flow.detach.SdxDetachEvent.SDX_DETACH_EXTERNAL_DB_SUCCESS_EVENT;
import static com.sequenceiq.datalake.flow.detach.SdxDetachEvent.SDX_DETACH_STACK_SUCCESS_EVENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateContext;
import org.springframework.test.util.ReflectionTestUtils;

import com.sequenceiq.datalake.entity.SdxCluster;
import com.sequenceiq.datalake.flow.SdxContext;
import com.sequenceiq.datalake.flow.SdxEvent;
import com.sequenceiq.datalake.flow.detach.event.SdxStartDetachRecoveryEvent;
import com.sequenceiq.flow.core.AbstractAction;
import com.sequenceiq.flow.core.AbstractActionTestSupport;
import com.sequenceiq.flow.core.FlowParameters;
import com.sequenceiq.flow.core.FlowRegister;
import com.sequenceiq.flow.reactor.ErrorHandlerAwareReactorEventFactory;

import reactor.bus.EventBus;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
public class SdxDetachActionsTest {
    private static final Long SDX_ID = 1L;

    private static final Long DIFFERENT_SDX_ID = 2L;

    private static final String FLOW_ID = "flowID";

    private static final String USER_ID = "userID";

    private static final String DETACHED_SDX = "DETACHED_SDX";

    @Mock
    private FlowRegister runningFlows;

    @Mock
    private EventBus eventBus;

    @Mock
    private ErrorHandlerAwareReactorEventFactory reactorEventFactory;

    @InjectMocks
    private final SdxDetachActions underTest = new SdxDetachActions();

    @Test
    public void testSdxAttachNewClusterCreateFlowContext() {
        FlowParameters flowParameters = new FlowParameters(FLOW_ID, null, null);

        SdxCluster cluster = new SdxCluster();
        cluster.setId(DIFFERENT_SDX_ID);
        ExtendedState extendedState = new DefaultExtendedState(Map.of(DETACHED_SDX, cluster));

        SdxEvent sdxDetachStackSuccessEvent = new SdxEvent(SDX_DETACH_STACK_SUCCESS_EVENT.event(), SDX_ID, USER_ID);
        StateContext sdxDetachStackSuccessStateContext = new DefaultStateContext(
                sdxDetachStackSuccessEvent, null, extendedState, null, null
        );

        SdxEvent sdxDetachExternalDBSuccessEvent = new SdxEvent(SDX_DETACH_EXTERNAL_DB_SUCCESS_EVENT.event(), SDX_ID, USER_ID);
        StateContext sdxDetachExternalDBSuccessStateContext = new DefaultStateContext(
                sdxDetachExternalDBSuccessEvent, null, extendedState, null, null
        );

        AbstractAction action = (AbstractAction) underTest.sdxAttachNewCluster();
        initActionPrivateFields(action);
        AbstractActionTestSupport testSupport = new AbstractActionTestSupport(action);

        checkFlowContext((SdxContext) testSupport.createFlowContext(flowParameters, sdxDetachStackSuccessStateContext, sdxDetachStackSuccessEvent));
        checkFlowContext((SdxContext) testSupport.createFlowContext(flowParameters, sdxDetachExternalDBSuccessStateContext, sdxDetachExternalDBSuccessEvent));
    }

    private void checkFlowContext(SdxContext context) {
        assertEquals(context.getUserId(), USER_ID);
        assertEquals(context.getSdxId(), DIFFERENT_SDX_ID);
    }

    @Test
    public void testSdxAttachNewClusterGetFailurePayload() {
        SdxEvent event = new SdxEvent("", SDX_ID, USER_ID);
        SdxContext context = new SdxContext(null, DIFFERENT_SDX_ID, USER_ID);
        Exception e = new Exception("");

        AbstractAction action = (AbstractAction) underTest.sdxAttachNewCluster();
        initActionPrivateFields(action);
        AbstractActionTestSupport testSupport = new AbstractActionTestSupport(action);

        checkFailurePayload(testSupport.getFailurePayload(event, Optional.of(context), e), DIFFERENT_SDX_ID);
        checkFailurePayload(testSupport.getFailurePayload(event, Optional.empty(), e), SDX_ID);
    }

    private void checkFailurePayload(Object payload, Long expectedSdxId) {
        assert payload instanceof SdxStartDetachRecoveryEvent;
        SdxStartDetachRecoveryEvent result = (SdxStartDetachRecoveryEvent) payload;
        assertTrue(result.isFailureEvent());
        assertEquals(result.getUserId(), USER_ID);
        assertEquals(result.getResourceId(), expectedSdxId);
    }

    private void initActionPrivateFields(Action<?, ?> action) {
        ReflectionTestUtils.setField(action, null, runningFlows, FlowRegister.class);
        ReflectionTestUtils.setField(action, null, eventBus, EventBus.class);
        ReflectionTestUtils.setField(action, null, reactorEventFactory, ErrorHandlerAwareReactorEventFactory.class);
    }
}
