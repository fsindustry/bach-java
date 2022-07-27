package com.fsindustry.bach.core.model.role;

import com.fsindustry.bach.core.constant.RoleName;

public abstract class AbstractNodeRole {

    private final RoleName name;

    private final Integer term;

    public AbstractNodeRole(RoleName name, Integer term) {
        this.name = name;
        this.term = term;
    }

    public RoleName getName() {
        return name;
    }

    public Integer getTerm() {
        return term;
    }

    public abstract void cancelTimeoutOrTask();
}
