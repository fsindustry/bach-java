package com.fsindustry.bach.core.model;

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
    private final NodeId id;

    @NonNull
    private final Address address;

    public NodeEndpoint(String id, String host, int port) {
        this(new NodeId(id), new Address(host, port));
    }
}
