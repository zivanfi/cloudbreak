package com.sequenceiq.it.cloudbreak.dto.ums;

import static com.sequenceiq.it.cloudbreak.context.RunningParameter.emptyRunningParameter;

import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.GetRightsResponse;
import com.sequenceiq.it.cloudbreak.Prototype;
import com.sequenceiq.it.cloudbreak.UmsClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.action.ums.AssignRoleRequest;
import com.sequenceiq.it.cloudbreak.actor.CloudbreakUser;
import com.sequenceiq.it.cloudbreak.assertion.Assertion;
import com.sequenceiq.it.cloudbreak.context.RunningParameter;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.AbstractTestDto;

@Prototype
public class UmsRoleTestDto extends AbstractTestDto<AssignRoleRequest, GetRightsResponse, UmsRoleTestDto, UmsClient> {

    public static final String CLASSIC_CLUSTERSCREATOR_CRN = "crn:altus:iam:us-west-1:altus:role:ClassicClustersCreator";

    public static final String DF_CATALOGADMIN_CRN = "crn:altus:iam:us-west-1:altus:role:DFCatalogAdmin";

    public static final String DATACATALOG_CSPRULE_MANAGER_CRN = "crn:altus:iam:us-west-1:altus:role:DataCatalogCspRuleManager";

    public static final String DATACATALOG_CSPRULE_VIEWER_CRN = "crn:altus:iam:us-west-1:altus:role:DataCatalogCspRuleViewer";

    public static final String DATAENG_USER_CRN = "crn:altus:iam:us-west-1:altus:role:DataEngUser";

    public static final String DATAWARE_USER_CRN = "crn:altus:iam:us-west-1:altus:role:DatawareUser";

    public static final String DBUS_UPLOADER_CRN = "crn:altus:iam:us-west-1:altus:role:DbusUploader";

    public static final String ENVIRONMENT_ADMIN_CRN = "crn:altus:iam:us-west-1:altus:role:EnvironmentAdmin";

    public static final String ENVIRONMENT_CREATOR_CRN = "crn:altus:iam:us-west-1:altus:role:EnvironmentCreator";

    public static final String ENVIRONMENT_USER_CRN = "crn:altus:iam:us-west-1:altus:role:EnvironmentUser";

    public static final String IAM_USER_CRN = "crn:altus:iam:us-west-1:altus:role:IamUser";

    public static final String IAM_VIEWER_CRN = "crn:altus:iam:us-west-1:altus:role:IamViewer";

    public static final String POWER_USER_CRN = "crn:altus:iam:us-west-1:altus:role:PowerUser";

    public static final String SDX_ADMIN_CRN = "crn:altus:iam:us-west-1:altus:role:SdxAdmin";

    private static final String UMS = "UMS";

    public UmsRoleTestDto(TestContext testContext) {
        super(new AssignRoleRequest(), testContext);
    }

    public UmsRoleTestDto() {
        super(UmsRoleTestDto.class.getSimpleName().toUpperCase());
        setRequest(new AssignRoleRequest());
    }

    public UmsRoleTestDto withClassicClustersCreator() {
        getRequest().setRoleCrn(CLASSIC_CLUSTERSCREATOR_CRN);
        return this;
    }

    public UmsRoleTestDto withDFCatalogAdmin() {
        getRequest().setRoleCrn(DF_CATALOGADMIN_CRN);
        return this;
    }

    public UmsRoleTestDto withDataCatalogCspRuleManager() {
        getRequest().setRoleCrn(DATACATALOG_CSPRULE_MANAGER_CRN);
        return this;
    }

    public UmsRoleTestDto withDataCatalogCspRuleViewer() {
        getRequest().setRoleCrn(DATACATALOG_CSPRULE_VIEWER_CRN);
        return this;
    }

    public UmsRoleTestDto withDataEngUse() {
        getRequest().setRoleCrn(DATAENG_USER_CRN);
        return this;
    }

    public UmsRoleTestDto withDatawareUser() {
        getRequest().setRoleCrn(DATAWARE_USER_CRN);
        return this;
    }

    public UmsRoleTestDto withDbusUploader() {
        getRequest().setRoleCrn(DBUS_UPLOADER_CRN);
        return this;
    }

    public UmsRoleTestDto withEnvironmentAdmin() {
        getRequest().setRoleCrn(ENVIRONMENT_ADMIN_CRN);
        return this;
    }

    public UmsRoleTestDto withEnvironmentCreator() {
        getRequest().setRoleCrn(ENVIRONMENT_CREATOR_CRN);
        return this;
    }

    public UmsRoleTestDto withEnvironmentUser() {
        getRequest().setRoleCrn(ENVIRONMENT_USER_CRN);
        return this;
    }

    public UmsRoleTestDto withIamUser() {
        getRequest().setRoleCrn(IAM_USER_CRN);
        return this;
    }

    public UmsRoleTestDto withIamViewer() {
        getRequest().setRoleCrn(IAM_VIEWER_CRN);
        return this;
    }

    public UmsRoleTestDto withPowerUser() {
        getRequest().setRoleCrn(POWER_USER_CRN);
        return this;
    }

    public UmsRoleTestDto withSdxAdmin() {
        getRequest().setRoleCrn(SDX_ADMIN_CRN);
        return this;
    }

    public UmsRoleTestDto withActingUser() {
        CloudbreakUser user = getTestContext().getActingUser();
        getRequest().setUserCrn(user.getCrn());
        return this;
    }

    public UmsRoleTestDto valid() {
        return withActingUser();
    }

    @Override
    public UmsRoleTestDto when(Action<UmsRoleTestDto, UmsClient> action) {
        return getTestContext().when((UmsRoleTestDto) this, UmsClient.class, action, emptyRunningParameter());
    }

    @Override
    public UmsRoleTestDto when(Action<UmsRoleTestDto, UmsClient> action, RunningParameter runningParameter) {
        return getTestContext().when((UmsRoleTestDto) this, UmsClient.class, action, runningParameter);
    }

    @Override
    public UmsRoleTestDto then(Assertion<UmsRoleTestDto, UmsClient> assertion) {
        return then(assertion, emptyRunningParameter());
    }

    @Override
    public UmsRoleTestDto then(Assertion<UmsRoleTestDto, UmsClient> assertion, RunningParameter runningParameter) {
        return getTestContext().then((UmsRoleTestDto) this, UmsClient.class, assertion, runningParameter);
    }
}
