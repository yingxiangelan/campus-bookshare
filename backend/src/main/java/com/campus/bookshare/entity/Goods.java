package com.campus.bookshare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类（用户发布的二手书）
 * 融合版：保留完整字段 + 兼容前端字段名
 */
@Data
@TableName("goods")
public class Goods {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 卖家用户ID
     */
    private Long userId;
    
    /**
     * 关联的书籍ID（标准书籍库）
     */
    private Long bookId;
    
    /**
     * 书名
     */
    private String bookName;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 出版社
     */
    private String publisher;
    
    /**
     * ISBN码
     */
    private String isbn;
    
    /**
     * 原价
     */
    private BigDecimal originalPrice;
    
    /**
     * 售价
     */
    private BigDecimal price;
    
    /**
     * 新旧程度
     * 【重要】condition 是 MySQL 保留字，查询时需要用反引号
     */
    @TableField("`condition`")
    private String condition;
    
    /**
     * 校区
     */
    private String campus;
    
    /**
     * 专业
     */
    private String major;
    
    /**
     * 适用课程
     */
    private String courseName;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 商品图片（逗号分隔的URL字符串）
     */
    private String images;
    
    /**
     * 状态 0-在售 1-已售 2-已下架
     */
    private Integer status;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 收藏次数
     */
    private Integer collectCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 是否删除 0-否 1-是
     */
    @TableLogic
    private Integer isDeleted;
    
    // ============ 兼容前端的便捷方法 ============
    
    /**
     * 获取封面图URL（取第一张图片）
     * 兼容前端的 coverUrl 字段
     */
    public String getCoverUrl() {
        if (images != null && !images.isEmpty()) {
            String[] imageArray = images.split(",");
            return imageArray[0].trim();
        }
        return "https://dummyimage.com/300x400";
    }
    
    /**
     * 获取状态文本
     */
    public String getStatusText() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "在售";
            case 1: return "已售";
            case 2: return "已下架";
            default: return "未知";
        }
    }
}
