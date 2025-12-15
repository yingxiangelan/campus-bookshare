package com.campus.bookshare.model;

/**
 * 更新用户信息请求对象
 * 用于接收前端传来的头像地址和昵称
 */
public class UpdateProfileRequest {
    
    // 头像的URL地址
    private String avatarUrl;
    
    // 用户昵称
    private String nickName;

    // 无参构造方法
    public UpdateProfileRequest() {
    }

    // avatarUrl 的获取方法
    public String getAvatarUrl() {
        return avatarUrl;
    }

    // avatarUrl 的设置方法
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    // nickName 的获取方法
    public String getNickName() {
        return nickName;
    }

    // nickName 的设置方法
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}