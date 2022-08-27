package com.hmall.search.service;

import com.hmall.common.dto.PageDTO;
import com.hmall.search.entity.ItemDoc;
import com.hmall.search.entity.SearchParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author 12141
 */
public interface SearchService {

    PageDTO<ItemDoc> list(SearchParam searchParam);

    Map<String, List<String>> filters(SearchParam searchParam);

    List<String> suggestion(String key);

    void addItemById(Long id);

    void deleteItemById(Long id);
}
