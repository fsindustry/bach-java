package com.fsindustry.bach.core.node.role;

import com.fsindustry.bach.core.node.model.NodeId;
import lombok.Value;

import javax.annotation.concurrent.Immutable;

/**
 * Role name and leader id.
 */
@Immutable
@Value
public class RoleNameAndLeaderId {
    RoleName roleName;
    NodeId leaderId;
}
