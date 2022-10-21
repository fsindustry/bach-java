package com.fsindustry.bach.core.connector.channel;

import com.fsindustry.bach.core.node.model.NodeId;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 作为服务端，管理所有客户端连接
 * 可以使用Netty自带的ChannelGroup替代
 */
@Slf4j
@ThreadSafe
public class InboundChannelGroup {

    private final List<NioRaftChannel> channels = new CopyOnWriteArrayList<>();

    public void add(NodeId remoteId, NioRaftChannel channel) {
        log.debug("channel INBOUND-{} connected", remoteId);
        channel.getDelegate().closeFuture().addListener((ChannelFutureListener) future -> {
            log.debug("channel INBOUND-{} disconnected", remoteId);
            remove(channel);
        });
    }

    private void remove(NioRaftChannel channel) {
        channels.remove(channel);
    }

    public void closeAll() {
        log.debug("close all inbound channels");
        for (NioRaftChannel channel : channels) {
            channel.close();
        }
    }

}
