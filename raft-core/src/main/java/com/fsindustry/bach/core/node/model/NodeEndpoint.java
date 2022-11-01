package com.fsindustry.bach.core.node.model;

import lombok.Getter;
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
    @Getter
    NodeId id;

    @NonNull
    @Getter
    Address address;

    public NodeEndpoint(String id, String host, int port) {
        this(new NodeId(id), new Address(host, port));
    }

    public String getHost() {
        return this.address.getHost();
    }

    public int getPort() {
        return this.address.getPort();
    }
}
