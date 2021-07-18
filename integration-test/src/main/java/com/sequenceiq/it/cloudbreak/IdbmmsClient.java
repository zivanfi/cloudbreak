package com.sequenceiq.it.cloudbreak;

import java.util.Map;
import java.util.Set;

import com.sequenceiq.cloudbreak.idbmms.GrpcIdbmmsClient;
import com.sequenceiq.cloudbreak.idbmms.config.IdbmmsConfig;
import com.sequenceiq.flow.api.FlowPublicEndpoint;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.CloudbreakTestDto;
import com.sequenceiq.it.cloudbreak.dto.idbmms.IdbmmsTestDto;
import com.sequenceiq.it.cloudbreak.exception.TestFailException;
import com.sequenceiq.it.cloudbreak.util.wait.service.WaitObject;
import com.sequenceiq.it.cloudbreak.util.wait.service.WaitService;

import io.opentracing.Tracer;

public class IdbmmsClient extends MicroserviceClient<GrpcIdbmmsClient, Void> {

    public static final String IDBMMS_CLIENT = "IDBMMS_CLIENT";

    private GrpcIdbmmsClient idbmmsClient;

    IdbmmsClient(String newId) {
        super(newId);
    }

    IdbmmsClient() {
        this(IDBMMS_CLIENT);
    }

    @Override
    public FlowPublicEndpoint flowPublicEndpoint() {
        throw new TestFailException("Flow does not support by idbmms client");
    }

    @Override
    public <T extends WaitObject> WaitService<T> waiterService() {
        throw new TestFailException("Wait service does not support by idbmms client");
    }

    @Override
    public <E extends Enum<E>, W extends WaitObject> W waitObject(CloudbreakTestDto entity, String name, Map<String, E> desiredStatuses,
            TestContext testContext) {
        throw new TestFailException("Wait object does not support by idbmms client");
    }

    @Override
    public GrpcIdbmmsClient getDefaultClient() {
        return idbmmsClient;
    }

    public static synchronized IdbmmsClient createProxyIdbmmsClient(Tracer tracer, String idbmmsHost) {
        IdbmmsClient clientEntity = new IdbmmsClient();
        clientEntity.idbmmsClient = GrpcIdbmmsClient.createClient(
                IdbmmsConfig.newManagedChannelWrapper(idbmmsHost, 8990), tracer);
        return clientEntity;
    }

    @Override
    public Set<String> supportedTestDtos() {
        return Set.of(IdbmmsTestDto.class.getSimpleName());
    }
}
