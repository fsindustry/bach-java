package com.fsindustry.bach.core.node.model;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * store node id and address for a node
 *
 * @author fsindustry
 */
@Value
@RequiredArgsConstructor
public class NodeEndpoint {

    @NonNull
    NodeId id;

    @NonNull
    Address address;

    public NodeEndpoint(String id, String host, int port) {
        this(new NodeId(id), new Address(host, port));
    }

}
