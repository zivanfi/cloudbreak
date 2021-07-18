package com.sequenceiq.cloudbreak.idbmms;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import com.cloudera.thunderhead.service.idbrokermappingmanagement.IdBrokerMappingManagementGrpc;
import com.cloudera.thunderhead.service.idbrokermappingmanagement.IdBrokerMappingManagementProto;
import com.sequenceiq.cloudbreak.auth.ThreadBasedUserCrnProvider;
import com.sequenceiq.cloudbreak.grpc.altus.AltusMetadataInterceptor;
import com.sequenceiq.cloudbreak.grpc.util.GrpcUtil;
import com.sequenceiq.cloudbreak.idbmms.model.MappingsConfig;

import io.grpc.ManagedChannel;
import io.opentracing.Tracer;

/**
 * <p>
 * A simple wrapper to the GRPC IDBroker Mapping Management Service. This handles setting up
 * the appropriate context-propagating interceptors and hides some boilerplate.
 * </p>
 *
 * <p>
 * This class is meant to be used only by {@link GrpcIdbmmsClient}.
 * </p>
 */
class IdbmmsClient {

    private final ManagedChannel channel;

    private final Tracer tracer;

    IdbmmsClient(ManagedChannel channel, Tracer tracer) {
        this.channel = checkNotNull(channel);
        this.tracer = tracer;
    }

    /**
     * Wraps a call to {@code GetMappingsConfig}.
     *
     * @param requestId      the request ID for the request; must not be {@code null}
     * @param environmentCrn the environment CRN; must not be {@code null}
     * @return the mappings config; never {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    MappingsConfig getMappingsConfig(String requestId, String environmentCrn) {
        checkNotNull(requestId);
        checkNotNull(environmentCrn);
        IdBrokerMappingManagementProto.GetMappingsConfigResponse mappingsConfig = newStub(requestId).getMappingsConfig(
                IdBrokerMappingManagementProto.GetMappingsConfigRequest.newBuilder()
                        .setEnvironmentCrn(environmentCrn)
                        .build()
        );
        long mappingsVersion = mappingsConfig.getMappingsVersion();
        Map<String, String> actorMappings = mappingsConfig.getActorMappingsMap();
        Map<String, String> groupMappings = mappingsConfig.getGroupMappingsMap();
        return new MappingsConfig(mappingsVersion, actorMappings, groupMappings);
    }

    /**
     * Wraps a call to {@code DeleteMappings}.
     *
     * @param requestId      the request ID for the request; must not be {@code null}
     * @param environmentCrn the environment CRN; must not be {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    void deleteMappings(String requestId, String environmentCrn) {
        checkNotNull(requestId);
        checkNotNull(environmentCrn);
        newStub(requestId).deleteMappings(
                IdBrokerMappingManagementProto.DeleteMappingsRequest.newBuilder()
                        .setEnvironmentCrn(environmentCrn)
                        .build()
        );
    }

    /**
     * Configure a real IDBroker mappings for the given environment.
     *
     * @param requestId      the request ID for the request; must not be {@code null}
     * @param environmentCrn the environment CRN; must not be {@code null}
     * @param dataAccessRole the role services will be mapped to. The list of services (users) mapped automatically to this role is:
     *                       "hbase", "hdfs", "hive", "impala", "yarn", "dpprofiler", "zeppelin", "kudu".
     * @param baseLineRole   the role used by ranger plugin to write audit logs. The list of services (users) mapped automatically to this role is:
     *                       "kafka", "solr", "knox", "atlas"
     * @param accountId      the account ID
     * @return               SetMappingsResponse
     */
    IdBrokerMappingManagementProto.SetMappingsResponse setMappings(String requestId, String environmentCrn, String dataAccessRole, String baseLineRole,
            String accountId) {
        checkNotNull(requestId);
        checkNotNull(environmentCrn);
        checkNotNull(dataAccessRole);
        checkNotNull(baseLineRole);
        checkNotNull(accountId);

        IdBrokerMappingManagementProto.SetMappingsResponse setMappingsResponse = newStub(requestId).setMappings(
                IdBrokerMappingManagementProto.SetMappingsRequest.newBuilder()
                        .setEnvironmentNameOrCrn(environmentCrn)
                        .setDataAccessRole(dataAccessRole)
                        .setBaselineRole(baseLineRole)
                        .setAccountId(accountId)
                        .build()
        );
        return setMappingsResponse;
    }

    /**
     * Creates a new stub with the appropriate metadata injecting interceptors.
     *
     * @param requestId the request ID
     * @return the stub
     */
    private IdBrokerMappingManagementGrpc.IdBrokerMappingManagementBlockingStub newStub(String requestId) {
        checkNotNull(requestId);
        return IdBrokerMappingManagementGrpc.newBlockingStub(channel)
                .withInterceptors(GrpcUtil.getTracingInterceptor(tracer),
                        new AltusMetadataInterceptor(requestId, ThreadBasedUserCrnProvider.INTERNAL_ACTOR_CRN));
    }
}
