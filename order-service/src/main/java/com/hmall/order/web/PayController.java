package com.hmall.order.web;

import com.hmall.order.pojo.Order;
import com.hmall.order.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private IOrderService orderService;

    @GetMapping("/url/{userId}/{orderId}")
    public void pay(@PathVariable Long orderId, @PathVariable Long userId) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(2);
        orderService.updateById(order);
    }
}
