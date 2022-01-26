package com.sequenceiq.datalake.service.recovery;

import com.sequenceiq.datalake.entity.SdxCluster;
import com.sequenceiq.sdx.api.model.SdxRecoverableResponse;
import com.sequenceiq.sdx.api.model.SdxRecoveryRequest;
import com.sequenceiq.sdx.api.model.SdxRecoveryResponse;

/**
 * Defines a service that can be used to recover CB deployed infrastructure.
 *
 * Services that can recovery failed infrastructure must be able to <em>validate</em> that a recovery action is appropriate and
 * be able to <em>trigger</em> the recovery.
 *
 * The recovery request should be a value object representing an HTTP response object.
 *
 */
public interface RecoveryService {
    /**
     * Starts recovery of the CB infrastructure related to the recovery request.
     *
     *
     * Trigger recovery may start a Flow or FlowChain to perform the recovery operations.
     *
     * Validation should have already been performed before calling
     *
     * @param recoveryRequest detailed information about the recovery
     * @return a response containing the identifier of the triggered recovery Flow
     */
    SdxRecoveryResponse triggerRecovery(SdxCluster sdxCluster, SdxRecoveryRequest recoveryRequest);

    /**
     * Validates that triggering a recovery is allowed.
     *
     * @return a message detailing if recovery is allowed and the reason why
     */
    SdxRecoverableResponse validateRecovery(SdxCluster sdxCluster);


    /**
     * check that can be performed before validation to ensure that the
     * cluster is not in an ambigious state between two recovery procedures
     * @return whether or not this recover service  is valid to be used
     */
    boolean canRecover(SdxCluster sdxCluster);

}