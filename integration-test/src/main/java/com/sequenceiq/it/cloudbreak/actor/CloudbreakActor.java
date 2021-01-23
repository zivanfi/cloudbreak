package com.sequenceiq.it.cloudbreak.actor;

import java.util.Base64;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.sequenceiq.it.TestParameter;
import com.sequenceiq.it.cloudbreak.CloudbreakTest;

@Component
public class CloudbreakActor implements Actor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudbreakActor.class);

    @Inject
    private CloudbreakUserCache cloudbreakUserCache;

    @Inject
    private TestParameter testParameter;

    @Override
    public CloudbreakUser getDefaultUser() {
        return new CloudbreakUser(testParameter.get(CloudbreakTest.ACCESS_KEY), testParameter.get(CloudbreakTest.SECRET_KEY));
    }

    @Override
    public CloudbreakUser getSecondUser() {
        String secondaryAccessKey = testParameter.get(CloudbreakTest.SECONDARY_ACCESS_KEY);
        String secondarySecretKey = testParameter.get(CloudbreakTest.SECONDARY_SECRET_KEY);
        checkNonEmpty("integrationtest.cb.secondary.accesskey", secondaryAccessKey);
        checkNonEmpty("integrationtest.cb.secondary.secretkey", secondarySecretKey);
        return new CloudbreakUser(secondaryAccessKey, secondarySecretKey);
    }

    @Override
    public CloudbreakUser createNewUser(String tenantName, String username) {
        String secretKey = testParameter.get(CloudbreakTest.SECRET_KEY);
        String crn = String.format("crn:cdp:iam:us-west-1:%s:user:%s", tenantName, username);
        String accessKey = Base64.getEncoder().encodeToString(crn.getBytes());
        return new CloudbreakUser(accessKey, secretKey, username + " at tenant " + tenantName);
    }

    @Override
    public CloudbreakUser getRealUmsUser(String key) {
        return cloudbreakUserCache.getByName(key);
    }

    public boolean realUmsActorPresent() {
        LOGGER.info("Has Cloudbreak User Cache already been initialized: {}", cloudbreakUserCache.isInitialized());
        return cloudbreakUserCache.isInitialized();
    }

    public String getUmsAdminAccessKeyByAccountId(String accountId) {
        return cloudbreakUserCache.getAdminAccessKeyByAccountId(accountId);
    }

    public String getUmsAdminAccessKeyByAccountId(String accountId, String environmentKey, String accountKey) {
        return cloudbreakUserCache.getAdminAccessKeyByAccountId(accountId, environmentKey, accountKey);
    }

    private void checkNonEmpty(String name, String value) {
        if (StringUtils.hasLength(value)) {
            throw new NullPointerException(String.format("Following variable must be set whether as environment variables or (test) application.yml: %s",
                    name.replaceAll("\\.", "_").toUpperCase()));
        }
    }
}