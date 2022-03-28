package com.fsindustry.bach.core.model;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor
public class Address {

    @NonNull
    private final String host;

    @NonNull
    private final int port;
}
