package com.hmall;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.hmall.common.client.ItemClient;
import com.hmall.common.dto.PageDTO;
import com.hmall.common.pojo.Item;
import com.hmall.search.SearchApplication;
import com.hmall.search.entity.ItemDoc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(classes = SearchApplication.class)
public class ImportData {

    @Autowired
    private ElasticsearchClient client;

    @Autowired
    private ItemClient itemClient;

    // 批量导入文档到es
    @Test
    void testBulk() throws IOException {
        int i = 1;
        while (true) {
            List<ItemDoc> itemDocList = new ArrayList<>();
            PageDTO<Item> items = itemClient.list(i, 200);
            if (items.getList() == null || items.getList().size() == 0) {
                System.out.println("我好了，你呢");
                break;
            }
            for (Item item : items.getList()) {
                itemDocList.add(new ItemDoc(item));
            }
            List<BulkOperation> list = itemDocList.stream().map(itemDoc ->
                    new BulkOperation.Builder().create(createOperation ->
                            createOperation.id(itemDoc.getId().toString()).document(itemDoc)).build()).collect(Collectors.toList());

            BulkResponse response = client.bulk(req -> req.index("mall").operations(list));
            System.out.println(response);
            i++;
        }
    }
}
