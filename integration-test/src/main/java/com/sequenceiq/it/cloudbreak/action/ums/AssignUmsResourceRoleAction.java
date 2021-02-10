package com.sequenceiq.it.cloudbreak.action.ums;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.it.cloudbreak.UmsClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.actor.CloudbreakUser;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.ums.UmsResourceTestDto;

public class AssignUmsResourceRoleAction implements Action<UmsResourceTestDto, UmsClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignUmsResourceRoleAction.class);

    private final String userKey;

    public AssignUmsResourceRoleAction(String userKey) {
        this.userKey = userKey;
    }

    @Override
    public UmsResourceTestDto action(TestContext testContext, UmsResourceTestDto testDto, UmsClient client) throws Exception {
        CloudbreakUser user = testContext.getRealUmsUserByKey(userKey);
        LOGGER.info(String.format("Assigning resourceRole %s over resource %s for user ",
                testDto.getRequest().getRoleCrn(), testDto.getRequest().getResourceCrn()), user.getCrn());
        client.getUmsClient().assignResourceRole(user.getCrn(), testDto.getRequest().getResourceCrn(), testDto.getRequest().getRoleCrn(), Optional.of(""));
        // wait for UmsRightsCache to expire
        Thread.sleep(7000);
        return testDto;
    }
}
