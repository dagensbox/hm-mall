package com.hmall.common.constants;

/**
 * @author 12141
 */
public class MqConstants {
    /**
     * 交换机
     */
    public final static String MALL_EXCHANGE = "mall.topic";
    /**
     * 新增和修改的队列
     */
    public final static String MALL_INSERT_QUEUE = "mall.insert.queue";
    /**
     * 删除的队列
     */
    public final static String MALL_DELETE_QUEUE = "mall.delete.queue";
    /**
     * 上架下架的队列
     */
    public final static String MALL_UPDOWN_QUEUE = "mall.updown.queue";
    /**
     * 新增或修改的routingKey
     */
    public final static String MALL_INSERT_KEY = "mall.insert";
    /**
     * 删除的routingKey
     */
    public final static String MALL_DELETE_KEY = "mall.delete";
    /**
     * 上架下架的routingKey
     */
    public final static String MALL_UPDOWN_KEY = "mall.updown";

}
