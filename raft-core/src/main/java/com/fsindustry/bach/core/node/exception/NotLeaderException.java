package com.fsindustry.bach.core.node.exception;

import com.fsindustry.bach.core.node.model.NodeEndpoint;
import com.fsindustry.bach.core.node.role.RoleName;
import com.google.common.base.Preconditions;
import lombok.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Thrown when current node is not leader.
 */
@Value
public class NotLeaderException extends RuntimeException {

    RoleName roleName;
    NodeEndpoint leaderEndpoint;

    public NotLeaderException(@Nonnull RoleName roleName, @Nullable NodeEndpoint leaderEndpoint) {
        super("not leader");
        Preconditions.checkNotNull(roleName);
        this.roleName = roleName;
        this.leaderEndpoint = leaderEndpoint;
    }
}
