package com.campus.bookshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.bookshare.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 订单Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 根据订单号查询订单
     */
    Order selectByOrderNo(@Param("orderNo") String orderNo);
    
    /**
     * 查询用户的订单列表（作为买家）
     */
    List<Order> selectBuyerOrders(
            @Param("buyerId") Long buyerId,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );
    
    /**
     * 查询用户的订单列表（作为卖家）
     */
    List<Order> selectSellerOrders(
            @Param("sellerId") Long sellerId,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );
    
    /**
     * 查询订单详情（包含关联信息）
     */
    Order selectOrderDetail(@Param("id") Long id);
    
    /**
     * 统计用户订单数量
     */
    Map<String, Object> countUserOrders(@Param("userId") Long userId);
    
    /**
     * 检查商品是否有进行中的订单
     */
    int countActiveOrdersByGoodsId(@Param("goodsId") Long goodsId);
    
    /**
     * 更新订单状态
     */
    int updateStatus(
            @Param("id") Long id,
            @Param("status") Integer status,
            @Param("updateFields") Map<String, Object> updateFields
    );
}
