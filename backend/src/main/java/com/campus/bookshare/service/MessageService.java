package com.campus.bookshare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.bookshare.entity.Message;

import java.util.List;
import java.util.Map;

/**
 * 消息服务接口
 */
public interface MessageService extends IService<Message> {
    
    /**
     * 发送消息
     * @param fromUserId 发送者ID
     * @param toUserId 接收者ID
     * @param content 消息内容
     * @param type 消息类型
     * @return 保存的消息
     */
    Message sendMessage(Long fromUserId, Long toUserId, String content, Integer type);
    
    /**
     * 获取会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    List<Map<String, Object>> getConversationList(Long userId);
    
    /**
     * 获取与指定用户的聊天记录
     * @param userId 当前用户ID
     * @param targetUserId 对方用户ID
     * @param limit 限制数量
     * @return 消息列表
     */
    List<Message> getConversation(Long userId, Long targetUserId, Integer limit);
    
    /**
     * 标记消息为已读
     * @param fromUserId 发送者ID
     * @param toUserId 接收者ID（当前用户）
     * @return 更新数量
     */
    int markAsRead(Long fromUserId, Long toUserId);
    
    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @return 未读数量
     */
    int getUnreadCount(Long userId);
}
