package com.sequenceiq.it.cloudbreak.action.idbmms;

import static java.lang.String.format;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.it.cloudbreak.IdbmmsClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.idbmms.IdbmmsTestDto;
import com.sequenceiq.it.cloudbreak.log.Log;

public class SetIdbmmsMappingAction implements Action<IdbmmsTestDto, IdbmmsClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetIdbmmsMappingAction.class);

    @Override
    public IdbmmsTestDto action(TestContext testContext, IdbmmsTestDto testDto, IdbmmsClient client) throws Exception {
        String accountId = testContext.getActingUserCrn().getAccountId();
        String environmentCrn = testDto.getRequest().getEnvironmentCrn();
        String dataAccessRole = testDto.getRequest().getDataAccessRole();
        String rangerAuditRole = testDto.getRequest().getRangerAuditRole();
        List<Set<String>> mappings = testDto.getRequest().getMappings();
        final String configuringMessage = format(" Configuring IDBroker Mapping for environment '%s' with data access '%s', ranger audit '%s'," +
                " mappings ['%S'] and account '%s' ... ", environmentCrn, dataAccessRole, rangerAuditRole, mappings, accountId);
        final String configuredMessage = format(" IDBroker Mapping has been configured for environment '%s' with data access '%s', ranger audit '%s'," +
                " mappings ['%S'] and account '%s' ... ", environmentCrn, dataAccessRole, rangerAuditRole, mappings, accountId);

        Log.when(LOGGER, configuringMessage);
        Log.whenJson(LOGGER, format(" Configuring IDBroker Mapping request:%n "), testDto.getRequest());
        LOGGER.info(configuringMessage);
        client.getDefaultClient().setMappingsConfig(environmentCrn, dataAccessRole, rangerAuditRole, accountId, Optional.of(""));
        LOGGER.info(configuredMessage);
        Log.when(LOGGER, configuredMessage);
        return testDto;
    }
}
