package com.fsindustry.batch.kvstore.server;

import com.fsindustry.batch.kvstore.Protos;
import com.fsindustry.batch.kvstore.cmd.*;
import com.fsindustry.batch.kvstore.constant.MessageConstants;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;

/**
 * 客户端响应打包
 */
public class ClientEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (msg instanceof SuccessResp) {
            this.writeMessage(MessageConstants.MSG_TYPE_SUCCESS, Protos.SuccessResp.newBuilder().build(), out);
        } else if (msg instanceof FailureResp) {
            FailureResp failure = (FailureResp) msg;
            Protos.FailureResp protoFailure = Protos.FailureResp.newBuilder().setErrorCode(failure.getErrorCode()).setMessage(failure.getMessage()).build();
            this.writeMessage(MessageConstants.MSG_TYPE_FAILURE, protoFailure, out);
        } else if (msg instanceof RedirectResp) {
            RedirectResp redirect = (RedirectResp) msg;
            Protos.RedirectResp protoRedirect = Protos.RedirectResp.newBuilder().setLeaderId(redirect.getLeaderId()).build();
            this.writeMessage(MessageConstants.MSG_TYPE_REDIRECT, protoRedirect, out);
        } else if (msg instanceof GetCommand) {
            GetCommand command = (GetCommand) msg;
            Protos.GetCommand protoGetCommand = Protos.GetCommand.newBuilder().setKey(command.getKey()).build();
            this.writeMessage(MessageConstants.MSG_TYPE_GET_COMMAND, protoGetCommand, out);
        } else if (msg instanceof GetCommandResp) {
            GetCommandResp response = (GetCommandResp) msg;
            byte[] value = response.getValue();
            Protos.GetCommandResp protoResponse = Protos.GetCommandResp.newBuilder()
                    .setFound(response.isFound())
                    .setValue(value != null ? ByteString.copyFrom(value) : ByteString.EMPTY).build();
            this.writeMessage(MessageConstants.MSG_TYPE_GET_COMMAND_RESPONSE, protoResponse, out);
        } else if (msg instanceof SetCommand) {
            SetCommand command = (SetCommand) msg;
            Protos.SetCommand protoSetCommand = Protos.SetCommand.newBuilder()
                    .setKey(command.getKey())
                    .setValue(ByteString.copyFrom(command.getValue()))
                    .build();
            this.writeMessage(MessageConstants.MSG_TYPE_SET_COMMAND, protoSetCommand, out);
        }
    }

    private void writeMessage(int messageType, MessageLite message, ByteBuf out) throws IOException {
        out.writeInt(messageType);
        byte[] bytes = message.toByteArray();
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
