package com.fsindustry.bach.core.node.model;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.*;
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
    private final NodeId selfId;

    /**
     * member mapping
     */
    private Map<NodeId, GroupMember> memberMap;

    public NodeGroup(NodeEndpoint endPoint) {
        this(Collections.singletonList(endPoint), endPoint.getId());
    }

    public NodeGroup(Collection<NodeEndpoint> endpoints, NodeId selfId) {
        this.memberMap = buildMemberMap(endpoints);
        this.selfId = selfId;
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

    /**
     * 计算提交过半的commitIndex，并提交日志
     * 计算方法：对所有node根据matchIndex排序，取中间节点的matchIndex，即为提交过半的commitIndex
     * 计算节点数排除Leader节点自身
     */
    public int getMatchIndexOfMajor() {
        List<NodeMatchIndex> matchIndices = new ArrayList<>();
        for (GroupMember member : memberMap.values()) {
            if (member.isMajor() && !member.idEquals(selfId)) {
                matchIndices.add(new NodeMatchIndex(member.getId(), member.getMatchIndex()));
            }
        }
        int count = matchIndices.size();
        if (count == 0) {
            throw new IllegalStateException("standalone or no major node");
        }
        Collections.sort(matchIndices);
        log.debug("match indices {}", matchIndices);
        return matchIndices.get(count / 2).getMatchIndex();
    }

    public Collection<GroupMember> listReplicationTarget() {
        return memberMap.values().stream()
                // 排除当前节点
                .filter(v -> !v.getEndpoint().getId().equals(this.selfId))
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
            log.error("endpoints is empty for nodeId: {}", selfId);
            throw new IllegalArgumentException("endpoints is empty.");
        }
        return map;
    }

    public int getCount() {
        // todo 排除learner
        return memberMap.size();
    }

    public GroupMember findSelf() {
        return findMember(selfId);
    }


    /**
     * Node match index.
     *
     * @see NodeGroup#getMatchIndexOfMajor()
     */
    @ToString
    private static class NodeMatchIndex implements Comparable<NodeMatchIndex> {

        private final NodeId nodeId;
        private final int matchIndex;

        NodeMatchIndex(NodeId nodeId, int matchIndex) {
            this.nodeId = nodeId;
            this.matchIndex = matchIndex;
        }

        int getMatchIndex() {
            return matchIndex;
        }

        @Override
        public int compareTo(@Nonnull NodeMatchIndex o) {
            return -Integer.compare(o.matchIndex, this.matchIndex);
        }

    }
}
