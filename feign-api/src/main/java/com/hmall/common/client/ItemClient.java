package com.hmall.common.client;

import com.hmall.common.dto.PageDTO;
import com.hmall.common.pojo.Item;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 12141
 */
@FeignClient("itemservice")
public interface ItemClient {

    @GetMapping("/item/list")
    PageDTO<Item> list(@RequestParam("page") int page, @RequestParam("size") int size);

    @GetMapping("/item/{id}")
    Item getItemById(@PathVariable("id") Long id);

    @PutMapping("/item/stock/{itemId}/{num}")
    void deductStock(@PathVariable("itemId") Long itemId, @PathVariable("num") Integer num);
}
