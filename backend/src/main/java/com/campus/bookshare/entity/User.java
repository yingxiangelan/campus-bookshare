package com.campus.bookshare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 融合版：保留完整字段 + 正确的数据库映射
 */
@Data
@TableName("user")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 微信openid
     * 【重要】数据库列名是 openid（全小写），需要注解映射
     */
    @TableField("openid")
    private String openId;
    
    /**
     * 用户昵称
     * 数据库列名是 nick_name，MyBatis Plus 自动驼峰转换
     */
    private String nickName;
    
    /**
     * 用户头像URL
     * 【重要】数据库列名是 avatar，需要注解映射
     */
    @TableField("avatar")
    private String avatarUrl;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 学号
     */
    private String studentId;
    
    /**
     * 学校
     */
    private String school;
    
    /**
     * 专业
     */
    private String major;
    
    /**
     * 校区
     */
    private String campus;
    
    /**
     * 是否认证 0-未认证 1-已认证
     */
    private Integer certified;
    
    /**
     * 状态 0-禁用 1-正常
     */
    private Integer status;
    
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
}
