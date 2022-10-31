package com.fsindustry.bach.core.connector.handler;

import com.fsindustry.bach.core.connector.msg.MsgConstants;
import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesResult;
import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesRpc;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteResult;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteRpc;
import com.fsindustry.bach.core.connector.proto.Protos;
import com.fsindustry.bach.core.node.model.NodeId;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * 响应打包
 * Object -> ByteBuf
 */
public class ResponseEncoder extends MessageToByteEncoder<Object> {


    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (msg instanceof NodeId) {
            this.writeMessage(out, MsgConstants.MSG_TYPE_NODE_ID, ((NodeId) msg).getValue().getBytes());
        } else if (msg instanceof RequestVoteRpc) {
            RequestVoteRpc rpc = (RequestVoteRpc) msg;
            Protos.RequestVoteRpc protoRpc = Protos.RequestVoteRpc.newBuilder()
                    .setTerm(rpc.getTerm())
                    .setCandidateId(rpc.getCandidateId().getValue())
                    .setLastLogIndex(rpc.getLastLogIndex())
                    .setLastLogTerm(rpc.getLastLogTerm())
                    .build();
            this.writeMessage(out, MsgConstants.MSG_TYPE_REQUEST_VOTE_RPC, protoRpc);
        } else if (msg instanceof RequestVoteResult) {
            RequestVoteResult result = (RequestVoteResult) msg;
            Protos.RequestVoteResult protoResult = Protos.RequestVoteResult.newBuilder()
                    .setTerm(result.getTerm())
                    .setVoteGranted(result.isVoteGranted())
                    .build();
            this.writeMessage(out, MsgConstants.MSG_TYPE_REQUEST_VOTE_RESULT, protoResult);
        } else if (msg instanceof AppendEntriesRpc) {
            AppendEntriesRpc rpc = (AppendEntriesRpc) msg;
            Protos.AppendEntriesRpc protoRpc = Protos.AppendEntriesRpc.newBuilder()
                    .setTerm(rpc.getTerm())
                    .setLeaderId(rpc.getLeaderId().getValue())
                    .setLeaderCommit(rpc.getLeaderCommit())
                    .setPrevLogIndex(rpc.getPrevLogIndex())
                    .setPrevLogTerm(rpc.getPrevLogTerm())
                    .addAllEntries(
                            rpc.getEntries().stream().map(e ->
                                    Protos.AppendEntriesRpc.LogEntry.newBuilder()
                                            .setType(e.getType())
                                            .setIndex(e.getIndex())
                                            .setTerm(e.getTerm())
                                            .setPayload(ByteString.copyFrom(e.getPayload()))
                                            .build()
                            ).collect(Collectors.toList())
                    ).build();
            this.writeMessage(out, MsgConstants.MSG_TYPE_APPEND_ENTRIES_RPC, protoRpc);
        } else if (msg instanceof AppendEntriesResult) {
            AppendEntriesResult result = (AppendEntriesResult) msg;
            Protos.AppendEntriesResult protoResult = Protos.AppendEntriesResult.newBuilder()
                    .setTerm(result.getTerm())
                    .setSuccess(result.isSuccess())
                    .build();
            this.writeMessage(out, MsgConstants.MSG_TYPE_APPEND_ENTRIES_RESULT, protoResult);
        }
    }

    private void writeMessage(ByteBuf out, int messageType, MessageLite message) throws IOException {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        message.writeTo(byteOutput);
        out.writeInt(messageType);
        this.writeBytes(out, byteOutput.toByteArray());
    }

    private void writeMessage(ByteBuf out, int messageType, byte[] bytes) {
        // 4 + 4 + VAR
        out.writeInt(messageType);
        this.writeBytes(out, bytes);
    }

    private void writeBytes(ByteBuf out, byte[] bytes) {
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }

}
