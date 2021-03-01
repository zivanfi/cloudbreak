package com.sequenceiq.it.cloudbreak.client;

import org.springframework.stereotype.Service;

import com.sequenceiq.it.cloudbreak.UmsClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.action.ums.AssignUmsResourceRoleAction;
import com.sequenceiq.it.cloudbreak.action.ums.AssignUmsUserRoleAction;
import com.sequenceiq.it.cloudbreak.action.ums.GetRightsForUserAction;
import com.sequenceiq.it.cloudbreak.dto.ums.UmsResourceTestDto;
import com.sequenceiq.it.cloudbreak.dto.ums.UmsRoleTestDto;

@Service
public class UmsTestClient {

    public Action<UmsResourceTestDto, UmsClient> assignResourceRole(String userKey) {
        return new AssignUmsResourceRoleAction(userKey);
    }

    public Action<UmsRoleTestDto, UmsClient> assignUserRole() {
        return new AssignUmsUserRoleAction();
    }

    public Action<UmsRoleTestDto, UmsClient> getRightsForUser() {
        return new GetRightsForUserAction();
    }
}
