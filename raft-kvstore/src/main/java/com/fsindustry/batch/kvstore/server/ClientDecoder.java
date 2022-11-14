package com.fsindustry.batch.kvstore.server;

import com.fsindustry.batch.kvstore.Protos;
import com.fsindustry.batch.kvstore.cmd.*;
import com.fsindustry.batch.kvstore.constant.MessageConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.UUID;

public class ClientDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 8) return;

        in.markReaderIndex();
        int messageType = in.readInt();
        int payloadLength = in.readInt();
        if (in.readableBytes() < payloadLength) {
            in.resetReaderIndex();
            return;
        }

        byte[] payload = new byte[payloadLength];
        in.readBytes(payload);
        switch (messageType) {
            case MessageConstants.MSG_TYPE_SUCCESS:
                out.add(SuccessResp.INSTANCE);
                break;
            case MessageConstants.MSG_TYPE_FAILURE:
                Protos.FailureResp protoFailure = Protos.FailureResp.parseFrom(payload);
                out.add(new FailureResp(protoFailure.getErrorCode(), protoFailure.getMessage()));
                break;
            case MessageConstants.MSG_TYPE_REDIRECT:
                Protos.RedirectResp protoRedirect = Protos.RedirectResp.parseFrom(payload);
                out.add(new RedirectResp(protoRedirect.getLeaderId()));
                break;
            case MessageConstants.MSG_TYPE_GET_COMMAND:
                Protos.GetCommand protoGetCommand = Protos.GetCommand.parseFrom(payload);
                out.add(new GetCommand(protoGetCommand.getKey()));
                break;
            case MessageConstants.MSG_TYPE_GET_COMMAND_RESPONSE:
                Protos.GetCommandResp protoGetCommandResponse = Protos.GetCommandResp.parseFrom(payload);
                out.add(new GetCommandResp(protoGetCommandResponse.getFound(), protoGetCommandResponse.getValue().toByteArray()));
                break;
            case MessageConstants.MSG_TYPE_SET_COMMAND:
                Protos.SetCommand protoSetCommand = Protos.SetCommand.parseFrom(payload);
                out.add(new SetCommand(UUID.randomUUID().toString(), protoSetCommand.getKey(), protoSetCommand.getValue().toByteArray()));
                break;
            default:
                throw new IllegalStateException("unexpected message type " + messageType);
        }
    }

}
