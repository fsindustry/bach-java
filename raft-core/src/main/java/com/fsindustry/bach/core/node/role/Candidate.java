package com.fsindustry.bach.core.node.role;

import com.fsindustry.bach.core.node.model.NodeId;
import com.fsindustry.bach.core.schedule.ElectionTimeout;
import lombok.Getter;

public class Candidate extends AbstractNodeRole {

    /**
     * 获得票数，初始为1
     */
    @Getter
    private final int votesCount;

    /**
     * 选举超时时间
     * 若超时，则重置选举超时时间，增加term，发起新一轮选举
     */
    private final ElectionTimeout electionTimeout;

    public Candidate(Integer term, ElectionTimeout electionTimeout) {
        // 默认投票给自己
        this(term, 1, electionTimeout);
    }

    public Candidate(Integer term, int votesCount, ElectionTimeout electionTimeout) {
        super(RoleName.CANDIDATE, term);
        this.votesCount = votesCount;
        this.electionTimeout = electionTimeout;
    }


    @Override
    public void cancelTimeoutOrTask() {
        electionTimeout.cancel();
    }

    @Override
    public NodeId getLeaderId(NodeId selfId) {
        // Candidate的LeaderId不确定，故返回null
        return null;
    }
}
