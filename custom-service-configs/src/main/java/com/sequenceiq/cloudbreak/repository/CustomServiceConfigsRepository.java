package com.sequenceiq.cloudbreak.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sequenceiq.cloudbreak.domain.CustomServiceConfigs;
import com.sequenceiq.cloudbreak.workspace.repository.EntityType;

@EntityType(entityClass = CustomServiceConfigs.class)
@Repository
public interface CustomServiceConfigsRepository extends JpaRepository<CustomServiceConfigs, Long> {

    @Query("SELECT c FROM CustomServiceConfigs c WHERE c.customConfigsName=?1")
    Optional<CustomServiceConfigs> findCustomServiceConfigsByName(String customConfigsName);

    @Query("SELECT c FROM CustomServiceConfigs c WHERE c.resourceCrn=?1")
    Optional<CustomServiceConfigs> findCustomServiceConfigsByResourceCrn(String resourceCrn);
}
