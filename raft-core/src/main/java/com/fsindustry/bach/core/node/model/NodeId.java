package com.fsindustry.bach.core.node.model;


import com.google.common.base.Preconditions;
import lombok.Data;

import java.io.Serializable;

/**
 * Store Node Identity
 * which flags the node uniquely.
 *
 * @author fsindustry
 */
@Data
public class NodeId implements Serializable {

    private final String value;

    public NodeId(String value) {
        Preconditions.checkNotNull(value);
        this.value = value;
    }

    public static NodeId of(String value) {
        return new NodeId(value);
    }
}
