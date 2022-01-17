package com.sequenceiq.cloudbreak.reactor.api.event.orchestration;

import java.util.Set;

import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;

public class RemoveHostsSuccess extends StackEvent {
    private final Set<String> hostGroups;

    private final Set<String> hostNames;

    public RemoveHostsSuccess(Long stackId, Set<String> hostGroups, Set<String> hostNames) {
        super(stackId);
        this.hostGroups = hostGroups;
        this.hostNames = hostNames;
    }

    public RemoveHostsSuccess(String selector, Long stackId, Set<String> hostGroups, Set<String> hostNames) {
        super(selector, stackId);
        this.hostGroups = hostGroups;
        this.hostNames = hostNames;
    }

    public Set<String> getHostGroups() {
        return hostGroups;
    }

    public Set<String> getHostNames() {
        return hostNames;
    }
}
