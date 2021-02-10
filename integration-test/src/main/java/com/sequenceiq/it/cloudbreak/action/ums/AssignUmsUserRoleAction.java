package com.sequenceiq.it.cloudbreak.action.ums;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.MachineUser;
import com.sequenceiq.it.cloudbreak.UmsClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.ums.UmsRoleTestDto;

public class AssignUmsUserRoleAction implements Action<UmsRoleTestDto, UmsClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignUmsUserRoleAction.class);

    private final String roleCrn;

    public AssignUmsUserRoleAction(String roleCrn) {
        this.roleCrn = roleCrn;
    }

    @Override
    public UmsRoleTestDto action(TestContext testContext, UmsRoleTestDto testDto, UmsClient client) throws Exception {
        String actingUserCrn = testContext.getActingUserCrn().toString();
        String accountId = testContext.getActingUserCrn().getAccountId();
        MachineUser machineUser = client.getMachineUserForUser(actingUserCrn, actingUserCrn, accountId);

        LOGGER.info(String.format("Assigning userRole %s for acting user ",
                testDto.getRequest().getRoleCrn(), testDto.getRequest().getRoleCrn()), testContext.getActingUser().getCrn());
        client.getUmsClient().assignMachineUserRole(actingUserCrn, accountId, machineUser.getCrn(), roleCrn, Optional.of(null));
        // wait for UmsRightsCache to expire
        Thread.sleep(7000);
        return testDto;

    }
}
