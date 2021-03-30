package com.sequenceiq.cloudbreak.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"customconfigsname", "resourcecrn"})
)
public class CustomServiceConfigs {
    @Id
    @SequenceGenerator(
            name = "custom_configs_generator",
            sequenceName = "custom_service_configs_id_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "custom_configs_generator"
    )
    @Column(
            updatable = false
    )
    private Long id;

    @Column(
            name = "customconfigsname",
            nullable = false
    )
    private String customConfigsName;

    @Column(
            name = "resourcecrn",
            nullable = false
    )
    private String resourceCrn;

    @Column(
            name = "serviceconfigs"
    )
    private String serviceConfigs;

    private Long created = System.currentTimeMillis();

    @Column(
            name = "lastmodified"
    )
    private Long lastModified;

    public CustomServiceConfigs(String customConfigsName, String resourceCrn, String serviceConfigs, Long created, Long lastModified) {
        this.customConfigsName = customConfigsName;
        this.resourceCrn = resourceCrn;
        this.serviceConfigs = serviceConfigs;
        this.created = created;
        this.lastModified = lastModified;
    }

    public CustomServiceConfigs() {
    }

    @JsonIgnore
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomConfigsName() {
        return customConfigsName;
    }

    public void setCustomConfigsName(String customConfigsName) {
        this.customConfigsName = customConfigsName;
    }

    public String getResourceCrn() {
        return resourceCrn;
    }

    public void setResourceCrn(String resourceCrn) {
        this.resourceCrn = resourceCrn;
    }

    public String getServiceConfigs() {
        return serviceConfigs;
    }

    public void setServiceConfigs(String customConfigsText) {
        this.serviceConfigs = customConfigsText;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "CustomServiceConfigs{" +
                "customConfigsName='" + customConfigsName + '\'' +
                ", resourceCrn='" + resourceCrn + '\'' +
                ", customConfigsText='" + serviceConfigs + '\'' +
                ", created=" + created +
                ", lastModified=" + lastModified +
                '}';
    }
}
