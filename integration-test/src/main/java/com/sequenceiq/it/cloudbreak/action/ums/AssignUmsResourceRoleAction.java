package com.sequenceiq.it.cloudbreak.action.ums;

import static java.lang.String.format;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.it.cloudbreak.UmsClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.actor.CloudbreakUser;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.ums.UmsResourceTestDto;
import com.sequenceiq.it.cloudbreak.log.Log;

public class AssignUmsResourceRoleAction implements Action<UmsResourceTestDto, UmsClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignUmsResourceRoleAction.class);

    private final String userKey;

    public AssignUmsResourceRoleAction(String userKey) {
        this.userKey = userKey;
    }

    @Override
    public UmsResourceTestDto action(TestContext testContext, UmsResourceTestDto testDto, UmsClient client) throws Exception {
        CloudbreakUser user = testContext.getRealUmsUserByKey(userKey);
        String resourceCrn = testDto.getRequest().getResourceCrn();
        LOGGER.info(String.format("Assign resource role: %s for user: %s at resource: %s",
                testDto.getRequest().getRoleCrn(), user.getCrn(), resourceCrn));
        client.getUmsClient().assignResourceRole(user.getCrn(), testDto.getRequest().getResourceCrn(), testDto.getRequest().getRoleCrn(), Optional.empty());
        // wait for UmsRightsCache to expire
        Thread.sleep(7000);
        Log.when(LOGGER, format(" Assigned resource role: %s for user: %s at resource: %s", testDto.getRequest().getRoleCrn(), user.getCrn(), resourceCrn));
        return testDto;
    }
}
