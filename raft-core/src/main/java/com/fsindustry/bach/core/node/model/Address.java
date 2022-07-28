package com.fsindustry.bach.core.node.model;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor
public class Address {

    @NonNull
    String host;

    @NonNull
    Integer port;
}
