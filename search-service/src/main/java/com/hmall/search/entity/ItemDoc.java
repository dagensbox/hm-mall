package com.hmall.search.entity;


import com.hmall.common.pojo.Item;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 12141
 */
@NoArgsConstructor
@Data
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

    private List<String> suggestion;

    public ItemDoc(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.image = item.getImage();
        this.category = item.getCategory();
        this.brand = item.getBrand();
        this.sold = item.getSold();
        this.commentCount = item.getCommentCount();
        this.isAD = item.getIsAD();
        List<String> list = new ArrayList<>();
        list.add(this.name);
        list.add(this.category);
        list.add(this.brand);
        this.suggestion = list;
    }
}
