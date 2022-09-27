package com.fsindustry.bach.core.node.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Store node information of one raft group
 */
@Slf4j
@Data
public class NodeGroup {

    /**
     * the id of current node.
     */
    private final NodeId seftId;

    /**
     * member mapping
     */
    private Map<NodeId, GroupMember> memberMap;

    public NodeGroup(NodeEndpoint endPoint) {
        this(Collections.singletonList(endPoint), endPoint.getId());
    }

    public NodeGroup(Collection<NodeEndpoint> endpoints, NodeId selfId) {
        this.memberMap = buildMemberMap(endpoints);
        this.seftId = selfId;
    }

    public GroupMember getMember(NodeId id) {
        return memberMap.get(id);
    }

    public GroupMember findMember(NodeId id) {
        GroupMember member = getMember(id);
        if (null == member) {
            throw new IllegalArgumentException("id " + id + " is not exist.");
        }
        return member;
    }

    public Collection<GroupMember> listReplicationTarget() {
        return memberMap.values().stream()
                // 排除当前节点
                .filter(v -> !v.getEndpoint().getId().equals(this.seftId))
                .collect(Collectors.toList());
    }

    public Collection<NodeEndpoint> listEndpointExceptSelf() {
        return listReplicationTarget().stream()
                .map(GroupMember::getEndpoint)
                .collect(Collectors.toList());
    }

    private Map<NodeId, GroupMember> buildMemberMap(Collection<NodeEndpoint> endpoints) {
        Map<NodeId, GroupMember> map = new HashMap<>();
        for (NodeEndpoint endpoint : endpoints) {
            map.put(endpoint.getId(), new GroupMember(endpoint));
        }

        if (map.isEmpty()) {
            log.error("endpoints is empty for nodeId: {}", seftId);
            throw new IllegalArgumentException("endpoints is empty.");
        }
        return map;
    }

    public int getCount() {
        // todo 排除learner
        return memberMap.size();
    }
}
