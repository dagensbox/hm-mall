package com.hmall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.hmall.common.dto.PageDTO;
import com.hmall.search.entity.ItemDoc;
import com.hmall.search.entity.SearchParam;
import com.hmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 12141
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchClient client;

    @Override
    public PageDTO<ItemDoc> list(SearchParam searchParam) {
        try {
            Integer page = searchParam.getPage();
            Integer size = searchParam.getSize();
            String sortBy = searchParam.getSortBy();
            Query finalQuery = getFinalQuery(searchParam);
            SearchResponse<ItemDoc> searchResponse;
            if ("default".equals(sortBy)) {
                searchResponse = client.search(builder ->
                        builder.index("mall").query(finalQuery).from((page - 1) * size).size(size), ItemDoc.class);
            } else {
                searchResponse = client.search(builder ->
                                builder.index("mall").query(finalQuery).from((page - 1) * size).size(size)
                                        .sort(builder1 -> builder1.field(builder2 -> builder2.field(sortBy).order(SortOrder.Desc)))
                        , ItemDoc.class);
            }
            System.out.println(searchResponse);

            PageDTO<ItemDoc> pageDTO = new PageDTO<>();
            ArrayList<ItemDoc> itemDocs = new ArrayList<>();
            if (searchResponse.hits().total() != null) {
                long total = searchResponse.hits().total().value();
                pageDTO.setTotal(total);
                for (Hit<ItemDoc> hit : searchResponse.hits().hits()) {
                    itemDocs.add(hit.source());
                }
            } else {
                pageDTO.setTotal(0L);
            }

            pageDTO.setList(itemDocs);
            return pageDTO;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Query getFinalQuery(SearchParam searchParam) {
        String key = searchParam.getKey();
        String brand = searchParam.getBrand();
        String category = searchParam.getCategory();
        Long maxPrice = searchParam.getMaxPrice();
        Long minPrice = searchParam.getMinPrice();

        //1、构建query集合
        List<Query> queries = new ArrayList<>();
        //2、key条件
        if (!StringUtils.isEmpty(key)) {
            Query query = Query.of(builder -> builder.match(builder1 -> builder1.field("all").query(key)));
            queries.add(query);
        } else {
            Query query = Query.of(builder -> builder.matchAll(builder1 -> builder1));
            queries.add(query);
        }
        //3、brand条件
        if (!StringUtils.isEmpty(brand)) {
            Query query = Query.of(builder -> builder.term(builder1 -> builder1.field("brand").value(brand)));
            queries.add(query);
        }
        //4、category条件
        if (!StringUtils.isEmpty(category)) {
            Query query = Query.of(builder -> builder.term(builder1 -> builder1.field("category").value(category)));
            queries.add(query);
        }
        //5、价格条件
        if (minPrice != null && maxPrice != null) {
            Query query = Query.of(builder -> builder.range(builder1 ->
                    builder1.field("price").lte(JsonData.of(maxPrice*100)).gte(JsonData.of(minPrice*100))));
            queries.add(query);
        }
        //放入最终query
        Query query = Query.of(builder -> builder.bool(builder1 -> builder1.must(queries)));
        FunctionScoreQuery functionScoreQuery = FunctionScoreQuery.of(builder -> builder.query(query).functions(builder1 ->
                builder1.filter(builder2 -> builder2.term(builder3 ->
                        builder3.field("isAD").value(true))).weight(10.0)).boostMode(FunctionBoostMode.Multiply));

        Query finalQuery = Query.of(builder -> builder.functionScore(functionScoreQuery));
        return finalQuery;
    }
}
