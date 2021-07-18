package com.sequenceiq.it.cloudbreak.request.idbmms;

import java.util.List;
import java.util.Set;

public class IdbmmsMappingRequest {

    private String environmentCrn;

    private String dataAccessRole;

    private String rangerAuditRole;

    private boolean emptyMappings;

    private List<Set<String>> mappings;

    private String accountId;

    public IdbmmsMappingRequest() {
    }

    public IdbmmsMappingRequest(String environmentCrn, String dataAccessRole, String rangerAuditRole, boolean emptyMappings, List<Set<String>> mappings,
            String accountId) {
        this.environmentCrn = environmentCrn;
        this.dataAccessRole = dataAccessRole;
        this.rangerAuditRole = rangerAuditRole;
        this.emptyMappings = emptyMappings;
        this.mappings = mappings;
        this.accountId = accountId;
    }

    public String getEnvironmentCrn() {
        return environmentCrn;
    }

    public void setEnvironmentCrn(String environmentCrn) {
        this.environmentCrn = environmentCrn;
    }

    public String getDataAccessRole() {
        return dataAccessRole;
    }

    public void setDataAccessRole(String dataAccessRole) {
        this.dataAccessRole = dataAccessRole;
    }

    public String getRangerAuditRole() {
        return rangerAuditRole;
    }

    public void setRangerAuditRole(String rangerAuditRole) {
        this.rangerAuditRole = rangerAuditRole;
    }

    public boolean getEmptyMappings() {
        return emptyMappings;
    }

    public void setEmptyMappings(boolean emptyMappings) {
        this.emptyMappings = emptyMappings;
    }

    public List<Set<String>> getMappings() {
        return mappings;
    }

    public void setMappings(List<Set<String>> mappings) {
        this.mappings = mappings;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        return "IdbmmsMappingRequest{" +
                "environmentCrn='" + environmentCrn + '\'' +
                ", dataAccessRole='" + dataAccessRole + '\'' +
                ", rangerAuditRole='" + rangerAuditRole + '\'' +
                ", emptyMappings='" + emptyMappings +
                ", mappings='" + mappings +
                ", accountId='" + accountId + '\'' +
                '}';
    }
}
