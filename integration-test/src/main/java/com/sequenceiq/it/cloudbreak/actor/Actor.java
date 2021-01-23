package com.sequenceiq.it.cloudbreak.actor;

public interface Actor {

    CloudbreakUser getDefaultUser();

    CloudbreakUser getSecondUser();

    CloudbreakUser createNewUser(String tenantName, String username);

    CloudbreakUser getRealUmsUser(String key);
}