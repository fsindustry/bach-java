package com.fsindustry.bach.core.connector.handler;

import com.fsindustry.bach.core.connector.channel.InboundChannelGroup;
import com.fsindustry.bach.core.node.model.NodeId;
import com.google.common.eventbus.EventBus;

public class MsgDispatchHandler extends AbstractHandler {

    public MsgDispatchHandler(EventBus eventBus, InboundChannelGroup inboundChannelGroup) {

    }

    public MsgDispatchHandler(EventBus eventBus, NodeId nodeId, NodeId selfNodeId) {

    }
}
