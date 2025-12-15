package com.campus.bookshare.utils;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    // 密钥，随便写一串复杂的字符串，不要告诉别人
    private static final byte[] KEY = "my_campus_secret_key_123456".getBytes(StandardCharsets.UTF_8);

    // 生成 Token
    public static String createToken(Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", userId);
        
        // 默认有效期 7 天
        DateTime now = DateTime.now();
        DateTime newTime = now.offsetNew(DateField.DAY_OF_YEAR, 7);

        return JWT.create()
                .setPayload("uid", userId)
                .setExpiresAt(newTime)
                .setKey(KEY)
                .sign();
    }

    // 从 Token 中获取 UserId
    public static Long getUserId(String token) {
        try {
            if (token == null || token.isEmpty()) return null;
            // 简单解析，实际项目建议校验签名
            final JWT jwt = JWTUtil.parseToken(token);
            return Long.valueOf(jwt.getPayload("uid").toString());
        } catch (Exception e) {
            return null;
        }
    }
}