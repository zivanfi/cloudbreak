package com.sequenceiq.it.cloudbreak.action.ums;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.GetRightsResponse;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.MachineUser;
import com.sequenceiq.it.cloudbreak.UmsClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.environment.EnvironmentTestDto;
import com.sequenceiq.it.cloudbreak.dto.ums.UmsRoleTestDto;

public class GetRightsForActingUserAction implements Action<UmsRoleTestDto, UmsClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetRightsForActingUserAction.class);

    @Override
    public UmsRoleTestDto action(TestContext testContext, UmsRoleTestDto testDto, UmsClient client) throws Exception {
        String actingUserCrn = testContext.getActingUserCrn().toString();
        String accountId = testContext.getActingUserCrn().getAccountId();
        MachineUser machineUser = client.getMachineUserForUser(actingUserCrn, actingUserCrn, accountId);
        EnvironmentTestDto environment = testContext.given(EnvironmentTestDto.class);
        GetRightsResponse userRights = client.getUmsClient().getRightsForUser(actingUserCrn, machineUser.getCrn(), environment.getCrn(), Optional.empty());
        LOGGER.info(String.format("User %s has rights: ", testContext.getActingUser().getCrn()));
        userRights.getRoleAssignmentList().forEach(roleAssignment -> {
            LOGGER.info(String.format("%nrole CRN: %s", roleAssignment.getRole().getCrn()));
        });
        return testDto;
    }
}
