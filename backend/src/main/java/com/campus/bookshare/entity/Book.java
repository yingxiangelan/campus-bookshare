package com.campus.bookshare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 书籍实体类（标准书籍库）
 */
@Data
@TableName("book")
public class Book {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * ISBN码（唯一）
     */
    private String isbn;
    
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
     * 出版日期
     */
    private LocalDate publishDate;
    
    /**
     * 定价
     */
    private BigDecimal price;
    
    /**
     * 封面URL
     */
    private String coverUrl;
    
    /**
     * 简介
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
