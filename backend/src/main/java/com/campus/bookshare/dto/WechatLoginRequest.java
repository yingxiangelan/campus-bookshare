package com.campus.bookshare.dto;

import lombok.Data;

/**
 * 微信登录请求对象
 */
@Data
public class WechatLoginRequest {
    
    /**
     * 微信登录凭证code
     * 前端通过 wx.login() 获取
     */
    private String code;
}
