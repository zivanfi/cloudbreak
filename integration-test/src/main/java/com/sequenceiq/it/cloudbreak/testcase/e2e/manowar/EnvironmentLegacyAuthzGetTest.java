package com.sequenceiq.it.cloudbreak.testcase.e2e.manowar;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.sequenceiq.environment.api.v1.environment.model.response.EnvironmentStatus;
import com.sequenceiq.freeipa.api.v1.freeipa.stack.model.common.Status;
import com.sequenceiq.it.cloudbreak.actor.CloudbreakActor;
import com.sequenceiq.it.cloudbreak.client.CredentialTestClient;
import com.sequenceiq.it.cloudbreak.client.EnvironmentTestClient;
import com.sequenceiq.it.cloudbreak.client.FreeIpaTestClient;
import com.sequenceiq.it.cloudbreak.context.Description;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.environment.EnvironmentTestDto;
import com.sequenceiq.it.cloudbreak.dto.freeipa.FreeIpaTestDto;
import com.sequenceiq.it.cloudbreak.dto.telemetry.TelemetryTestDto;
import com.sequenceiq.it.cloudbreak.testcase.authorization.AuthUserKeys;
import com.sequenceiq.it.cloudbreak.testcase.e2e.AbstractE2ETest;

public class EnvironmentLegacyAuthzGetTest extends AbstractE2ETest {

    private static final String REAL_UMS_ACCOUNT_KEY = "legacy";

    private static final String REAL_UMS_ENVIRONMENT_KEY = "dev";

    @Inject
    private EnvironmentTestClient environmentTestClient;

    @Inject
    private CredentialTestClient credentialTestClient;

    @Inject
    private FreeIpaTestClient freeIpaTestClient;

    @Inject
    private CloudbreakActor cloudbreakActor;

    @Override
    protected void setupTest(TestContext testContext) {
        useRealUmsUser(testContext, AuthUserKeys.LEGACY_NON_POWER, REAL_UMS_ENVIRONMENT_KEY, REAL_UMS_ACCOUNT_KEY);
        useRealUmsUser(testContext, AuthUserKeys.LEGACY_POWER, REAL_UMS_ENVIRONMENT_KEY, REAL_UMS_ACCOUNT_KEY);
        useRealUmsUser(testContext, AuthUserKeys.LEGACY_ACC_ENV_ADMIN, REAL_UMS_ENVIRONMENT_KEY, REAL_UMS_ACCOUNT_KEY);
        initializeDefaultBlueprints(testContext);
        createDefaultCredential(testContext);
        createDefaultImageCatalog(testContext);
    }

    @Test(dataProvider = TEST_CONTEXT)
    @Description(
            given = "there is a running env service",
            when = "valid create environment request is sent",
            then = "environment should be created but unauthorized users should not be able to access it")
    public void testCreateEnvironment(TestContext testContext) {
        useRealUmsUser(testContext, AuthUserKeys.LEGACY_ACC_ENV_ADMIN, REAL_UMS_ENVIRONMENT_KEY, REAL_UMS_ACCOUNT_KEY);
        testContext
                .given(EnvironmentTestDto.class)
                .withCreateFreeIpa(false)
                .when(environmentTestClient.create())
                .await(EnvironmentStatus.AVAILABLE)
                .when(environmentTestClient.describe())
                .given("telemetry", TelemetryTestDto.class)
                .withLogging()
                .withReportClusterLogs()
                .given(FreeIpaTestDto.class)
                .withTelemetry("telemetry")
                .when(freeIpaTestClient.create())
                .await(Status.AVAILABLE)
                .when(freeIpaTestClient.describe())
                .validate();
    }
}
