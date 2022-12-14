package com.hmall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.hmall.common.client.ItemClient;
import com.hmall.common.dto.PageDTO;
import com.hmall.common.pojo.Item;
import com.hmall.search.entity.ItemDoc;
import com.hmall.search.entity.SearchParam;
import com.hmall.search.service.SearchService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 12141
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchClient client;

    @Autowired
    ItemClient itemClient;

    @Override
    public PageDTO<ItemDoc> list(SearchParam searchParam) {
        try {
            SearchResponse<ItemDoc> searchResponse = getItemDocSearchResponse(searchParam);
            return getItemDocPageDTOByHandleResponse(searchResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SearchResponse<ItemDoc> getItemDocSearchResponse(SearchParam searchParam) throws IOException {
        Integer page = searchParam.getPage();
        Integer size = searchParam.getSize();
        String key = searchParam.getKey();
        boolean isKeyExist = !StringUtils.isEmpty(key);
        String sortBy = searchParam.getSortBy();
        Query finalQuery = getFinalQuery(searchParam);
        SearchResponse<ItemDoc> searchResponse;
        Highlight highlight = Highlight.of(builder1 -> builder1.fields("name", builder2 ->
                builder2.preTags("<font color='red'>")
                        .postTags("</font>").
                        requireFieldMatch(false)));
        if ("default".equals(sortBy)) {
            if (isKeyExist) {
                searchResponse = client.search(builder ->
                        builder.index("mall").query(finalQuery).from((page - 1) * size).size(size).highlight(highlight), ItemDoc.class);
            } else {
                searchResponse = client.search(builder ->
                        builder.index("mall").query(finalQuery).from((page - 1) * size).size(size), ItemDoc.class);
            }
        } else {
            if (isKeyExist) {
                searchResponse = client.search(builder ->
                                builder.index("mall").query(finalQuery).from((page - 1) * size).size(size)
                                        .sort(builder1 -> builder1.field(builder2 -> builder2.field(sortBy).order(SortOrder.Desc)))
                                        .highlight(highlight)
                        , ItemDoc.class);
            } else {
                searchResponse = client.search(builder ->
                                builder.index("mall").query(finalQuery).from((page - 1) * size).size(size)
                                        .sort(builder1 -> builder1.field(builder2 -> builder2.field(sortBy).order(SortOrder.Desc)))
                        , ItemDoc.class);
            }
        }
        return searchResponse;
    }

    private static PageDTO<ItemDoc> getItemDocPageDTOByHandleResponse(SearchResponse<ItemDoc> searchResponse) {
        PageDTO<ItemDoc> pageDTO = new PageDTO<>();
        ArrayList<ItemDoc> itemDocs = new ArrayList<>();
        if (searchResponse.hits().total() != null) {
            long total = searchResponse.hits().total().value();
            pageDTO.setTotal(total);
            for (Hit<ItemDoc> hit : searchResponse.hits().hits()) {
                ItemDoc source = hit.source();
                Map<String, List<String>> listMap = hit.highlight();
                if (!CollectionUtils.isEmpty(listMap)) {
                    String name = listMap.get("name").get(0);
                    source.setName(name);
                }
                itemDocs.add(source);
            }
        } else {
            pageDTO.setTotal(0L);
        }

        pageDTO.setList(itemDocs);
        return pageDTO;
    }

    @Override
    public Map<String, List<String>> filters(SearchParam searchParam) {
        try {
            Map<String, Aggregation> map = new HashMap<>(8);
            //1???????????????
            Aggregation categoryAgg = Aggregation.of(builder -> builder.terms(builder1 -> builder1.field("category").size(100)));
            map.put("category", categoryAgg);
            //2???????????????
            Aggregation brandAgg = Aggregation.of(builder -> builder.terms(builder1 -> builder1.field("brand").size(1000)));
            map.put("brand", brandAgg);

            SearchResponse<ItemDoc> response = client.search(builder -> builder.index("mall").query(getFinalQuery(searchParam)).aggregations(map).size(0), ItemDoc.class);
            Map<String, List<String>> result = new HashMap<>(4);
            for (Map.Entry<String, Aggregate> stringAggregateEntry : response.aggregations().entrySet()) {
                String key = stringAggregateEntry.getKey();
                Aggregate value = stringAggregateEntry.getValue();
                List<String> list = new ArrayList<>();
                for (StringTermsBucket bucket : value.sterms().buckets().array()) {
                    list.add(bucket.key());
                }
                result.put(key, list);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> suggestion(String key) {
        try {
            //1?????????
            SearchResponse<ItemDoc> response = client.search(builder -> builder
                            .index("mall")
                            .suggest(builder1 -> builder1.text(key)
                                    .suggesters("mySuggestion", builder2 -> builder2.completion(builder3 ->
                                            builder3.field("suggestion").skipDuplicates(true).size(10))))
                    , ItemDoc.class);
            //2???????????????
            List<CompletionSuggestOption<ItemDoc>> options = response.suggest().get("mySuggestion").get(0).completion().options();
            List<String> list = new ArrayList<>();
            for (CompletionSuggestOption<ItemDoc> option : options) {
                String text = option.text();
                list.add(text);
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addItemById(Long id) {
        try {
            //1?????????id??????item
            Item item = itemClient.getItemById(id);
            //2????????????????????????
            ItemDoc itemDoc = new ItemDoc(item);
            //3??????????????????
            client.index(builder -> builder.index("mall").id(itemDoc.getId().toString()).document(itemDoc));
        } catch (IOException e) {
            System.out.println("mq????????????????????????");
            System.out.println(e);
        }
    }

    @Override
    public void deleteItemById(Long id) {
        try {
            client.delete(builder -> builder.index("mall").id(id.toString()));
        } catch (IOException e) {
            System.out.println("es?????????????????????????????????");
        }
    }

    private static Query getFinalQuery(SearchParam searchParam) {
        String key = searchParam.getKey();
        String brand = searchParam.getBrand();
        String category = searchParam.getCategory();
        Long maxPrice = searchParam.getMaxPrice();
        Long minPrice = searchParam.getMinPrice();

        //1?????????query??????
        List<Query> queries = new ArrayList<>();
        //2???key??????
        if (!StringUtils.isEmpty(key)) {
            Query query = Query.of(builder -> builder.match(builder1 -> builder1.field("all").query(key)));
            queries.add(query);
        } else {
            Query query = Query.of(builder -> builder.matchAll(builder1 -> builder1));
            queries.add(query);
        }
        //3???brand??????
        if (!StringUtils.isEmpty(brand)) {
            Query query = Query.of(builder -> builder.term(builder1 -> builder1.field("brand").value(brand)));
            queries.add(query);
        }
        //4???category??????
        if (!StringUtils.isEmpty(category)) {
            Query query = Query.of(builder -> builder.term(builder1 -> builder1.field("category").value(category)));
            queries.add(query);
        }
        //5???????????????
        if (minPrice != null && maxPrice != null) {
            Query query = Query.of(builder -> builder.range(builder1 ->
                    builder1.field("price").lte(JsonData.of(maxPrice * 100)).gte(JsonData.of(minPrice * 100))));
            queries.add(query);
        }
        //????????????query
        Query query = Query.of(builder -> builder.bool(builder1 -> builder1.must(queries)));
        FunctionScoreQuery functionScoreQuery = FunctionScoreQuery.of(builder -> builder.query(query).functions(builder1 ->
                builder1.filter(builder2 -> builder2.term(builder3 ->
                        builder3.field("isAD").value(true))).weight(10.0)).boostMode(FunctionBoostMode.Multiply));

        Query finalQuery = Query.of(builder -> builder.functionScore(functionScoreQuery));
        return finalQuery;
    }
}
