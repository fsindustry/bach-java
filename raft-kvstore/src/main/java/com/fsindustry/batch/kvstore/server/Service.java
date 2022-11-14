package com.fsindustry.batch.kvstore.server;

import com.fsindustry.bach.core.log.statemachine.AbstractSingleThreadStateMachine;
import com.fsindustry.bach.core.node.Node;
import com.fsindustry.bach.core.node.role.RoleName;
import com.fsindustry.bach.core.node.role.RoleNameAndLeaderId;
import com.fsindustry.batch.kvstore.cmd.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 命令处理入口（玩具）
 */
@Slf4j
public class Service {

    private final Node node;

    public Service(Node node) {
        this.node = node;
        // 注册状态机到node实例
        this.node.registerStateMachine(new StateMachineImpl());
    }

    /**
     * 暂存处理中的命令
     * IO线程将收到请求放入该map；
     * 核心线程处理命令后读取该map，回复响应；
     * map( requestId, CommandReq )
     */
    private final ConcurrentMap<String, CommandReq<?>> pendingCommands = new ConcurrentHashMap<>();

    /**
     * 响应数据缓存
     */
    private Map<String, byte[]> map = new HashMap<>();

    private class StateMachineImpl extends AbstractSingleThreadStateMachine {

        @Override
        protected void applyCommand(@Nonnull byte[] commandBytes) {
            SetCommand command = SetCommand.fromBytes(commandBytes);
            map.put(command.getKey(), command.getValue());
            CommandReq<?> commandReq = pendingCommands.remove(command.getRequestId());
            if (commandReq != null) {
                commandReq.reply(SuccessResp.INSTANCE);
            }
        }
    }


    private RedirectResp checkLeadership() {
        RoleNameAndLeaderId state = node.getRoleNameAndLeaderId();
        if (state.getRoleName() != RoleName.LEADER) {
            return new RedirectResp(state.getLeaderId().getValue());
        }
        return null;
    }

    /**
     * set命令入口
     */
    public void set(CommandReq<SetCommand> commandReq) {
        // 若当前节点不是leader节点，则重定向到leader
        RedirectResp redirect = checkLeadership();
        if (redirect != null) {
            commandReq.reply(redirect);
            return;
        }

        // 执行set命令
        SetCommand command = commandReq.getCommand();
        log.debug("set {}", command.getKey());
        this.pendingCommands.put(command.getRequestId(), commandReq);
        commandReq.addCloseListener(() -> pendingCommands.remove(command.getRequestId()));
        this.node.appendLog(command.toBytes());
    }

    /**
     * get命令入口
     */
    public void get(CommandReq<GetCommand> commandRequest) {
        String key = commandRequest.getCommand().getKey();
        log.debug("get {}", key);
        // todo map中数据从哪里来？
        byte[] value = this.map.get(key);
        // TODO view from node state machine
        commandRequest.reply(new GetCommandResp(null != value, value));
    }
}
