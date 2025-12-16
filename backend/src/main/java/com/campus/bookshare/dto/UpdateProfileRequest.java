package com.campus.bookshare.dto;

import lombok.Data;

/**
 * 更新用户信息请求对象
 */
@Data
public class UpdateProfileRequest {
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 昵称
     */
    private String nickName;
    
    /**
     * 手机号（可选）
     */
    private String phone;
    
    /**
     * 学号（可选）
     */
    private String studentId;
    
    /**
     * 专业（可选）
     */
    private String major;
    
    /**
     * 校区（可选）
     */
    private String campus;
}
