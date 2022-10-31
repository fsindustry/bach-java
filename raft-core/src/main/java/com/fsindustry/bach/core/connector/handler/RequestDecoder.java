package com.fsindustry.bach.core.connector.handler;

import com.fsindustry.bach.core.connector.msg.MsgConstants;
import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesResult;
import com.fsindustry.bach.core.connector.msg.vo.AppendEntriesRpc;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteResult;
import com.fsindustry.bach.core.connector.msg.vo.RequestVoteRpc;
import com.fsindustry.bach.core.connector.proto.Protos;
import com.fsindustry.bach.core.log.entry.EntryFactory;
import com.fsindustry.bach.core.node.model.NodeId;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 请求解析
 * ByteBuf -> Object
 */
public class RequestDecoder extends ByteToMessageDecoder {

    private final EntryFactory entryFactory = new EntryFactory();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 读取字节长度，若未达到解析长度，则跳过当次处理
        int availableBytes = in.readableBytes();
        if (availableBytes < 8) return;

        in.markReaderIndex();
        // 读取消息类型 和 消息长度
        int messageType = in.readInt();
        int payloadLength = in.readInt();
        // 若消息不完整，则记录读取位置，跳过当次处理
        if (in.readableBytes() < payloadLength) {
            in.resetReaderIndex();
            return;
        }

        // 读取消息体
        byte[] payload = new byte[payloadLength];
        in.readBytes(payload);


        switch (messageType) {
            case MsgConstants.MSG_TYPE_NODE_ID:
                out.add(new NodeId(new String(payload)));
                break;
            case MsgConstants.MSG_TYPE_REQUEST_VOTE_RPC:
                Protos.RequestVoteRpc protoRVRpc = Protos.RequestVoteRpc.parseFrom(payload);
                RequestVoteRpc rpc = new RequestVoteRpc();
                rpc.setTerm(protoRVRpc.getTerm());
                rpc.setCandidateId(new NodeId(protoRVRpc.getCandidateId()));
                rpc.setLastLogIndex(protoRVRpc.getLastLogIndex());
                rpc.setLastLogTerm(protoRVRpc.getLastLogTerm());
                out.add(rpc);
                break;
            case MsgConstants.MSG_TYPE_REQUEST_VOTE_RESULT:
                Protos.RequestVoteResult protoRVResult = Protos.RequestVoteResult.parseFrom(payload);
                out.add(new RequestVoteResult(protoRVResult.getTerm(), protoRVResult.getVoteGranted()));
                break;
            case MsgConstants.MSG_TYPE_APPEND_ENTRIES_RPC:
                Protos.AppendEntriesRpc protoAERpc = Protos.AppendEntriesRpc.parseFrom(payload);
                AppendEntriesRpc aeRpc = new AppendEntriesRpc();
                aeRpc.setTerm(protoAERpc.getTerm());
                aeRpc.setLeaderId(new NodeId(protoAERpc.getLeaderId()));
                aeRpc.setLeaderCommit(protoAERpc.getLeaderCommit());
                aeRpc.setPrevLogIndex(protoAERpc.getPrevLogIndex());
                aeRpc.setPrevLogTerm(protoAERpc.getPrevLogTerm());
                aeRpc.setEntries(protoAERpc.getEntriesList().stream().map(e ->
                        entryFactory.create(e.getType(), e.getIndex(), e.getTerm(), e.getPayload().toByteArray())
                ).collect(Collectors.toList()));
                out.add(aeRpc);
                break;
            case MsgConstants.MSG_TYPE_APPEND_ENTRIES_RESULT:
                Protos.AppendEntriesResult protoAEResult = Protos.AppendEntriesResult.parseFrom(payload);
                out.add(new AppendEntriesResult(protoAEResult.getTerm(), protoAEResult.getSuccess()));
                break;
        }
    }
}
