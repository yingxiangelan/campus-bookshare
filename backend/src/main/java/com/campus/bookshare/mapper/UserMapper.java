package com.campus.bookshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.bookshare.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // MyBatis Plus 会自动帮我们实现增删改查，不需要写代码
}