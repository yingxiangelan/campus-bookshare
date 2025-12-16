package com.campus.bookshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.bookshare.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 消息Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    
    /**
     * 获取用户的会话列表
     * 返回每个会话的最后一条消息
     */
    List<Map<String, Object>> selectConversationList(@Param("userId") Long userId);
    
    /**
     * 获取两个用户之间的消息记录
     */
    List<Message> selectConversation(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            @Param("limit") Integer limit
    );
    
    /**
     * 标记消息为已读
     */
    int markAsRead(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
    
    /**
     * 获取未读消息数量
     */
    int countUnread(@Param("userId") Long userId);
}
