package com.campus.bookshare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.bookshare.entity.Order;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {
    
    /**
     * 创建订单
     * 
     * @param buyerId 买家ID
     * @param goodsId 商品ID
     * @param buyerMessage 买家留言（可选）
     * @param tradeLocation 交易地点（可选）
     * @return 创建的订单
     */
    Order createOrder(Long buyerId, Long goodsId, String buyerMessage, String tradeLocation);
    
    /**
     * 获取订单列表
     * 
     * @param userId 用户ID
     * @param type 类型：buy-我买的, sell-我卖的, all-全部
     * @param status 状态筛选（可选）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    List<Order> getOrderList(Long userId, String type, Integer status, Integer page, Integer pageSize);
    
    /**
     * 获取订单详情
     * 
     * @param orderId 订单ID
     * @return 订单详情（包含关联信息）
     */
    Order getOrderDetail(Long orderId);
    
    /**
     * 根据订单号获取订单
     * 
     * @param orderNo 订单号
     * @return 订单信息
     */
    Order getByOrderNo(String orderNo);
    
    /**
     * 卖家确认订单
     * 
     * @param orderId 订单ID
     * @param sellerId 卖家ID（用于权限验证）
     * @return 是否成功
     */
    boolean confirmOrder(Long orderId, Long sellerId);
    
    /**
     * 完成订单
     * 
     * @param orderId 订单ID
     * @param userId 操作用户ID（买家或卖家）
     * @return 是否成功
     */
    boolean finishOrder(Long orderId, Long userId);
    
    /**
     * 取消订单
     * 
     * @param orderId 订单ID
     * @param userId 操作用户ID
     * @param reason 取消原因（可选）
     * @return 是否成功
     */
    boolean cancelOrder(Long orderId, Long userId, String reason);
    
    /**
     * 获取用户订单统计
     * 
     * @param userId 用户ID
     * @return 统计数据（buyCount, sellCount, pendingCount, finishedCount）
     */
    Map<String, Object> getOrderStats(Long userId);
    
    /**
     * 检查商品是否可以购买（没有进行中的订单）
     * 
     * @param goodsId 商品ID
     * @return 是否可以购买
     */
    boolean canPurchase(Long goodsId);
    
    /**
     * 生成订单号
     * 
     * @return 唯一订单号
     */
    String generateOrderNo();
}
