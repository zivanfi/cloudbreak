package com.sequenceiq.it.cloudbreak.testcase.users.virtualgroups;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sequenceiq.authorization.info.model.RightV4;
import com.sequenceiq.environment.api.v1.environment.model.response.EnvironmentStatus;
import com.sequenceiq.freeipa.api.v1.freeipa.stack.model.common.Status;
import com.sequenceiq.it.cloudbreak.actor.CloudbreakActor;
import com.sequenceiq.it.cloudbreak.assertion.util.CheckResourceRightFalseAssertion;
import com.sequenceiq.it.cloudbreak.assertion.util.CheckResourceRightTrueAssertion;
import com.sequenceiq.it.cloudbreak.assertion.util.CheckRightFalseAssertion;
import com.sequenceiq.it.cloudbreak.assertion.util.CheckRightTrueAssertion;
import com.sequenceiq.it.cloudbreak.client.CredentialTestClient;
import com.sequenceiq.it.cloudbreak.client.EnvironmentTestClient;
import com.sequenceiq.it.cloudbreak.client.FreeIpaTestClient;
import com.sequenceiq.it.cloudbreak.client.UtilTestClient;
import com.sequenceiq.it.cloudbreak.context.Description;
import com.sequenceiq.it.cloudbreak.context.RunningParameter;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.credential.CredentialTestDto;
import com.sequenceiq.it.cloudbreak.dto.environment.EnvironmentTestDto;
import com.sequenceiq.it.cloudbreak.dto.freeipa.FreeIpaTestDto;
import com.sequenceiq.it.cloudbreak.dto.ums.UmsRoleTestDto;
import com.sequenceiq.it.cloudbreak.testcase.authorization.AuthUserKeys;
import com.sequenceiq.it.cloudbreak.testcase.mock.AbstractMockTest;
import com.sequenceiq.it.cloudbreak.util.AuthorizationTestUtil;

public class EnvironmentVirtualGroupsTest extends AbstractMockTest {

    @Inject
    private EnvironmentTestClient environmentTestClient;

    @Inject
    private CredentialTestClient credentialTestClient;

    @Inject
    private FreeIpaTestClient freeIpaTestClient;

    @Inject
    private UtilTestClient utilTestClient;

    @Inject
    private CloudbreakActor cloudbreakActor;

    @Inject
    private AuthorizationTestUtil authorizationTestUtil;

    @Override
    protected void setupTest(TestContext testContext) {
        useRealUmsUser(testContext, AuthUserKeys.ACCOUNT_ADMIN);
        useRealUmsUser(testContext, AuthUserKeys.ENV_ADMIN_A);
        useRealUmsUser(testContext, AuthUserKeys.ENV_CREATOR_A);
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "there is a running Cloudbreak",
            when = "a new environment should be created with CB-AccountAdmin" +
                   "CB-Machine-EnvAdminA should be added to the running environment" +
                   "CB-Machine-EnvCreatorA user should be added to the running environment",
            then = "a new user shou")
    public void testCreateEnvironment(TestContext testContext) {
        useRealUmsUser(testContext, AuthUserKeys.ENV_CREATOR_A);
        testContext
                .given(CredentialTestDto.class)
                .when(credentialTestClient.create())
                .given(EnvironmentTestDto.class)
                    .withCreateFreeIpa(false)
                .when(environmentTestClient.create())
                .await(EnvironmentStatus.AVAILABLE)
                .when(environmentTestClient.describe())
                .given(FreeIpaTestDto.class)
                    .withCatalog(getImageCatalogMockServerSetup().getFreeIpaImageCatalogUrl())
                .when(freeIpaTestClient.create())
                .await(Status.AVAILABLE)
                .when(freeIpaTestClient.describe())
                .validate();

        testContext
                //after assignment describe should work for the environment
                .given(UmsRoleTestDto.class)
                .assignTarget(EnvironmentTestDto.class.getSimpleName())
                    .withEnvironmentAdmin()
                .when(environmentTestClient.assignUserRole(AuthUserKeys.ENV_ADMIN_A))
                .withEnvironmentUser()
                .when(environmentTestClient.assignResourceRole(AuthUserKeys.ENV_CREATOR_B))
                .given(EnvironmentTestDto.class)
                .when(environmentTestClient.describe(), RunningParameter.who(cloudbreakActor.useRealUmsUser(AuthUserKeys.ENV_CREATOR_B)))
                .validate();

        testCheckRightUtil(testContext, testContext.given(EnvironmentTestDto.class).getCrn());

        testContext
                .given(EnvironmentTestDto.class)
                .when(environmentTestClient.delete(), RunningParameter.who(cloudbreakActor.useRealUmsUser(AuthUserKeys.ENV_CREATOR_A)))
                .validate();
    }

    private void testCheckRightUtil(TestContext testContext, String envCrn) {
        authorizationTestUtil.testCheckRightUtil(testContext, AuthUserKeys.ENV_CREATOR_A, new CheckRightTrueAssertion(),
                Lists.newArrayList(RightV4.ENV_CREATE), utilTestClient);
        authorizationTestUtil.testCheckRightUtil(testContext, AuthUserKeys.ZERO_RIGHTS, new CheckRightFalseAssertion(),
                Lists.newArrayList(RightV4.ENV_CREATE), utilTestClient);

        Map<String, List<RightV4>> resourceRightsToCheckForEnv = Maps.newHashMap();
        resourceRightsToCheckForEnv.put(envCrn, Lists.newArrayList(RightV4.ENV_DELETE, RightV4.ENV_START, RightV4.ENV_STOP));
        Map<String, List<RightV4>> resourceRightsToCheckForDhOnEnv = Maps.newHashMap();
        resourceRightsToCheckForDhOnEnv.put(envCrn, Lists.newArrayList(RightV4.DH_CREATE));
        authorizationTestUtil.testCheckResourceRightUtil(testContext, AuthUserKeys.ENV_CREATOR_A, new CheckResourceRightTrueAssertion(),
                resourceRightsToCheckForEnv, utilTestClient);
        authorizationTestUtil.testCheckResourceRightUtil(testContext, AuthUserKeys.ENV_CREATOR_A, new CheckResourceRightTrueAssertion(),
                resourceRightsToCheckForDhOnEnv, utilTestClient);
        authorizationTestUtil.testCheckResourceRightUtil(testContext, AuthUserKeys.ENV_CREATOR_B, new CheckResourceRightFalseAssertion(),
                resourceRightsToCheckForEnv, utilTestClient);
        authorizationTestUtil.testCheckResourceRightUtil(testContext, AuthUserKeys.ENV_CREATOR_B, new CheckResourceRightTrueAssertion(),
                resourceRightsToCheckForDhOnEnv, utilTestClient);
        authorizationTestUtil.testCheckResourceRightUtil(testContext, AuthUserKeys.ZERO_RIGHTS, new CheckResourceRightFalseAssertion(),
                resourceRightsToCheckForEnv, utilTestClient);
        authorizationTestUtil.testCheckResourceRightUtil(testContext, AuthUserKeys.ZERO_RIGHTS, new CheckResourceRightFalseAssertion(),
                resourceRightsToCheckForDhOnEnv, utilTestClient);
    }
}