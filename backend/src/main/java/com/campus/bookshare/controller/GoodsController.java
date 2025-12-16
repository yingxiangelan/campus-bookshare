package com.campus.bookshare.controller;

import com.campus.bookshare.common.Result;
import com.campus.bookshare.entity.Goods;
import com.campus.bookshare.service.GoodsService;
import com.campus.bookshare.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品Controller
 * 融合版：保留完整CRUD + 适配前端返回格式
 */
@RestController
@RequestMapping("/goods")
@CrossOrigin
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 获取商品列表
     * 前端调用: GET /api/goods/list
     */
    @GetMapping("/list")
    public Result<?> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String campus,
            @RequestParam(required = false) String major,
            @RequestParam(defaultValue = "time") String sortType) {
        
        try {
            List<Goods> goodsList = goodsService.getGoodsList(campus, major, page, pageSize);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> list = goodsList.stream().map(goods -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", goods.getId());
                item.put("bookName", goods.getBookName());
                item.put("author", goods.getAuthor());
                item.put("price", goods.getPrice());
                item.put("originalPrice", goods.getOriginalPrice());
                item.put("condition", goods.getCondition());
                item.put("campus", goods.getCampus());
                item.put("major", goods.getMajor());
                item.put("coverUrl", goods.getCoverUrl());  // 使用实体类的便捷方法
                item.put("status", goods.getStatus());
                item.put("statusText", goods.getStatusText());
                return item;
            }).collect(Collectors.toList());
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("hasMore", goodsList.size() == pageSize);
            data.put("total", list.size());
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取商品详情
     * 前端调用: GET /api/goods/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        try {
            Map<String, Object> detail = goodsService.getGoodsDetail(id);
            if (detail == null) {
                return Result.error("商品不存在或已删除");
            }
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error("获取商品详情失败: " + e.getMessage());
        }
    }

    /**
     * 发布商品
     * 前端调用: POST /api/goods/publish
     */
    @PostMapping("/publish")
    public Result<?> publish(@RequestBody Map<String, Object> data,
                            HttpServletRequest request) {
        try {
            // 优先从Token获取用户ID
            String token = request.getHeader("Authorization");
            Long userId = JwtUtils.getUserId(token);
            
            // 如果Token无效，尝试从请求体获取
            if (userId == null && data.get("userId") != null) {
                userId = Long.valueOf(data.get("userId").toString());
            }
            
            // 如果仍然没有，使用默认用户ID（仅用于测试）
            if (userId == null) {
                userId = 1L;
            }
            
            goodsService.publishGoods(userId, data);
            return Result.success("发布成功");
        } catch (Exception e) {
            return Result.error("发布失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的商品
     * 前端调用: GET /api/goods/my
     */
    @GetMapping("/my")
    public Result<?> getMyGoods(HttpServletRequest request,
                               @RequestParam(required = false) Long userId) {
        try {
            // 优先从Token获取用户ID
            String token = request.getHeader("Authorization");
            Long tokenUserId = JwtUtils.getUserId(token);
            
            if (tokenUserId != null) {
                userId = tokenUserId;
            } else if (userId == null) {
                return Result.error(401, "未登录");
            }
            
            List<Goods> goodsList = goodsService.getMyGoods(userId);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> list = goodsList.stream().map(goods -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", goods.getId());
                item.put("bookName", goods.getBookName());
                item.put("author", goods.getAuthor());
                item.put("price", goods.getPrice());
                item.put("originalPrice", goods.getOriginalPrice());
                item.put("condition", goods.getCondition());
                item.put("campus", goods.getCampus());
                item.put("major", goods.getMajor());
                item.put("coverUrl", goods.getCoverUrl());
                item.put("status", goods.getStatus());
                item.put("statusText", goods.getStatusText());
                return item;
            }).collect(Collectors.toList());
            
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取我的商品失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品状态
     * 前端调用: PUT /api/goods/{id}/status
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, 
                                 @RequestBody Map<String, Object> data) {
        try {
            Integer status = null;
            if (data.get("status") != null) {
                status = Integer.valueOf(data.get("status").toString());
            }
            
            if (status == null) {
                return Result.error("状态参数不能为空");
            }
            
            boolean success = goodsService.updateGoodsStatus(id, status);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败，商品不存在或已删除");
            }
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品
     * 前端调用: DELETE /api/goods/{id}
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        try {
            boolean success = goodsService.deleteGoods(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败，商品不存在");
            }
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
