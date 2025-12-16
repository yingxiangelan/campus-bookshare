package com.campus.bookshare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 
 * 订单状态流转：
 * 0-待确认 → 1-已确认 → 2-已完成
 *     ↓          ↓
 *   3-已取消   3-已取消
 */
@Data
@TableName("`order`")  // order是MySQL保留字，需要转义
public class Order {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单号（唯一）
     * 格式：ORD + 时间戳 + 随机数
     */
    private String orderNo;
    
    /**
     * 商品ID
     */
    private Long goodsId;
    
    /**
     * 买家用户ID
     */
    private Long buyerId;
    
    /**
     * 卖家用户ID
     */
    private Long sellerId;
    
    /**
     * 成交价格
     */
    private BigDecimal price;
    
    /**
     * 订单状态
     * 0-待确认 1-已确认 2-已完成 3-已取消
     */
    private Integer status;
    
    /**
     * 买家留言
     */
    private String buyerMessage;
    
    /**
     * 交易地点
     */
    private String tradeLocation;
    
    /**
     * 卖家确认时间
     */
    private LocalDateTime confirmTime;
    
    /**
     * 交易完成时间
     */
    private LocalDateTime finishTime;
    
    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;
    
    /**
     * 取消原因
     */
    private String cancelReason;
    
    /**
     * 取消方 0-买家 1-卖家
     */
    private Integer cancelBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    // ============ 非数据库字段，用于关联查询 ============
    
    /**
     * 商品名称（关联查询）
     */
    @TableField(exist = false)
    private String goodsName;
    
    /**
     * 商品图片（关联查询）
     */
    @TableField(exist = false)
    private String goodsImage;
    
    /**
     * 买家昵称（关联查询）
     */
    @TableField(exist = false)
    private String buyerName;
    
    /**
     * 买家头像（关联查询）
     */
    @TableField(exist = false)
    private String buyerAvatar;
    
    /**
     * 卖家昵称（关联查询）
     */
    @TableField(exist = false)
    private String sellerName;
    
    /**
     * 卖家头像（关联查询）
     */
    @TableField(exist = false)
    private String sellerAvatar;
    
    // ============ 便捷方法 ============
    
    /**
     * 获取状态文本
     */
    public String getStatusText() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待确认";
            case 1: return "已确认";
            case 2: return "已完成";
            case 3: return "已取消";
            default: return "未知";
        }
    }
    
    /**
     * 订单是否可以取消
     */
    public boolean canCancel() {
        return status != null && (status == 0 || status == 1);
    }
    
    /**
     * 订单是否可以确认
     */
    public boolean canConfirm() {
        return status != null && status == 0;
    }
    
    /**
     * 订单是否可以完成
     */
    public boolean canFinish() {
        return status != null && status == 1;
    }
    
    // ============ 订单状态常量 ============
    
    public static final int STATUS_PENDING = 0;    // 待确认
    public static final int STATUS_CONFIRMED = 1;  // 已确认
    public static final int STATUS_FINISHED = 2;   // 已完成
    public static final int STATUS_CANCELLED = 3;  // 已取消
}
