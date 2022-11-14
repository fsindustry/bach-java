package com.fsindustry.batch.kvstore.cmd;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.Value;

/**
 * 封装命令请求
 */
@Value
public class CommandReq<T> {

    T command;
    Channel channel;

    public void reply(Object response) {
        this.channel.writeAndFlush(response);
    }

    public void addCloseListener(Runnable runnable) {
        // 注册客户端连接关闭回调
        this.channel.closeFuture().addListener((ChannelFutureListener) future -> runnable.run());
    }
}
