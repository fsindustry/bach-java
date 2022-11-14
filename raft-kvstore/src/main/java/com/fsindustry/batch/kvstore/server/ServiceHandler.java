package com.fsindustry.batch.kvstore.server;

import com.fsindustry.batch.kvstore.cmd.CommandReq;
import com.fsindustry.batch.kvstore.cmd.GetCommand;
import com.fsindustry.batch.kvstore.cmd.SetCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServiceHandler extends ChannelInboundHandlerAdapter {

    private final Service service;

    public ServiceHandler(Service service) {
        this.service = service;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof GetCommand) {
            service.get(new CommandReq<>((GetCommand) msg, ctx.channel()));
        } else if (msg instanceof SetCommand) {
            service.set(new CommandReq<>((SetCommand) msg, ctx.channel()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
