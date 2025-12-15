package com.campus.bookshare.controller;

import com.campus.bookshare.common.Result;
import com.campus.bookshare.model.UpdateProfileRequest;
import com.campus.bookshare.model.User;
import com.campus.bookshare.model.WechatLoginRequest;
import com.campus.bookshare.service.UserService;
import com.campus.bookshare.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户Controller
 * 修改记录：接入微信真实登录逻辑
 */
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    // 【新增】注入我们刚才写的业务逻辑层
    @Autowired
    private UserService userService;

    /**
     * 【新增】微信真实登录接口
     * 对应前端请求：POST /user/login/wechat
     */
    @PostMapping("/login/wechat")
    public Result<?> loginWechat(@RequestBody WechatLoginRequest request) {
        // 1. 调用 Service，传入 code，获取登录凭证 Token
        String token = userService.wechatLogin(request.getCode());
        
        // 2. 直接返回 Token 给前端
        return Result.success(token);
    }

    /**
     * 【新增】更新用户信息（头像、昵称）
     * 对应前端请求：POST /user/updateUserInfo
     */
    @PostMapping("/updateUserInfo")
    public Result<?> updateUserInfo(@RequestBody UpdateProfileRequest request, HttpServletRequest httpRequest) {
        // 1. 从请求头 Authorization 中获取 Token
        String token = httpRequest.getHeader("Authorization");
        
        // 2. 解析 Token 获取用户 ID
        Long userId = JwtUtils.getUserId(token);
        
        // 3. 安全校验
        if (userId == null) {
            return Result.error("未登录或Token已过期"); // 假设你的 Result 有 error 方法，如果没有请改成 Result.success("失败")
        }

        // 4. 调用 Service 更新数据库
        userService.updateProfile(userId, request.getAvatarUrl(), request.getNickName());
        
        return Result.success("更新成功");
    }

    // ========================================================================
    // 下面是原有的接口（暂时保留，防止小程序其他页面报错，后续需要逐步改成查数据库）
    // ========================================================================

    /**
     * 获取用户信息
     * (TODO: 后续需要修改为根据 Token 从数据库查询真实 User)
     */
    @GetMapping("/info")
    public Result<?> getUserInfo(HttpServletRequest request) {
        // 1. 从请求头拿到 Token
        String token = request.getHeader("Authorization");

        // 2. 解析 Token 获取 UserId
        Long userId = JwtUtils.getUserId(token);

        if (userId == null) {
            return Result.error("未登录");
        }

        // 3. 去数据库查询真实的用户信息
        User user = userService.getUserById(userId);

        if (user == null) {
            return Result.error("用户不存在");
        }

        // 4. 返回给前端
        return Result.success(user);
    }

    /**
     * 学生认证
     * (TODO: 后续需要修改为真实逻辑)
     */
    @PostMapping("/certify")
    public Result<?> certify(@RequestBody Map<String, String> data) {
        return Result.success("认证成功");
    }

    /**
     * 旧的登录接口（已废弃，被 /login/wechat 替代）
     */
    @PostMapping("/login")
    public Result<?> loginOld(@RequestBody Map<String, String> params) {
        return Result.success("请使用新的微信登录接口");
    }
}