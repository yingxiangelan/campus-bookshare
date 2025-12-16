package com.campus.bookshare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.bookshare.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 微信登录
     * @param code 微信登录凭证
     * @return JWT Token
     */
    String wechatLogin(String code);
    
    /**
     * 更新用户资料
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @param nickName 昵称
     */
    void updateProfile(Long userId, String avatarUrl, String nickName);
    
    /**
     * 根据ID获取用户
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);
    
    /**
     * 根据openid获取用户
     * @param openId 微信openid
     * @return 用户信息
     */
    User getByOpenId(String openId);
    
    /**
     * 学生认证
     * @param userId 用户ID
     * @param studentId 学号
     * @return 是否成功
     */
    boolean certifyStudent(Long userId, String studentId);
}
