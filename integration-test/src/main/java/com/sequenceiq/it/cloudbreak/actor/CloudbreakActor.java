package com.sequenceiq.it.cloudbreak.actor;

import java.util.Base64;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.sequenceiq.it.TestParameter;
import com.sequenceiq.it.cloudbreak.CloudbreakTest;

@Component
public class CloudbreakActor implements Actor {

    @Inject
    private CloudbreakUserCache cloudbreakUserCache;

    private TestParameter testParameter;

    public CloudbreakActor(TestParameter testParameter) {
        this.testParameter = testParameter;
    }

    public TestParameter getTestParameter() {
        return testParameter;
    }

    public final void setTestParameter(TestParameter testParameter) {
        this.testParameter = testParameter;
    }

    @Override
    public CloudbreakUser defaultUser() {
        return new CloudbreakUser(testParameter.get(CloudbreakTest.ACCESS_KEY), testParameter.get(CloudbreakTest.SECRET_KEY));
    }

    @Override
    public CloudbreakUser secondUser() {
        String secondaryAccessKey = testParameter.get(CloudbreakTest.SECONDARY_ACCESS_KEY);
        String secondarySecretKey = testParameter.get(CloudbreakTest.SECONDARY_SECRET_KEY);
        if (StringUtils.hasLength(secondaryAccessKey)) {
            throw new IllegalStateException("Add a secondary accessKey to the test: integrationtest.cb.secondary.accesskey");
        }
        if (StringUtils.hasLength(secondarySecretKey)) {
            throw new IllegalStateException("Add a secondary secretKey to the test: integrationtest.cb.secondary.secretkey");
        }
        return new CloudbreakUser(testParameter.get(CloudbreakTest.SECONDARY_ACCESS_KEY), testParameter.get(CloudbreakTest.SECONDARY_SECRET_KEY));
    }

    @Override
    public CloudbreakUser create(String tenantName, String username) {
        String secretKey = testParameter.get(CloudbreakTest.SECRET_KEY);
        String crn = String.format("crn:cdp:iam:us-west-1:%s:user:%s", tenantName, username);
        String accessKey = Base64.getEncoder().encodeToString(crn.getBytes());
        return new CloudbreakUser(accessKey, secretKey, username + " at tenant " + tenantName);
    }

    @Override
    public CloudbreakUser useRealUmsUser(String key) {
        return cloudbreakUserCache.getByName(key);
    }
}