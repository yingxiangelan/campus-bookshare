package com.campus.bookshare.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.bookshare.entity.User;
import com.campus.bookshare.mapper.UserMapper;
import com.campus.bookshare.service.UserService;
import com.campus.bookshare.util.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 微信小程序AppID（从配置文件读取）
     */
    @Value("${wechat.appid}")
    private String appid;

    /**
     * 微信小程序Secret（从配置文件读取）
     */
    @Value("${wechat.secret}")
    private String secret;

    @Override
    @Transactional
    public String wechatLogin(String code) {
        // 1. 调用微信API，用code换取openid
        String url = String.format(
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
            appid, secret, code
        );
        
        String response = HttpUtil.get(url);
        JSONObject json = JSONUtil.parseObj(response);
        
        // 2. 检查是否成功获取openid
        String openid = json.getStr("openid");
        if (openid == null || openid.isEmpty()) {
            String errMsg = json.getStr("errmsg", "未知错误");
            System.err.println("微信登录失败: " + response);
            throw new RuntimeException("微信登录失败: " + errMsg);
        }
        
        // 3. 查询数据库是否存在该用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openid);
        User user = baseMapper.selectOne(queryWrapper);
        
        // 4. 如果用户不存在，自动注册
        if (user == null) {
            user = new User();
            user.setOpenId(openid);
            user.setNickName("微信用户");
            user.setAvatarUrl("");
            user.setStatus(1);  // 正常状态
            user.setCertified(0);  // 未认证
            user.setCreateTime(LocalDateTime.now());
            user.setIsDeleted(0);
            baseMapper.insert(user);
            
            System.out.println("新用户注册成功，ID: " + user.getId());
        }
        
        // 5. 生成JWT Token
        String token = JwtUtils.createToken(user.getId());
        System.out.println("用户登录成功，ID: " + user.getId());
        
        return token;
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, String avatarUrl, String nickName) {
        User user = new User();
        user.setId(userId);
        
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            user.setAvatarUrl(avatarUrl);
        }
        if (nickName != null && !nickName.isEmpty()) {
            user.setNickName(nickName);
        }
        user.setUpdateTime(LocalDateTime.now());
        
        baseMapper.updateById(user);
    }

    @Override
    public User getUserById(Long userId) {
        return baseMapper.selectById(userId);
    }

    @Override
    public User getByOpenId(String openId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openId);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional
    public boolean certifyStudent(Long userId, String studentId) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        
        // 简单验证学号格式（8-12位数字）
        if (studentId == null || !studentId.matches("\\d{8,12}")) {
            return false;
        }
        
        user.setStudentId(studentId);
        user.setCertified(1);
        user.setUpdateTime(LocalDateTime.now());
        
        return baseMapper.updateById(user) > 0;
    }
}
