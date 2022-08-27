package com.hmall.search.listener;

import com.hmall.common.constants.MqConstants;
import com.hmall.search.service.SearchService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author 12141
 */
@Component
public class MqListener {

    @Autowired
    SearchService searchService;

    @RabbitListener(queues = MqConstants.MALL_INSERT_QUEUE)
    private void listenMallInsertOrUpdate(Long id) {
        searchService.addItemById(id);
    }

    @RabbitListener(queues = MqConstants.MALL_DELETE_QUEUE)
    private void listenMallDelete(Long id) {
        searchService.deleteItemById(id);
    }

    @RabbitListener(queues = MqConstants.MALL_UPDOWN_QUEUE)
    private void listenMallUpAndDown(Map<String, Long> map) {
        Long status = map.get("status");
        Long id = map.get("id");
        if (status == 1) {
            searchService.addItemById(id);
        } else {
            searchService.deleteItemById(id);
        }
    }

}
