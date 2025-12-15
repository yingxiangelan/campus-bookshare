package com.campus.bookshare.model;

/**
 * 微信登录请求对象
 * 用于接收前端传来的 code
 */
public class WechatLoginRequest {
    
    // 前端传过来的临时登录凭证
    private String code;

    // 无参构造方法
    public WechatLoginRequest() {
    }

    // code 的获取方法
    public String getCode() {
        return code;
    }

    // code 的设置方法
    public void setCode(String code) {
        this.code = code;
    }
}