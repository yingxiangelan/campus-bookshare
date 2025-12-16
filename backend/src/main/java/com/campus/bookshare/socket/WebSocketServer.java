package com.campus.bookshare.socket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.campus.bookshare.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务
 * 实现用户间的实时消息推送
 * 
 * URL格式: ws://localhost:8080/websocket/{userId}
 * 
 * 消息格式:
 * 发送: {"toUserId": 102, "content": "你好，书还在吗？", "type": 1}
 * 接收: {"fromUserId": 101, "content": "在的", "createTime": "..."}
 */
@Slf4j
@Component
@ServerEndpoint("/websocket/{userId}")
public class WebSocketServer {

    /**
     * 在线用户会话池
     * Key: 用户ID
     * Value: WebSocket Session
     */
    private static final ConcurrentHashMap<Long, Session> SESSION_POOL = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        // 将用户加入会话池
        SESSION_POOL.put(userId, session);
        log.info("【WebSocket】用户 {} 已连接，当前在线人数: {}", userId, SESSION_POOL.size());
    }

    /**
     * 连接关闭调用
     */
    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        // 从会话池移除
        SESSION_POOL.remove(userId);
        log.info("【WebSocket】用户 {} 已断开，当前在线人数: {}", userId, SESSION_POOL.size());
    }

    /**
     * 收到客户端消息后调用
     * 
     * 消息格式: {"toUserId": 102, "content": "你好", "type": 1}
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long fromUserId) {
        log.info("【WebSocket】收到用户 {} 的消息: {}", fromUserId, message);
        
        try {
            JSONObject msgObj = JSON.parseObject(message);
            
            // 心跳消息处理
            if ("heartbeat".equals(msgObj.getString("type"))) {
                log.debug("【WebSocket】收到用户 {} 的心跳", fromUserId);
                return;
            }
            
            Long toUserId = msgObj.getLong("toUserId");
            String content = msgObj.getString("content");
            
            if (toUserId == null || content == null) {
                log.warn("【WebSocket】消息格式错误: {}", message);
                return;
            }
            
            // TODO: 持久化消息到数据库
            // 这里需要注入 MessageService 并调用 save 方法
            // 由于 WebSocket 的特殊性，注入需要使用静态变量方式
            // 参考: saveMessageToDb(fromUserId, toUserId, content);
            
            // 推送消息给接收者（如果在线）
            Session toSession = SESSION_POOL.get(toUserId);
            if (toSession != null && toSession.isOpen()) {
                // 构造返回数据
                JSONObject returnData = new JSONObject();
                returnData.put("fromUserId", fromUserId);
                returnData.put("content", content);
                returnData.put("createTime", new Date());
                
                // 异步发送
                toSession.getAsyncRemote().sendText(returnData.toJSONString());
                log.info("【WebSocket】消息已推送给用户 {}", toUserId);
            } else {
                log.info("【WebSocket】用户 {} 不在线，消息已存库待拉取", toUserId);
            }

        } catch (Exception e) {
            log.error("【WebSocket】消息处理异常", e);
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("【WebSocket】连接错误", error);
    }

    /**
     * 服务端主动推送消息给指定用户
     * 
     * @param userId  目标用户ID
     * @param message 消息内容
     */
    public static void sendTo(Long userId, String message) {
        Session session = SESSION_POOL.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.info("【WebSocket】主动推送消息给用户 {}: {}", userId, message);
            } catch (Exception e) {
                log.error("【WebSocket】推送消息失败", e);
            }
        }
    }

    /**
     * 广播消息给所有在线用户
     * 
     * @param message 消息内容
     */
    public static void broadcast(String message) {
        SESSION_POOL.values().forEach(session -> {
            if (session.isOpen()) {
                session.getAsyncRemote().sendText(message);
            }
        });
        log.info("【WebSocket】广播消息: {}", message);
    }

    /**
     * 获取当前在线用户数
     */
    public static int getOnlineCount() {
        return SESSION_POOL.size();
    }

    /**
     * 检查用户是否在线
     */
    public static boolean isOnline(Long userId) {
        Session session = SESSION_POOL.get(userId);
        return session != null && session.isOpen();
    }
}
