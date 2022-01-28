package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.ccm;

import com.sequenceiq.cloudbreak.reactor.api.event.StackFailureEvent;

public class CcmUpgradeFailed extends StackFailureEvent {
    public CcmUpgradeFailed(Long stackId, Exception ex) {
        super(stackId, ex);
    }
}
