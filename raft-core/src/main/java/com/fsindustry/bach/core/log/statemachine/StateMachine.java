package com.fsindustry.bach.core.log.statemachine;


import javax.annotation.Nonnull;

/**
 * State machine.
 */
public interface StateMachine {

    int getLastApplied();

    void applyLog(StateMachineContext context, int index, @Nonnull byte[] commandBytes, int firstLogIndex);

    void shutdown();

}
