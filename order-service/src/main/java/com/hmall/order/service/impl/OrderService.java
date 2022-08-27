package com.hmall.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.client.ItemClient;
import com.hmall.order.mapper.OrderMapper;
import com.hmall.order.pojo.Order;
import com.hmall.order.pojo.OrderDetail;
import com.hmall.order.service.IOrderDetailService;
import com.hmall.order.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    IOrderDetailService orderDetailService;

    @Autowired
    private ItemClient itemClient;

    @GlobalTransactional
    @Override
    public void handleDelayPay(Long orderId) {
        // 根据orderId查询订单
        Order order = this.getById(orderId);
        // 判断订单status是否为1
        if (order.getStatus() != 1) {
            // 不为1则丢弃
            return;
        }
        // 为1则继续
        // 根据orderId查询订单详情，得到商品购买数量
        OrderDetail orderDetail = orderDetailService.getById(orderId);
        Integer num = orderDetail.getNum();
        // 根据orderId修改订单status为5（取消），注意幂等判断，避免重复消息
        order.setStatus(5);
        this.updateById(order);
        // 调用item-service，根据商品id、商品数量恢复库存
        itemClient.deductStock(orderDetail.getItemId(), -1 * num);
    }
}
