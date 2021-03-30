package com.sequenceiq.cloudbreak.cmtemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.cloudera.api.swagger.model.ApiClusterTemplateConfig;
import com.cloudera.api.swagger.model.ApiClusterTemplateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sequenceiq.cloudbreak.domain.CustomServiceConfigs;
import com.sequenceiq.cloudbreak.service.CustomServiceConfigsService;
import com.sequenceiq.cloudbreak.template.TemplatePreparationObject;

@Component
public class CustomServiceConfigsInjectorProcessor {

    @Inject
    private CustomServiceConfigsService customConfigsService;

    public void process(CmTemplateProcessor processor, TemplatePreparationObject template) throws JsonProcessingException {
        Optional<CustomServiceConfigs> customServiceConfigsIfExists = template.getCustomServiceConfigs();
        if (customServiceConfigsIfExists.isEmpty()) {
            return;
        }
        Map<String, List<ApiClusterTemplateConfig>> serviceMappedToConfigs = customConfigsService.getCustomServiceConfigsMap(customServiceConfigsIfExists.get());
        List<ApiClusterTemplateService> services = Optional.ofNullable(processor.getTemplate().getServices()).orElse(List.of());

        // approach 1
        services.forEach(service -> processor.mergeCustomServiceConfigs(service, serviceMappedToConfigs.getOrDefault(service.getRefName(), List.of())));
        // approach 2
//        serviceMappedToConfigs.forEach((String serviceName, List<ApiClusterTemplateConfig> newConfigs) -> {
//            Optional<ApiClusterTemplateService> serviceOptional = services.stream()
//                    .filter(service -> service.getRefName().equalsIgnoreCase(serviceName))
//                    .findFirst();
//            processor.mergeCustomServiceConfigs(serviceOptional.get(), newConfigs);
//        });
    }
}
