package com.hmall.search.entity;

import lombok.Data;

/**
 * @author 12141
 */
@Data
public class SearchParam {
    private Integer page;
    private Integer size;
    private String sortBy;
    private String key;
    private String brand;
    private String category;
    private Long minPrice;
    private Long maxPrice;
}
