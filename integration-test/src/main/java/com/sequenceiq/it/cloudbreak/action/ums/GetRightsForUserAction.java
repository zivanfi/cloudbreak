package com.sequenceiq.it.cloudbreak.action.ums;

import static java.lang.String.format;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.GetRightsResponse;
import com.sequenceiq.it.cloudbreak.UmsClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.ums.UmsRoleTestDto;
import com.sequenceiq.it.cloudbreak.log.Log;

public class GetRightsForUserAction implements Action<UmsRoleTestDto, UmsClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetRightsForUserAction.class);

    private final String userCrn;

    private final String environmentCrn;

    public GetRightsForUserAction(String userCrn, String environmentCrn) {
        this.userCrn = userCrn;
        this.environmentCrn = environmentCrn;
    }

    @Override
    public UmsRoleTestDto action(TestContext testContext, UmsRoleTestDto testDto, UmsClient client) throws Exception {
        String actingUserCrn = testContext.getActingUserCrn().toString();
        LOGGER.info(String.format("Get rights for %nenvironment: %s %nuser: %s %nacting user: %s", environmentCrn, userCrn, actingUserCrn));
        GetRightsResponse userRights = client.getUmsClient().getRightsForUser(actingUserCrn, userCrn, environmentCrn, Optional.empty());
        testDto.setResponse(userRights);
        LOGGER.info(String.format("User %s has %nroles: %s %nresource roles: %s %ngroups: %s", userCrn, userRights.getRoleAssignmentList(),
                userRights.getResourceRolesAssignmentList(), userRights.getGroupCrnList()));
        Log.when(LOGGER, format(" User %s has %nroles: %s %nresource roles: %s %ngroups: %s", userCrn, userRights.getRoleAssignmentList(),
                userRights.getResourceRolesAssignmentList(), userRights.getGroupCrnList()));
        return testDto;
    }
}
