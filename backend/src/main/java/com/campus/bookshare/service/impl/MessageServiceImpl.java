package com.campus.bookshare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.bookshare.entity.Message;
import com.campus.bookshare.mapper.MessageMapper;
import com.campus.bookshare.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 消息服务实现类
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    
    @Override
    public Message sendMessage(Long fromUserId, Long toUserId, String content, Integer type) {
        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setType(type != null ? type : 0);
        message.setIsRead(0);
        message.setCreateTime(new Date());
        
        baseMapper.insert(message);
        return message;
    }
    
    @Override
    public List<Map<String, Object>> getConversationList(Long userId) {
        return baseMapper.selectConversationList(userId);
    }
    
    @Override
    public List<Message> getConversation(Long userId, Long targetUserId, Integer limit) {
        return baseMapper.selectConversation(userId, targetUserId, limit);
    }
    
    @Override
    public int markAsRead(Long fromUserId, Long toUserId) {
        return baseMapper.markAsRead(fromUserId, toUserId);
    }
    
    @Override
    public int getUnreadCount(Long userId) {
        return baseMapper.countUnread(userId);
    }
}
