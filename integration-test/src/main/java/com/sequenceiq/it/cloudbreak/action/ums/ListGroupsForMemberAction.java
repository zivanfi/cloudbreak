package com.sequenceiq.it.cloudbreak.action.ums;

import static java.lang.String.format;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.it.cloudbreak.UmsClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.ums.UmsRoleTestDto;
import com.sequenceiq.it.cloudbreak.log.Log;

public class ListGroupsForMemberAction implements Action<UmsRoleTestDto, UmsClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListGroupsForMemberAction.class);

    private final String userCrn;

    public ListGroupsForMemberAction(String userCrn) {
        this.userCrn = userCrn;
    }

    @Override
    public UmsRoleTestDto action(TestContext testContext, UmsRoleTestDto testDto, UmsClient client) throws Exception {
        LOGGER.info(String.format("List groups for user %s", testContext.getActingUser().getDisplayName()));
        String actingUserCrn = testContext.getActingUserCrn().toString();
        String accountId = testContext.getActingUserCrn().getAccountId();
        List<String> userGroups = client.getUmsClient().listGroupsForMember(actingUserCrn, accountId, userCrn, Optional.empty());
        LOGGER.info(String.format("User %s is member of groups: %s", testContext.getActingUser().getCrn(), userGroups));
        Log.when(LOGGER, format(" User %s is member of groups: %s", testContext.getActingUser().getCrn(), userGroups));
        return testDto;
    }
}
