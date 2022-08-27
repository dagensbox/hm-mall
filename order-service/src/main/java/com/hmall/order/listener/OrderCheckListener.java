package com.hmall.order.listener;

import com.hmall.order.config.RabbitMQConfig;
import com.hmall.order.pojo.Order;
import com.hmall.order.pojo.OrderDetail;
import com.hmall.order.service.IOrderDetailService;
import com.hmall.order.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 12141
 */
@Component
@Slf4j
public class OrderCheckListener {

    @Autowired
    private IOrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE_NAME)
    public void listenDeadQueue(Long orderId) {
        log.warn("接收到订单id为{}的消息,开始处理", orderId);
        orderService.handleDelayPay(orderId);
    }
}
