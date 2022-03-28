package com.fsindustry.bach.core.model;

import lombok.Data;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Store node information of one raft group
 */
@Data
public class NodeGroup {

    private final NodeId seftId;

    private Map<NodeId, GroupMember> memberMap;

    public NodeGroup(NodeEndpoint endPoint) {
        this(Collections.singletonList(endPoint), endPoint.getId());
    }

    public NodeGroup(Collection<NodeEndpoint> endpoints, NodeId selfId) {
        this.memberMap = buildMemberMap(endpoints);
        this.seftId = selfId;
    }

    private Map<NodeId, GroupMember> buildMemberMap(Collection<NodeEndpoint> endpoints) {
        return null;
    }
}
