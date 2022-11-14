package com.fsindustry.bach.core.log.statemachine;

import com.fsindustry.bach.core.executor.SingleThreadTaskExecutor;
import com.fsindustry.bach.core.executor.TaskExecutor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

@Slf4j
public abstract class AbstractSingleThreadStateMachine implements StateMachine {

    private volatile int lastApplied = 0;
    private final TaskExecutor taskExecutor;

    public AbstractSingleThreadStateMachine() {
        taskExecutor = new SingleThreadTaskExecutor("state-machine");
    }

    @Override
    public int getLastApplied() {
        return lastApplied;
    }

    @Override
    public void applyLog(StateMachineContext context, int index, @Nonnull byte[] commandBytes, int firstLogIndex) {
        taskExecutor.submit(() -> doApplyLog(context, index, commandBytes, firstLogIndex));
    }

    private void doApplyLog(StateMachineContext context, int index, @Nonnull byte[] commandBytes, int firstLogIndex) {
        if (index <= lastApplied) {
            return;
        }
        log.debug("apply log {}", index);
        applyCommand(commandBytes);
        lastApplied = index;
    }

    protected abstract void applyCommand(@Nonnull byte[] commandBytes);

    @Override
    public void shutdown() {
        try {
            taskExecutor.shutdown();
        } catch (InterruptedException e) {
            throw new StateMachineException(e);
        }
    }
}
