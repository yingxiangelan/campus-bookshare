package com.campus.bookshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.bookshare.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据openid查询用户
     */
    User selectByOpenId(@Param("openId") String openId);
}
