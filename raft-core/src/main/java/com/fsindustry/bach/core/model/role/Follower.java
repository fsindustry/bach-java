package com.fsindustry.bach.core.model.role;

import com.fsindustry.bach.core.constant.RoleName;
import com.fsindustry.bach.core.schedule.ElectionTimeout;
import com.fsindustry.bach.core.model.NodeId;

public class Follower extends AbstractNodeRole {

    private final NodeId voteFor;

    private final NodeId leaderId;

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
