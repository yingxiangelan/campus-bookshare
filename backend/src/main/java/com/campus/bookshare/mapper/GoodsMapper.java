package com.campus.bookshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.bookshare.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品Mapper
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
    
    /**
     * 根据条件查询商品列表
     */
    List<Goods> selectGoodsList(
            @Param("campus") String campus,
            @Param("major") String major,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );
    
    /**
     * 根据用户ID查询商品列表
     */
    List<Goods> selectGoodsByUserId(@Param("userId") Long userId);
    
    /**
     * 增加浏览次数
     */
    void incrementViewCount(@Param("id") Long id);
    
    /**
     * 根据ID查询商品（覆盖MyBatis Plus的selectById，转义保留关键字condition）
     */
    Goods selectById(@Param("id") Long id);
    
    /**
     * 根据ID更新商品（覆盖MyBatis Plus的updateById，转义保留关键字condition）
     */
    int updateById(Goods goods);
}

