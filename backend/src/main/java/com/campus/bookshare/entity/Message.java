package com.campus.bookshare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 消息实体类
 * 用于存储用户之间的聊天消息
 */
@Data
@TableName("message")
public class Message {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 发送者用户ID
     */
    private Long fromUserId;
    
    /**
     * 接收者用户ID
     */
    private Long toUserId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型 0-文本 1-图片 2-系统消息
     */
    private Integer type;
    
    /**
     * 是否已读 0-未读 1-已读
     */
    private Integer isRead;
    
    /**
     * 关联商品ID（可选）
     */
    private Long relatedGoodsId;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 默认构造函数
     */
    public Message() {
        this.type = 0;
        this.isRead = 0;
        this.createTime = new Date();
    }
}
