package com.campus.bookshare.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.bookshare.mapper.UserMapper;
import com.campus.bookshare.model.User;
import com.campus.bookshare.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService {

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    @Autowired
    private UserMapper userMapper;

    public String wechatLogin(String code) {
        // 1. 获取 openid
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid +
                "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";

        String response = HttpUtil.get(url);
        JSONObject json = JSONUtil.parseObj(response);
        String openid = json.getStr("openid");

        if (openid == null) {
            System.out.println("微信登录失败: " + response);
            throw new RuntimeException("微信登录失败: " + json.getStr("errmsg"));
        }

        // 2. 查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 【核心修改】这里一定要写 "openid"，对应你数据库里的列名！
        queryWrapper.eq("openid", openid);
        User user = userMapper.selectOne(queryWrapper);

        // 3. 注册逻辑
        if (user == null) {
            user = new User();
            user.setOpenId(openid);
            user.setCreateTime(new Date());
            user.setNickName("微信用户");
            user.setAvatarUrl(""); // 注意 User.java 里这里映射的是 avatar 列
            userMapper.insert(user);
        }

        // 4. 生成 Token
        return JwtUtils.createToken(user.getId());
    }

    public void updateProfile(Long userId, String avatarUrl, String nickName) {
        User user = new User();
        user.setId(userId);
        user.setAvatarUrl(avatarUrl);
        user.setNickName(nickName);
        userMapper.updateById(user);
    }

    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
}