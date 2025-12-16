package com.campus.bookshare.controller;

import com.campus.bookshare.common.Result;
import com.campus.bookshare.entity.Order;
import com.campus.bookshare.service.OrderService;
import com.campus.bookshare.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单Controller
 * 
 * 订单流程：
 * 1. 买家创建订单 → 状态：待确认
 * 2. 卖家确认订单 → 状态：已确认
 * 3. 双方线下交易后，任一方点击完成 → 状态：已完成
 * 4. 任一方可在完成前取消 → 状态：已取消
 */
@RestController
@RequestMapping("/order")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     * 前端调用: POST /api/order/create
     * 
     * 请求体:
     * {
     *   "goodsId": 1,
     *   "buyerMessage": "什么时候方便交易？",
     *   "tradeLocation": "图书馆门口"
     * }
     */
    @PostMapping("/create")
    public Result<?> create(@RequestBody Map<String, Object> data,
                           HttpServletRequest request) {
        try {
            // 获取当前用户ID
            String token = request.getHeader("Authorization");
            Long buyerId = JwtUtils.getUserId(token);
            
            if (buyerId == null) {
                return Result.error(401, "请先登录");
            }
            
            // 解析参数
            Long goodsId = Long.valueOf(data.get("goodsId").toString());
            String buyerMessage = data.get("buyerMessage") != null ? 
                    data.get("buyerMessage").toString() : null;
            String tradeLocation = data.get("tradeLocation") != null ? 
                    data.get("tradeLocation").toString() : null;
            
            // 创建订单
            Order order = orderService.createOrder(buyerId, goodsId, buyerMessage, tradeLocation);
            
            // 返回订单信息
            Map<String, Object> result = new HashMap<>();
            result.put("id", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("status", order.getStatus());
            result.put("statusText", order.getStatusText());
            
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单列表
     * 前端调用: GET /api/order/list
     * 
     * 参数:
     * - type: buy(我买的) / sell(我卖的) / all(全部)
     * - status: 0/1/2/3 (可选)
     * - page: 页码
     * - pageSize: 每页数量
     */
    @GetMapping("/list")
    public Result<?> getList(
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            Long userId = JwtUtils.getUserId(token);
            
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            
            List<Order> orders = orderService.getOrderList(userId, type, status, page, pageSize);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> list = orders.stream().map(order -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", order.getId());
                item.put("orderNo", order.getOrderNo());
                item.put("goodsId", order.getGoodsId());
                item.put("goodsName", order.getGoodsName());
                item.put("goodsImage", order.getGoodsImage());
                item.put("price", order.getPrice());
                item.put("status", order.getStatus());
                item.put("statusText", order.getStatusText());
                item.put("createTime", order.getCreateTime());
                
                // 根据查询类型返回对方信息
                if ("buy".equals(type)) {
                    item.put("sellerName", order.getSellerName());
                    item.put("sellerAvatar", order.getSellerAvatar());
                    item.put("sellerId", order.getSellerId());
                } else if ("sell".equals(type)) {
                    item.put("buyerName", order.getBuyerName());
                    item.put("buyerAvatar", order.getBuyerAvatar());
                    item.put("buyerId", order.getBuyerId());
                } else {
                    // all类型返回双方信息
                    item.put("buyerId", order.getBuyerId());
                    item.put("sellerId", order.getSellerId());
                    item.put("isBuyer", order.getBuyerId().equals(userId));
                }
                
                return item;
            }).collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("hasMore", orders.size() == pageSize);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单详情
     * 前端调用: GET /api/order/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public Result<?> getDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            Long userId = JwtUtils.getUserId(token);
            
            Order order = orderService.getOrderDetail(id);
            
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            // 验证权限（只有买家或卖家可以查看）
            if (userId != null && 
                !order.getBuyerId().equals(userId) && 
                !order.getSellerId().equals(userId)) {
                return Result.error(403, "无权查看此订单");
            }
            
            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("id", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("goodsId", order.getGoodsId());
            result.put("goodsName", order.getGoodsName());
            result.put("goodsImage", order.getGoodsImage());
            result.put("price", order.getPrice());
            result.put("status", order.getStatus());
            result.put("statusText", order.getStatusText());
            result.put("buyerMessage", order.getBuyerMessage());
            result.put("tradeLocation", order.getTradeLocation());
            
            // 买家信息
            result.put("buyerId", order.getBuyerId());
            result.put("buyerName", order.getBuyerName());
            result.put("buyerAvatar", order.getBuyerAvatar());
            
            // 卖家信息
            result.put("sellerId", order.getSellerId());
            result.put("sellerName", order.getSellerName());
            result.put("sellerAvatar", order.getSellerAvatar());
            
            // 时间信息
            result.put("createTime", order.getCreateTime());
            result.put("confirmTime", order.getConfirmTime());
            result.put("finishTime", order.getFinishTime());
            result.put("cancelTime", order.getCancelTime());
            result.put("cancelReason", order.getCancelReason());
            
            // 当前用户角色
            if (userId != null) {
                result.put("isBuyer", order.getBuyerId().equals(userId));
                result.put("isSeller", order.getSellerId().equals(userId));
            }
            
            // 可执行操作
            result.put("canConfirm", order.canConfirm());
            result.put("canFinish", order.canFinish());
            result.put("canCancel", order.canCancel());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 卖家确认订单
     * 前端调用: PUT /api/order/{id}/confirm
     */
    @PutMapping("/{id}/confirm")
    public Result<?> confirm(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            Long userId = JwtUtils.getUserId(token);
            
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            
            boolean success = orderService.confirmOrder(id, userId);
            
            if (success) {
                return Result.success("订单已确认");
            } else {
                return Result.error("确认失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("确认订单失败: " + e.getMessage());
        }
    }

    /**
     * 完成订单
     * 前端调用: PUT /api/order/{id}/finish
     */
    @PutMapping("/{id}/finish")
    public Result<?> finish(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            Long userId = JwtUtils.getUserId(token);
            
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            
            boolean success = orderService.finishOrder(id, userId);
            
            if (success) {
                return Result.success("交易完成");
            } else {
                return Result.error("操作失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("完成订单失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     * 前端调用: PUT /api/order/{id}/cancel
     * 
     * 请求体:
     * {
     *   "reason": "取消原因"
     * }
     */
    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id,
                           @RequestBody(required = false) Map<String, Object> data,
                           HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            Long userId = JwtUtils.getUserId(token);
            
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            
            String reason = null;
            if (data != null && data.get("reason") != null) {
                reason = data.get("reason").toString();
            }
            
            boolean success = orderService.cancelOrder(id, userId, reason);
            
            if (success) {
                return Result.success("订单已取消");
            } else {
                return Result.error("取消失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单统计
     * 前端调用: GET /api/order/stats
     */
    @GetMapping("/stats")
    public Result<?> getStats(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            Long userId = JwtUtils.getUserId(token);
            
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            
            Map<String, Object> stats = orderService.getOrderStats(userId);
            
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计失败: " + e.getMessage());
        }
    }

    /**
     * 检查商品是否可购买
     * 前端调用: GET /api/order/check/{goodsId}
     */
    @GetMapping("/check/{goodsId}")
    public Result<?> checkPurchase(@PathVariable Long goodsId) {
        try {
            boolean canPurchase = orderService.canPurchase(goodsId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("canPurchase", canPurchase);
            result.put("message", canPurchase ? "可以购买" : "该商品有订单正在处理中");
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("检查失败: " + e.getMessage());
        }
    }
}
