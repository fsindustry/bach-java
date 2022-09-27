package com.fsindustry.bach.core.node.role;

import com.fsindustry.bach.core.node.model.NodeId;
import com.fsindustry.bach.core.schedule.ElectionTimeout;
import lombok.Getter;
import lombok.ToString;

@ToString
public class Follower extends AbstractNodeRole {

    /**
     * 投票给谁
     */
    @Getter
    private final NodeId voteFor;

    /**
     * 当前leader是谁
     */
    @Getter
    private final NodeId leaderId;

    /**
     * 选举超时定时器
     * 若超时，则变为Candidate，发起选举
     */
    @Getter
    private final ElectionTimeout electionTimeout;

    public Follower(Integer term, NodeId voteFor, NodeId leaderId, ElectionTimeout electionTimeout) {
        super(RoleName.FOLLOWER, term);
        this.voteFor = voteFor;
        this.leaderId = leaderId;
        this.electionTimeout = electionTimeout;
    }

    @Override
    public void cancelTimeoutOrTask() {
        electionTimeout.cancel();
    }

    @Override
    public NodeId getLeaderId(NodeId selfId) {
        return leaderId;
    }
}
