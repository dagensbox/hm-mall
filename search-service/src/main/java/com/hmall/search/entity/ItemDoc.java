package com.hmall.search.entity;


/**
 * @author 12141
 */
public class ItemDoc {
    private Long id;//商品id
    private String name;//商品名称
    private Long price;//价格（分）
    private String image;//商品图片
    private String category;//分类名称
    private String brand;//品牌名称
    private Integer sold;//销量
    private Integer commentCount;//评论数
    private Boolean isAD;//商品状态 1-正常，2-下架
}
