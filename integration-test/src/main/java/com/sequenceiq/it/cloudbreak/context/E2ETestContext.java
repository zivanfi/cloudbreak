package com.sequenceiq.it.cloudbreak.context;

import org.springframework.context.annotation.Primary;

import com.sequenceiq.cloudbreak.common.mappable.CloudPlatform;
import com.sequenceiq.it.cloudbreak.Prototype;
import com.sequenceiq.it.cloudbreak.dto.CloudbreakTestDto;

@Prototype
@Primary
public class E2ETestContext extends TestContext {

    @Override
    public <O extends CloudbreakTestDto> O init(Class<O> clss, CloudPlatform cloudPlatform) {
        return super.init(clss, cloudPlatform);
    }

    @Override
    public void cleanupTestContext() {
        super.cleanupTestContext();
    }
}
