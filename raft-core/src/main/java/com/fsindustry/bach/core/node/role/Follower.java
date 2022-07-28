package com.fsindustry.bach.core.node.role;

import com.fsindustry.bach.core.node.model.NodeId;
import com.fsindustry.bach.core.schedule.ElectionTimeout;
import lombok.Getter;

public class Follower extends AbstractNodeRole {

    @Getter
    private final NodeId voteFor;

    @Getter
    private final NodeId leaderId;

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
}
