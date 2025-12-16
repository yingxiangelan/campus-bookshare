package com.campus.bookshare.controller;

import com.campus.bookshare.common.Result;
import com.campus.bookshare.dto.UpdateProfileRequest;
import com.campus.bookshare.dto.WechatLoginRequest;
import com.campus.bookshare.entity.User;
import com.campus.bookshare.service.UserService;
import com.campus.bookshare.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户Controller
 * 融合版：包含微信真实登录 + 原有接口
 */
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 【核心】微信登录接口
     * 前端调用: POST /api/user/login/wechat
     */
    @PostMapping("/login/wechat")
    public Result<?> loginWechat(@RequestBody WechatLoginRequest request) {
        try {
            if (request.getCode() == null || request.getCode().isEmpty()) {
                return Result.error("code不能为空");
            }
            
            String token = userService.wechatLogin(request.getCode());
            return Result.success(token);
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 【核心】更新用户信息（头像、昵称）
     * 前端调用: POST /api/user/updateUserInfo
     */
    @PostMapping("/updateUserInfo")
    public Result<?> updateUserInfo(@RequestBody UpdateProfileRequest request, 
                                    HttpServletRequest httpRequest) {
        // 1. 从请求头获取Token
        String token = httpRequest.getHeader("Authorization");
        
        // 2. 解析Token获取用户ID
        Long userId = JwtUtils.getUserId(token);
        if (userId == null) {
            return Result.error(401, "未登录或Token已过期");
        }
        
        // 3. 更新用户信息
        userService.updateProfile(userId, request.getAvatarUrl(), request.getNickName());
        
        return Result.success("更新成功");
    }

    /**
     * 获取当前登录用户信息
     * 前端调用: GET /api/user/info
     */
    @GetMapping("/info")
    public Result<?> getUserInfo(HttpServletRequest request) {
        // 1. 从请求头获取Token
        String token = request.getHeader("Authorization");
        
        // 2. 解析Token获取用户ID
        Long userId = JwtUtils.getUserId(token);
        if (userId == null) {
            return Result.error(401, "未登录或Token已过期");
        }
        
        // 3. 查询用户信息
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 4. 构建返回数据（过滤敏感信息）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("nickName", user.getNickName());
        userInfo.put("avatarUrl", user.getAvatarUrl());
        userInfo.put("phone", user.getPhone());
        userInfo.put("studentId", user.getStudentId());
        userInfo.put("school", user.getSchool());
        userInfo.put("major", user.getMajor());
        userInfo.put("campus", user.getCampus());
        userInfo.put("certified", user.getCertified() == 1);
        
        return Result.success(userInfo);
    }

    /**
     * 更新用户资料（通用接口）
     * 前端调用: PUT /api/user/profile
     */
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody UpdateProfileRequest request,
                                   HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        Long userId = JwtUtils.getUserId(token);
        
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        userService.updateProfile(userId, request.getAvatarUrl(), request.getNickName());
        return Result.success("更新成功");
    }

    /**
     * 学生认证
     * 前端调用: POST /api/user/certify
     */
    @PostMapping("/certify")
    public Result<?> certify(@RequestBody Map<String, String> data,
                            HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        Long userId = JwtUtils.getUserId(token);
        
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        String studentId = data.get("studentId");
        if (studentId == null || studentId.isEmpty()) {
            return Result.error("学号不能为空");
        }
        
        boolean success = userService.certifyStudent(userId, studentId);
        if (success) {
            return Result.success("认证成功");
        } else {
            return Result.error("认证失败，请检查学号格式");
        }
    }

    /**
     * 【兼容】旧版登录接口（已废弃，重定向到微信登录）
     */
    @PostMapping("/login")
    public Result<?> loginOld(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        if (code != null && !code.isEmpty()) {
            // 兼容旧接口，内部调用微信登录
            try {
                String token = userService.wechatLogin(code);
                
                // 获取用户信息
                Long userId = JwtUtils.getUserId(token);
                User user = userService.getUserById(userId);
                
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("nickName", user.getNickName());
                userInfo.put("avatar", user.getAvatarUrl());
                userInfo.put("certified", user.getCertified() == 1);
                data.put("userInfo", userInfo);
                
                return Result.success(data);
            } catch (Exception e) {
                return Result.error("登录失败: " + e.getMessage());
            }
        }
        return Result.error("请提供code参数");
    }
}
