package com.sequenceiq.it.cloudbreak.client;

import org.springframework.stereotype.Service;

import com.sequenceiq.it.cloudbreak.IdbmmsClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.action.idbmms.SetIdbmmsMappingAction;
import com.sequenceiq.it.cloudbreak.dto.idbmms.IdbmmsTestDto;

@Service
public class IdbmmsTestClient {

    public Action<IdbmmsTestDto, IdbmmsClient> configureMapping() {
        return new SetIdbmmsMappingAction();
    }
}
