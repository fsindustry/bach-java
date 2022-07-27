package com.fsindustry.bach.core.model;

import com.fsindustry.bach.core.constant.ReplicatingState;
import lombok.Data;

/**
 * store member info of on memeber in a raft group
 *
 * @author fsindustry
 */
@Data
public class GroupMember {

    private final NodeEndpoint endpoint;

    private ReplicatingState replicatingState;


}
