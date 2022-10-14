package com.fsindustry.bach.core.node.model;

import com.fsindustry.bach.core.constant.ReplicatingState;
import lombok.Data;

/**
 * store member info of on memeber in a raft group
 *
 * @author fsindustry
 */
@Data
public class GroupMember {

    private final NodeEndpoint endpoint;

    private ReplicatingState replicatingState;

    private boolean major;

    private ReplicatingState ensureReplicatingState() {
        if (replicatingState == null) {
            throw new IllegalStateException("replication state not set");
        }
        return replicatingState;
    }

    public int getNextIndex() {
        return ensureReplicatingState().getNextIndex();
    }

    public boolean advanceReplicatingState(int lastEntryIndex) {
        return ensureReplicatingState().advance(lastEntryIndex);
    }

    public boolean idEquals(NodeId id) {
        return endpoint.getId().equals(id);
    }

    public NodeId getId() {
        return endpoint.getId();
    }

    public int getMatchIndex() {
        return ensureReplicatingState().getMatchIndex();
    }

    public boolean backOffNextIndex() {
        return ensureReplicatingState().backOffNextIndex();
    }
}
