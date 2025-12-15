package com.campus.bookshare.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.bookshare.mapper.GoodsMapper;
import com.campus.bookshare.model.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    // 获取首页商品列表 (只查在售的，按时间倒序)
    public List<Goods> getIndexList() {
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0); // 0 表示在售
        wrapper.orderByDesc("create_time"); // 最新发布的在前
        return goodsMapper.selectList(wrapper);
    }

    // 获取我的发布
    public List<Goods> getMyGoods(Long userId) {
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        return goodsMapper.selectList(wrapper);
    }
}