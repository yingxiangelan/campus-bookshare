package com.campus.bookshare.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;

import java.nio.charset.StandardCharsets;

/**
 * JWT工具类
 * 用于生成和解析用户登录Token
 */
public class JwtUtils {

    /**
     * JWT密钥（生产环境应该放在配置文件中）
     */
    private static final byte[] KEY = "campus_bookshare_jwt_secret_key_2024".getBytes(StandardCharsets.UTF_8);
    
    /**
     * Token有效期（天）
     */
    private static final int EXPIRE_DAYS = 7;

    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @return Token字符串
     */
    public static String createToken(Long userId) {
        DateTime now = DateTime.now();
        DateTime expireTime = now.offsetNew(DateField.DAY_OF_YEAR, EXPIRE_DAYS);

        return JWT.create()
                .setPayload("uid", userId)
                .setIssuedAt(now)
                .setExpiresAt(expireTime)
                .setKey(KEY)
                .sign();
    }

    /**
     * 从Token中获取用户ID
     * @param token Token字符串
     * @return 用户ID，解析失败返回null
     */
    public static Long getUserId(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return null;
            }
            
            // 去除可能的 "Bearer " 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // 解析Token
            JWT jwt = JWTUtil.parseToken(token);
            
            // 验证Token是否有效
            if (!jwt.setKey(KEY).verify()) {
                return null;
            }
            
            // 检查是否过期
            if (!jwt.validate(0)) {
                return null;
            }
            
            // 获取用户ID
            Object uid = jwt.getPayload("uid");
            if (uid != null) {
                return Long.valueOf(uid.toString());
            }
            return null;
        } catch (Exception e) {
            System.err.println("JWT解析失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 验证Token是否有效
     * @param token Token字符串
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        return getUserId(token) != null;
    }
}
