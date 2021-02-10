package com.sequenceiq.it.cloudbreak.action.ums;

public class AssignRoleRequest {

    private String userCrn;

    private String roleCrn;

    public AssignRoleRequest() {
    }

    public AssignRoleRequest(String userCrn, String roleCrn) {
        this.userCrn = userCrn;
        this.roleCrn = roleCrn;
    }

    public String getUserCrn() {
        return userCrn;
    }

    public String getRoleCrn() {
        return roleCrn;
    }

    public void setUserCrn(String userCrn) {
        this.userCrn = userCrn;
    }

    public void setRoleCrn(String roleCrn) {
        this.roleCrn = roleCrn;
    }
}
