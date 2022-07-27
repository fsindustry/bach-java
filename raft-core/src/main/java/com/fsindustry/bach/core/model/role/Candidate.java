package com.fsindustry.bach.core.model.role;

import com.fsindustry.bach.core.constant.RoleName;
import com.fsindustry.bach.core.schedule.ElectionTimeout;

public class Candidate extends AbstractNodeRole {

    private final int votesCount;

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
}
