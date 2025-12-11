package com.campus.bookshare.controller;

import com.campus.bookshare.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户Controller
 */
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        
        // 实际项目中这里会：
        // 1. 用code换取openid
        // 2. 查询或创建用户
        // 3. 生成JWT token
        
        Map<String, Object> data = new HashMap<>();
        
        // Mock token
        data.put("token", "mock_jwt_token_" + System.currentTimeMillis());
        
        // Mock 用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1L);
        userInfo.put("openid", "test_openid_1");
        userInfo.put("nickName", "张同学");
        userInfo.put("avatar", "https://dummyimage.com/150");
        userInfo.put("studentId", "20210001");
        userInfo.put("school", "某某大学");
        userInfo.put("major", "计算机科学与技术");
        userInfo.put("campus", "东校区");
        userInfo.put("certified", true);
        
        data.put("userInfo", userInfo);
        
        return Result.success(data);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result<?> getUserInfo() {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1L);
        userInfo.put("nickName", "张同学");
        userInfo.put("avatar", "https://dummyimage.com/150");
        userInfo.put("studentId", "20210001");
        userInfo.put("school", "某某大学");
        userInfo.put("major", "计算机科学与技术");
        userInfo.put("campus", "东校区");
        userInfo.put("certified", true);
        
        return Result.success(userInfo);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody Map<String, Object> data) {
        // 实际项目中这里会更新数据库
        return Result.success("更新成功");
    }

    /**
     * 学生认证
     */
    @PostMapping("/certify")
    public Result<?> certify(@RequestBody Map<String, String> data) {
        String studentId = data.get("studentId");
        
        // 实际项目中这里会：
        // 1. 验证学号格式
        // 2. 可能对接学校接口验证
        // 3. 更新用户认证状态
        
        return Result.success("认证成功");
    }
}
