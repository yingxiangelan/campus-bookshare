package com.campus.bookshare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.bookshare.entity.Goods;

import java.util.List;
import java.util.Map;

/**
 * 商品服务接口
 */
public interface GoodsService extends IService<Goods> {
    
    /**
     * 发布商品
     */
    Goods publishGoods(Long userId, Map<String, Object> data);
    
    /**
     * 获取商品列表（分页）
     */
    List<Goods> getGoodsList(String campus, String major, Integer page, Integer pageSize);
    
    /**
     * 获取商品详情
     */
    Map<String, Object> getGoodsDetail(Long id);
    
    /**
     * 获取我的商品列表
     */
    List<Goods> getMyGoods(Long userId);
    
    /**
     * 更新商品状态
     */
    boolean updateGoodsStatus(Long id, Integer status);
    
    /**
     * 删除商品（逻辑删除）
     */
    boolean deleteGoods(Long id);
    
    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long id);
}

