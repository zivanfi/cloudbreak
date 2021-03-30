package com.sequenceiq.cloudbreak.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.sequenceiq.authorization.annotation.DisableCheckPermissions;
import com.sequenceiq.cloudbreak.api.endpoint.CustomServiceConfigsEndpoint;
import com.sequenceiq.cloudbreak.auth.ThreadBasedUserCrnProvider;
import com.sequenceiq.cloudbreak.domain.CustomServiceConfigs;
import com.sequenceiq.cloudbreak.service.CustomServiceConfigsService;

@Controller
public class CustomServiceConfigsController implements CustomServiceConfigsEndpoint {

    private final CustomServiceConfigsService customServiceConfigsService;

    @Autowired
    public CustomServiceConfigsController(CustomServiceConfigsService customServiceConfigsService) {
        this.customServiceConfigsService = customServiceConfigsService;
    }

    @Override
    @DisableCheckPermissions
    public List<CustomServiceConfigs> listCustomServiceConfigs() {
        return customServiceConfigsService.getAllCustomServiceConfigs();
    }

    @Override
    @DisableCheckPermissions
    public CustomServiceConfigs listCustomServiceConfigsByCrn(String crn) {
        return customServiceConfigsService.getCustomServiceConfigsByCrn(crn).get();
    }

    @Override
    @DisableCheckPermissions
    public CustomServiceConfigs listCustomServiceConfigsByName(String name) {
        return customServiceConfigsService.getCustomServiceConfigsByName(name);
    }

    @Override
    @DisableCheckPermissions
    public String addCustomServiceConfigs(CustomServiceConfigs customServiceConfigs) {
        String accountId = ThreadBasedUserCrnProvider.getAccountId();
        return customServiceConfigsService.addCustomServiceConfigs(customServiceConfigs, accountId);
    }

    @Override
    @DisableCheckPermissions
    public String updateCustomServiceConfigsByCrn(String crn, String customServiceConfigsText) {
        return customServiceConfigsService.updateCustomServiceConfigsByCrn(crn, customServiceConfigsText);
    }

    @Override
    @DisableCheckPermissions
    public String updateCustomServiceConfigsByName(String name, String customServiceConfigsText) {
        return customServiceConfigsService.updateCustomServiceConfigsByName(name, customServiceConfigsText);
    }

    @Override
    @DisableCheckPermissions
    public CustomServiceConfigs deleteCustomServiceConfigsByCrn(String crn) {
        return customServiceConfigsService.deleteCustomServiceConfigsByCrn(crn);
    }

    @Override
    @DisableCheckPermissions
    public CustomServiceConfigs deleteCustomServiceConfigsByName(String name) {
        return customServiceConfigsService.deleteCustomServiceConfigsByName(name);
    }
}
