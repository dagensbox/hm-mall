package com.hmall.search.service;

import com.hmall.common.dto.PageDTO;
import com.hmall.search.entity.ItemDoc;
import com.hmall.search.entity.SearchParam;
import org.springframework.stereotype.Service;

/**
 * @author 12141
 */
public interface SearchService {

    PageDTO<ItemDoc> list(SearchParam searchParam);
}
