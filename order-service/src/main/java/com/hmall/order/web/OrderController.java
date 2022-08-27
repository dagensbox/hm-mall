package com.hmall.order.web;

import com.hmall.common.client.ItemClient;
import com.hmall.common.client.UserClient;
import com.hmall.common.pojo.Address;
import com.hmall.common.pojo.Item;
import com.hmall.order.config.ExpirationMessagePostProcessor;
import com.hmall.order.config.RabbitMQConfig;
import com.hmall.order.interceptor.ThreadLocalUtils;
import com.hmall.order.pojo.Order;
import com.hmall.order.pojo.OrderDetail;
import com.hmall.order.pojo.OrderLogistics;
import com.hmall.order.pojo.OrderParam;
import com.hmall.order.service.IOrderDetailService;
import com.hmall.order.service.IOrderLogisticsService;
import com.hmall.order.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IOrderDetailService orderDetailService;

    @Autowired
    private IOrderLogisticsService orderLogisticsService;

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("{id}")
    public Order queryOrderById(@PathVariable("id") Long orderId) {
        return orderService.getById(orderId);
    }

    @GlobalTransactional
    @PostMapping
    public Long createOrder(@RequestBody OrderParam param) {
        // 1）根据雪花算法生成订单id
        // 2）商品微服务提供FeignClient，实现根据id查询商品的接口
        // 3）根据itemId查询商品信息
        Item item = itemClient.getItemById(param.getItemId());
        // 4）基于商品价格、购买数量计算商品总价：totalFee
        Long totalFee = item.getPrice()*param.getNum();
        // 5）封装Order对象，初识status为未支付
        Order order = new Order();
        order.setTotalFee(totalFee);
        order.setPaymentType(param.getPaymentType());
        Long currentUserId = ThreadLocalUtils.getCurrentUserId();
        System.out.println(currentUserId);
        order.setUserId(ThreadLocalUtils.getCurrentUserId());
        order.setStatus(1);
        // 6）将Order写入数据库tb_order表中
        orderService.save(order);
        Long orderId = order.getId();
        // 7）将商品信息、orderId信息封装为OrderDetail对象，写入tb_order_detail表
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setId(orderId);
        orderDetail.setOrderId(orderId);
        orderDetail.setItemId(param.getItemId());
        orderDetail.setNum(param.getNum());
        orderDetail.setName(item.getName());
        orderDetail.setPrice(item.getPrice());
        orderDetail.setSpec(item.getSpec());
        orderDetail.setImage(item.getImage());
        orderDetailService.save(orderDetail);
        // 8）将user-service的根据id查询地址接口封装为FeignClient
        // 9）根据addressId查询user-service服务，获取地址信息
        Address address = userClient.findAddressById(param.getAddressId());
        // 10）将地址封装为OrderLogistics对象，写入tb_order_logistics表
        OrderLogistics orderLogistics = new OrderLogistics();
        orderLogistics.setOrderId(orderId);
        orderLogistics.setContact(address.getContact());
        orderLogistics.setPhone(address.getMobile());
        orderLogistics.setProvince(address.getProvince());
        orderLogistics.setCity(address.getCity());
        orderLogistics.setTown(address.getTown());
        orderLogistics.setStreet(address.getStreet());
        orderLogisticsService.save(orderLogistics);
        // 11）在item-service提供减库存接口，并编写FeignClient
        // 12）调用item-service的减库存接口
        itemClient.deductStock(item.getId(), param.getNum());
        rabbitTemplate.convertAndSend(RabbitMQConfig.DELAY_QUEUE_NAME,orderId,new ExpirationMessagePostProcessor(1000L*60*30));
        log.info("发送消息到{}",RabbitMQConfig.DELAY_QUEUE_ROUTING_KEY);
        return orderId;
    }
}
