package com.fsindustry.bach.core.node.role;

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
