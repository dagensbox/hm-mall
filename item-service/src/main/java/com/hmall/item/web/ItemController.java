package com.hmall.item.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.constants.MqConstants;
import com.hmall.common.dto.PageDTO;
import com.hmall.common.pojo.Item;
import com.hmall.item.service.IItemService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private IItemService itemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 分页查询商品
     *
     * @param page 当前所在页
     * @param size 页码大小
     * @return 分页返回PageDTO
     */
    @GetMapping("/list")
    public PageDTO<Item> list(int page, int size) {
        Page<Item> page1 = new Page<>(page, size);
        itemService.page(page1);
        PageDTO<Item> pageDTO = new PageDTO<>();
        pageDTO.setTotal(page1.getTotal());
        pageDTO.setList(page1.getRecords());
        return pageDTO;
    }

    /**
     * 根据id查询商品
     *
     * @param id 商品id
     * @return 商品对象
     */
    @GetMapping("/{id}")
    public Item getItemById(@PathVariable Long id) {
        return itemService.getById(id);
    }

    /**
     * 新增商品
     *
     * @param item 商品
     */
    @PostMapping
    public void addItem(@RequestBody Item item) {
        itemService.save(item);
        rabbitTemplate.convertAndSend(MqConstants.MALL_EXCHANGE, MqConstants.MALL_INSERT_KEY, item.getId());
    }

    /**
     * 上架下架商品
     *
     * @param id     商品id
     * @param status 修改后的状态，上架1，下架2
     */
    @PutMapping("/status/{id}/{status}")
    public void updateItemStatusById(@PathVariable Long id, @PathVariable Integer status) {
        Item item = new Item();
        item.setId(id);
        item.setStatus(status);
        itemService.updateById(item);
        Map<String, Long> map = new HashMap<>();
        map.put("id", id);
        map.put("status", Long.valueOf(status));
        rabbitTemplate.convertAndSend(MqConstants.MALL_EXCHANGE, MqConstants.MALL_UPDOWN_KEY, map);
    }

    /**
     * 修改商品
     *
     * @param item 修改的商品信息
     */
    @PutMapping
    public void updateItemById(@RequestBody Item item) {
        itemService.updateById(item);
        rabbitTemplate.convertAndSend(MqConstants.MALL_EXCHANGE, MqConstants.MALL_INSERT_KEY, item.getId());
    }

    @DeleteMapping("/{id}")
    public void deleteItemById(@PathVariable Long id) {
        itemService.removeById(id);
        rabbitTemplate.convertAndSend(MqConstants.MALL_EXCHANGE, MqConstants.MALL_INSERT_KEY, id);
    }

    /**
     * 扣减库存
     *
     * @param itemId 商品id
     * @param num    数量
     */
    @PutMapping("/stock/{itemId}/{num}")
    public void deductStock(@PathVariable("itemId") Long itemId, @PathVariable("num") Integer num) {
        Item item = getItemById(itemId);
        item.setStock(item.getStock() - num);
        itemService.updateById(item);
    }

}
