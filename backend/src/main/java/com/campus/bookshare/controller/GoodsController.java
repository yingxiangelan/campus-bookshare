package com.campus.bookshare.controller;

import com.campus.bookshare.common.Result;
import com.campus.bookshare.model.Goods;
import com.campus.bookshare.service.GoodsService;
import com.campus.bookshare.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    // 【首页接口】获取所有书籍
    @GetMapping("/list")
    public Result<?> getList() {
        List<Goods> list = goodsService.getIndexList();
        return Result.success(list);
    }

    // 【我的发布接口】
    @GetMapping("/my")
    public Result<?> getMyGoods(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Long userId = JwtUtils.getUserId(token);
        if (userId == null) return Result.error("未登录");

        List<Goods> list = goodsService.getMyGoods(userId);
        return Result.success(list);
    }
}