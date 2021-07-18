package com.sequenceiq.it.cloudbreak.dto.idbmms;

import static com.sequenceiq.it.cloudbreak.context.RunningParameter.emptyRunningParameter;

import java.util.List;
import java.util.Set;

import com.sequenceiq.it.cloudbreak.IdbmmsClient;
import com.sequenceiq.it.cloudbreak.Prototype;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.assertion.Assertion;
import com.sequenceiq.it.cloudbreak.context.RunningParameter;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.AbstractTestDto;
import com.sequenceiq.it.cloudbreak.dto.environment.EnvironmentTestDto;
import com.sequenceiq.it.cloudbreak.request.idbmms.IdbmmsMappingRequest;

@Prototype
public class IdbmmsTestDto extends AbstractTestDto<IdbmmsMappingRequest, Object, IdbmmsTestDto, IdbmmsClient> {

    private static final String IDBMMS = "IDBMMS";

    public IdbmmsTestDto(TestContext testContext) {
        super(new IdbmmsMappingRequest(), testContext);
    }

    public IdbmmsTestDto(IdbmmsMappingRequest idbmmsMappingRequest, TestContext testContext) {
        super(idbmmsMappingRequest, testContext);
    }

    public IdbmmsTestDto() {
        super(IDBMMS);
        setRequest(new IdbmmsMappingRequest());
    }

    @Override
    public IdbmmsTestDto valid() {
        return withEnvironment(getTestContext().get(EnvironmentTestDto.class).getResponse().getCrn())
                .withAccountId(getTestContext().getActingUserCrn().getAccountId())
                .getCloudProvider().idbmms(this);
    }

    public IdbmmsTestDto withEnvironment() {
        EnvironmentTestDto environment = getTestContext().given(EnvironmentTestDto.class);
        if (environment == null) {
            throw new IllegalArgumentException(String.format("Environment has not been provided for this Sdx: '%s' response!", getName()));
        }
        return withEnvironment(environment.getResponse().getCrn());
    }

    public IdbmmsTestDto withEnvironment(String environmentCrn) {
        getRequest().setEnvironmentCrn(environmentCrn);
        return this;
    }

    public IdbmmsTestDto withDataAccessRole(String dataAccessRole) {
        getRequest().setDataAccessRole(dataAccessRole);
        return this;
    }

    public IdbmmsTestDto withRangerAuditRole(String rangerAuditRole) {
        getRequest().setRangerAuditRole(rangerAuditRole);
        return this;
    }

    public IdbmmsTestDto withEmptyMappings(boolean emptyMappings) {
        getRequest().setEmptyMappings(emptyMappings);
        return this;
    }

    public IdbmmsTestDto withMappings(List<Set<String>> mappings) {
        getRequest().setMappings(mappings);
        return this;
    }

    public IdbmmsTestDto withAccountId(String accountId) {
        getRequest().setAccountId(accountId);
        return this;
    }

    @Override
    public IdbmmsTestDto when(Action<IdbmmsTestDto, IdbmmsClient> action) {
        return getTestContext().when((IdbmmsTestDto) this, IdbmmsClient.class, action, emptyRunningParameter());
    }

    @Override
    public IdbmmsTestDto then(Assertion<IdbmmsTestDto, IdbmmsClient> assertion) {
        return then(assertion, emptyRunningParameter());
    }

    @Override
    public IdbmmsTestDto then(Assertion<IdbmmsTestDto, IdbmmsClient> assertion, RunningParameter runningParameter) {
        return getTestContext().then((IdbmmsTestDto) this, IdbmmsClient.class, assertion, runningParameter);
    }
}
