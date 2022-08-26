package com.hmall.search.web;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.hmall.common.dto.PageDTO;
import com.hmall.search.entity.ItemDoc;
import com.hmall.search.entity.SearchParam;
import com.hmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author 12141
 */
@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private ElasticsearchClient client;

    @Autowired
    private SearchService searchService;

    @PostMapping("/list")
    public PageDTO<ItemDoc> list(@RequestBody SearchParam searchParam){
        return searchService.list(searchParam);
    }

    @PostMapping("/filters")
    public Map<String, List<String>> filters(@RequestBody SearchParam searchParam){
        return searchService.filters(searchParam);
    }
}
