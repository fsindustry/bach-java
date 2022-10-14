package com.fsindustry.bach.core.constant;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 日志复制状态
 */
@Data
public class ReplicatingState {

    @Getter
    private int nextIndex;
    @Getter
    private int matchIndex;
    @Getter
    @Setter
    private boolean replicating = false;
    @Getter
    @Setter
    private long lastReplicatedAt = 0;

    public ReplicatingState(int nextIndex) {
        this(nextIndex, 0);
    }

    public ReplicatingState(int nextIndex, int matchIndex) {
        this.nextIndex = nextIndex;
        this.matchIndex = matchIndex;
    }

    /**
     * Back off next index, in other word, decrease.
     *
     * @return true if decrease successfully, false if next index is less than or equal to {@code 1}
     */
    public boolean backOffNextIndex() {
        if (nextIndex > 1) {
            nextIndex--;
            return true;
        }
        return false;
    }

    /**
     * Advance next index and match index by last entry index.
     *
     * @param lastEntryIndex last entry index
     * @return true if advanced, false if no change
     */
    public boolean advance(int lastEntryIndex) {
        // changed
        boolean result = (matchIndex != lastEntryIndex || nextIndex != (lastEntryIndex + 1));

        // todo matchIndex 和 nextIndex 只可能相差1吗？
        matchIndex = lastEntryIndex;
        nextIndex = lastEntryIndex + 1;

        return result;
    }
}
