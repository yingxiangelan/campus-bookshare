package com.campus.bookshare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.bookshare.entity.Goods;
import com.campus.bookshare.entity.Order;
import com.campus.bookshare.mapper.OrderMapper;
import com.campus.bookshare.service.GoodsService;
import com.campus.bookshare.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    
    @Autowired
    private GoodsService goodsService;
    
    @Override
    @Transactional
    public Order createOrder(Long buyerId, Long goodsId, String buyerMessage, String tradeLocation) {
        // 1. 查询商品信息
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }
        
        // 2. 检查商品状态
        if (goods.getStatus() != 0) {
            throw new RuntimeException("商品已下架或已售出");
        }
        
        // 3. 检查是否买自己的商品
        if (goods.getUserId().equals(buyerId)) {
            throw new RuntimeException("不能购买自己发布的商品");
        }
        
        // 4. 检查是否有进行中的订单
        if (!canPurchase(goodsId)) {
            throw new RuntimeException("该商品有订单正在处理中");
        }
        
        // 5. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setGoodsId(goodsId);
        order.setBuyerId(buyerId);
        order.setSellerId(goods.getUserId());
        order.setPrice(goods.getPrice());
        order.setStatus(Order.STATUS_PENDING);
        order.setBuyerMessage(buyerMessage);
        order.setTradeLocation(tradeLocation);
        
        baseMapper.insert(order);
        
        // 6. 更新商品状态为"已售"（可选，也可以等订单完成后再更新）
        // goodsService.updateGoodsStatus(goodsId, 1);
        
        return order;
    }
    
    @Override
    public List<Order> getOrderList(Long userId, String type, Integer status, Integer page, Integer pageSize) {
        Integer offset = (page - 1) * pageSize;
        
        List<Order> orders = new ArrayList<>();
        
        if ("buy".equals(type)) {
            // 我买的
            orders = baseMapper.selectBuyerOrders(userId, status, offset, pageSize);
        } else if ("sell".equals(type)) {
            // 我卖的
            orders = baseMapper.selectSellerOrders(userId, status, offset, pageSize);
        } else {
            // 全部：合并买的和卖的
            List<Order> buyOrders = baseMapper.selectBuyerOrders(userId, status, null, null);
            List<Order> sellOrders = baseMapper.selectSellerOrders(userId, status, null, null);
            
            orders.addAll(buyOrders);
            orders.addAll(sellOrders);
            
            // 按创建时间排序
            orders.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
            
            // 手动分页
            int start = Math.min(offset, orders.size());
            int end = Math.min(offset + pageSize, orders.size());
            orders = orders.subList(start, end);
        }
        
        return orders;
    }
    
    @Override
    public Order getOrderDetail(Long orderId) {
        return baseMapper.selectOrderDetail(orderId);
    }
    
    @Override
    public Order getByOrderNo(String orderNo) {
        return baseMapper.selectByOrderNo(orderNo);
    }
    
    @Override
    @Transactional
    public boolean confirmOrder(Long orderId, Long sellerId) {
        Order order = baseMapper.selectById(orderId);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 验证是卖家操作
        if (!order.getSellerId().equals(sellerId)) {
            throw new RuntimeException("无权操作此订单");
        }
        
        // 检查订单状态
        if (!order.canConfirm()) {
            throw new RuntimeException("订单状态不允许确认");
        }
        
        // 更新状态
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("confirmTime", LocalDateTime.now());
        
        return baseMapper.updateStatus(orderId, Order.STATUS_CONFIRMED, updateFields) > 0;
    }
    
    @Override
    @Transactional
    public boolean finishOrder(Long orderId, Long userId) {
        Order order = baseMapper.selectById(orderId);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 验证是买家或卖家操作
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        
        // 检查订单状态
        if (!order.canFinish()) {
            throw new RuntimeException("订单状态不允许完成");
        }
        
        // 更新订单状态
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("finishTime", LocalDateTime.now());
        
        boolean success = baseMapper.updateStatus(orderId, Order.STATUS_FINISHED, updateFields) > 0;
        
        // 更新商品状态为已售
        if (success) {
            goodsService.updateGoodsStatus(order.getGoodsId(), 1);
        }
        
        return success;
    }
    
    @Override
    @Transactional
    public boolean cancelOrder(Long orderId, Long userId, String reason) {
        Order order = baseMapper.selectById(orderId);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 验证是买家或卖家操作
        boolean isBuyer = order.getBuyerId().equals(userId);
        boolean isSeller = order.getSellerId().equals(userId);
        
        if (!isBuyer && !isSeller) {
            throw new RuntimeException("无权操作此订单");
        }
        
        // 检查订单状态
        if (!order.canCancel()) {
            throw new RuntimeException("订单状态不允许取消");
        }
        
        // 更新订单状态
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("cancelTime", LocalDateTime.now());
        updateFields.put("cancelReason", reason);
        updateFields.put("cancelBy", isBuyer ? 0 : 1);
        
        return baseMapper.updateStatus(orderId, Order.STATUS_CANCELLED, updateFields) > 0;
    }
    
    @Override
    public Map<String, Object> getOrderStats(Long userId) {
        return baseMapper.countUserOrders(userId);
    }
    
    @Override
    public boolean canPurchase(Long goodsId) {
        int activeOrders = baseMapper.countActiveOrdersByGoodsId(goodsId);
        return activeOrders == 0;
    }
    
    @Override
    public String generateOrderNo() {
        // 格式：ORD + yyyyMMddHHmmss + 4位随机数
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ORD" + timestamp + random;
    }
}
