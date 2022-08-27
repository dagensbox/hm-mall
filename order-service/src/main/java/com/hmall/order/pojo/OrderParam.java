package com.hmall.order.pojo;

import lombok.Data;

/**
 * @author 12141
 */
@Data
public class OrderParam {
    private Integer num;
    private Integer paymentType;
    private Long addressId;
    private Long itemId;
}
