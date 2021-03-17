package com.sequenceiq.it.cloudbreak.testcase.users.virtualgroups;

import static com.sequenceiq.it.cloudbreak.context.RunningParameter.expectedMessage;
import static com.sequenceiq.it.cloudbreak.util.AuthorizationTestUtil.environmentPattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.GetRightsResponse;
import com.google.common.collect.Lists;
import com.sequenceiq.authorization.info.model.RightV4;
import com.sequenceiq.cloudbreak.auth.altus.Crn;
import com.sequenceiq.cloudbreak.auth.altus.UmsRight;
import com.sequenceiq.cloudbreak.auth.altus.VirtualGroupRequest;
import com.sequenceiq.cloudbreak.auth.altus.VirtualGroupService;
import com.sequenceiq.environment.api.v1.environment.model.response.EnvironmentStatus;
import com.sequenceiq.freeipa.api.v1.operation.model.OperationState;
import com.sequenceiq.it.cloudbreak.EnvironmentClient;
import com.sequenceiq.it.cloudbreak.FreeIpaClient;
import com.sequenceiq.it.cloudbreak.UmsClient;
import com.sequenceiq.it.cloudbreak.actor.CloudbreakActor;
import com.sequenceiq.it.cloudbreak.assertion.util.CheckResourceRightFalseAssertion;
import com.sequenceiq.it.cloudbreak.assertion.util.CheckResourceRightTrueAssertion;
import com.sequenceiq.it.cloudbreak.client.CredentialTestClient;
import com.sequenceiq.it.cloudbreak.client.EnvironmentTestClient;
import com.sequenceiq.it.cloudbreak.client.FreeIpaTestClient;
import com.sequenceiq.it.cloudbreak.client.UmsTestClient;
import com.sequenceiq.it.cloudbreak.client.UtilTestClient;
import com.sequenceiq.it.cloudbreak.context.Description;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.credential.CredentialTestDto;
import com.sequenceiq.it.cloudbreak.dto.environment.EnvironmentTestDto;
import com.sequenceiq.it.cloudbreak.dto.freeipa.FreeIpaTestDto;
import com.sequenceiq.it.cloudbreak.dto.freeipa.FreeIpaUserSyncTestDto;
import com.sequenceiq.it.cloudbreak.dto.ums.UmsResourceTestDto;
import com.sequenceiq.it.cloudbreak.dto.ums.UmsRoleTestDto;
import com.sequenceiq.it.cloudbreak.exception.TestFailException;
import com.sequenceiq.it.cloudbreak.testcase.authorization.AuthUserKeys;
import com.sequenceiq.it.cloudbreak.testcase.mock.AbstractMockTest;
import com.sequenceiq.it.cloudbreak.util.AuthorizationTestUtil;
import com.sequenceiq.it.cloudbreak.util.ResourceCreator;

public class VirtualGroupTest extends AbstractMockTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualGroupTest.class);

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

    @Inject
    private UmsTestClient umsTestClient;

    @Inject
    private ResourceCreator resourceCreator;

    @Override
    protected void setupTest(TestContext testContext) {
        useRealUmsUser(testContext, AuthUserKeys.ENV_ADMIN_A);
        useRealUmsUser(testContext, AuthUserKeys.ENV_CREATOR_A);
        useRealUmsUser(testContext, AuthUserKeys.ACCOUNT_ADMIN);
        initializeDefaultBlueprints(testContext);
        createDefaultCredential(testContext);
        createDefaultImageCatalog(testContext);
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "there is a running Cloudbreak",
            when = "a new environment should be created with CB-AccountAdmin",
            then = "CB-Machine-EnvCreatorB user should be assigned to the running environment")
    public void testCreateEnvironment(TestContext testContext) {
        useRealUmsUser(testContext, AuthUserKeys.ACCOUNT_ADMIN);
        EnvironmentTestDto environment = resourceCreator.createDefaultEnvironment(testContext);
        resourceCreator.createNewFreeIpa(testContext, environment);

        testContext
                .given(EnvironmentTestDto.class)
                .whenException(environmentTestClient.describe(), ForbiddenException.class,
                        expectedMessage("Doesn't have 'environments/describeEnvironment' right on 'environment' " + environmentPattern(testContext))
                                .withWho(cloudbreakActor.useRealUmsUser(AuthUserKeys.ENV_ADMIN_A)))
                .validate();
        testContext
                .given(EnvironmentTestDto.class)
                .given(UmsResourceTestDto.class)
                .assignTarget(EnvironmentTestDto.class.getSimpleName())
                .withEnvironmentAdmin()
                .when(umsTestClient.assignResourceRole(AuthUserKeys.ENV_ADMIN_A))
                .withEnvironmentUser()
                .when(umsTestClient.assignResourceRole(AuthUserKeys.ENV_CREATOR_A))
                .validate();
        testContext
                .as(cloudbreakActor.useRealUmsUser(AuthUserKeys.ACCOUNT_ADMIN))
                .given(FreeIpaUserSyncTestDto.class)
                .when(freeIpaTestClient.syncAll())
                .await(OperationState.COMPLETED)
                .validate();
        testContext
                .given(UmsRoleTestDto.class)
                .when(umsTestClient.getRightsForUser(cloudbreakActor.useRealUmsUser(AuthUserKeys.ENV_ADMIN_A).getCrn(),
                        testContext.given(EnvironmentTestDto.class).getCrn()))
                .when(umsTestClient.getRightsForUser(cloudbreakActor.useRealUmsUser(AuthUserKeys.ENV_CREATOR_A).getCrn(),
                        testContext.given(EnvironmentTestDto.class).getCrn()))
                .then(this::validateEnvironmentUserRole)
                .then(this::validateEnvironmentUserResourceRole)
                .validate();
        testContext
                .given(EnvironmentTestDto.class)
                .when(environmentTestClient.describe())
                .then(this::validateResourceRights)
                .validate();
        testContext
                .given(FreeIpaTestDto.class)
                .when(freeIpaTestClient.describe())
                .then(this::validateVirtualGroupName)
                .then(this::validateVirtualGroupNameFormat)
                .then(this::validateUserVirtualGroup)
                .then(this::validateRangerAdminGroupName)
                .then(this::validateRangerAdmin)
                .validate();
        testContext
                .given(EnvironmentTestDto.class)
                .when(environmentTestClient.delete())
                .await(EnvironmentStatus.ARCHIVED)
                .given(CredentialTestDto.class)
                .when(credentialTestClient.delete())
                .validate();
    }

    private EnvironmentTestDto validateResourceRights(TestContext testContext, EnvironmentTestDto testDto, EnvironmentClient environmentClient) {
        Map<String, List<RightV4>> creatorResourceRights = new HashMap<String, List<RightV4>>() {{
            put(testDto.getCrn(), Lists.newArrayList(RightV4.ENV_DELETE, RightV4.ENV_START, RightV4.ENV_STOP));
        }};
        Map<String, List<RightV4>> adminResourceRights = new HashMap<String, List<RightV4>>() {{
            put(testDto.getCrn(), Lists.newArrayList(RightV4.ENV_DELETE));
        }};
        Map<String, List<RightV4>> userResourceRights = new HashMap<String, List<RightV4>>() {{
            put(testDto.getCrn(), Lists.newArrayList(RightV4.ENV_DESCRIBE, RightV4.SDX_DESCRIBE));
        }};
        authorizationTestUtil.testCheckResourceRightUtil(testContext, AuthUserKeys.ACCOUNT_ADMIN, new CheckResourceRightTrueAssertion(),
                creatorResourceRights, utilTestClient);
        authorizationTestUtil.testCheckResourceRightUtil(testContext, AuthUserKeys.ENV_ADMIN_A, new CheckResourceRightFalseAssertion(),
                adminResourceRights, utilTestClient);
        authorizationTestUtil.testCheckResourceRightUtil(testContext, AuthUserKeys.ENV_CREATOR_A, new CheckResourceRightTrueAssertion(),
                userResourceRights, utilTestClient);
        return testDto;
    }

    private UmsRoleTestDto validateEnvironmentUserRole(TestContext testContext, UmsRoleTestDto testDto, UmsClient umsClient) {
        GetRightsResponse getRightsResponse = testDto.getResponse();
        LOGGER.info(String.format("Environment '%s' should have '%s' user role assignment: %s", testContext.get(EnvironmentTestDto.class).getCrn(),
                UmsRoleTestDto.ENVIRONMENT_CREATOR_CRN, testDto.getResponse().getRoleAssignmentList()));
        Assertions.assertThat(
                getRightsResponse.getRoleAssignmentList().stream()
                        .anyMatch(roleAssignment -> roleAssignment.getRole().getCrn().equals(UmsRoleTestDto.ENVIRONMENT_CREATOR_CRN))).isTrue();
        return testDto;
    }

    private UmsRoleTestDto validateEnvironmentUserResourceRole(TestContext testContext, UmsRoleTestDto testDto, UmsClient umsClient) {
        GetRightsResponse getRightsResponse = testDto.getResponse();
        LOGGER.info(String.format("Environment '%s' should have '%s' resource role assignment: %s.", testContext.get(EnvironmentTestDto.class).getCrn(),
                UmsResourceTestDto.ENV_USER_CRN, testDto.getResponse().getResourceRolesAssignmentList()));
        Assertions.assertThat(
                getRightsResponse.getResourceRolesAssignmentList().stream()
                        .anyMatch(resourceRoleAssignment -> resourceRoleAssignment.getResourceRole().getCrn()
                                .equals(UmsResourceTestDto.ENV_USER_CRN))).isTrue();
        return testDto;
    }

    private FreeIpaTestDto validateVirtualGroupName(TestContext testContext, FreeIpaTestDto testDto, FreeIpaClient freeIpaClient) {
        String adminGroupName = testDto.getRequest().getFreeIpa().getAdminGroupName();
        if (StringUtils.isEmpty(adminGroupName)) {
            throw new TestFailException("FreeIpa admin group have not been created!");
        } else {
            LOGGER.info(String.format("FreeIpa admin group name is '%s'.", adminGroupName));
        }
        return testDto;
    }

    private FreeIpaTestDto validateVirtualGroupNameFormat(TestContext testContext, FreeIpaTestDto testDto, FreeIpaClient freeIpaClient) {
        String adminGroupName = testDto.getRequest().getFreeIpa().getAdminGroupName();
        if (StringUtils.startsWith(adminGroupName, "_c_")) {
            LOGGER.info(String.format("FreeIpa admin group name is '%s' with expected format.", adminGroupName));
        } else {
            throw new TestFailException("FreeIpa admin group name does not have correct format!");
        }
        return testDto;
    }

    private FreeIpaTestDto validateUserVirtualGroup(TestContext testContext, FreeIpaTestDto testDto, FreeIpaClient freeIpaClient) {
        String userName = testDto.getResponse().getAuthentication().getLoginUserName();
        String environmentCrn = testDto.getRequest().getEnvironmentCrn();
        String name = testDto.getName();
        long freeIpaId = Long.parseLong(Objects.requireNonNull(Crn.fromString(testDto.getCrn())).getResource());

        LOGGER.info(String.format("FreeIpa user details: %s", freeIpaClient.getFreeIpaClient().getClientTestV1Endpoint().userShow(freeIpaId, userName)));
        LOGGER.info(String.format("FreeIpa user group: %s", freeIpaClient.getFreeIpaClient().getLdapConfigV1Endpoint().getForCluster(environmentCrn, name)
                .getUserGroup()));
        return testDto;
    }

    private FreeIpaTestDto validateRangerAdminGroupName(TestContext testContext, FreeIpaTestDto testDto, FreeIpaClient freeIpaClient) {
        String environmentCrn = testDto.getRequest().getEnvironmentCrn();
        String name = testDto.getName();
        String adminGroup = freeIpaClient.getFreeIpaClient().getLdapConfigV1Endpoint().getForCluster(environmentCrn, name).getAdminGroup();
        VirtualGroupService virtualGroupService = new VirtualGroupService();
        LOGGER.info("Admin Group:", adminGroup);
        VirtualGroupRequest virtualGroupRequest = new VirtualGroupRequest(environmentCrn, adminGroup);
        LOGGER.info(String.format("Ranger admin group name: '%s'", virtualGroupService.getVirtualGroup(virtualGroupRequest, UmsRight.RANGER_ADMIN
                .getRight())));
        return testDto;
    }

    private FreeIpaTestDto validateRangerAdmin(TestContext testContext, FreeIpaTestDto testDto, FreeIpaClient freeIpaClient) {
        String environmentCrn = testDto.getRequest().getEnvironmentCrn();
        String name = testDto.getName();
        String adminGroup = freeIpaClient.getFreeIpaClient().getLdapConfigV1Endpoint().getForCluster(environmentCrn, name).getAdminGroup();
        VirtualGroupService virtualGroupService = new VirtualGroupService();
        LOGGER.info("Admin Group:", adminGroup);
        VirtualGroupRequest virtualGroupRequest = new VirtualGroupRequest(environmentCrn, adminGroup);
        LOGGER.info(String.format("Ranger admin group name: '%s'", virtualGroupService.getVirtualGroup(virtualGroupRequest, UmsRight.RANGER_ADMIN
                .getRight())));
        return testDto;
    }
}