package com.ben.core.connector.elasticsearch7;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ben.core.conf.JacksonConfig;
import com.ben.core.constance.ConfigKeys;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.api.common.functions.Function;
import org.apache.flink.configuration.Configuration;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author learn
 * @date 2022/4/11 17:57
 */
public class Elasticsearch7Function implements Serializable, Function {

    protected static final Logger log = LoggerFactory.getLogger(Elasticsearch7Function.class);

    private int MAX_QUERY_SIZE;
    protected RestHighLevelClient restHighLevelClient;

    public void open(Configuration parameters) {
        MAX_QUERY_SIZE = parameters.get(ConfigKeys.elasticsearch_max_query_size);

        HttpHost[] hosts = new HttpHost[]{
                new HttpHost(parameters.get(ConfigKeys.elasticsearch_host)
                        , parameters.get(ConfigKeys.elasticsearch_port)
                        , parameters.get(ConfigKeys.elasticsearch_scheme))
        };

        try {
            restHighLevelClient = new RestHighLevelClient(Elasticsearch7ClientBase.getRestClientBuilder(hosts,parameters));
        }catch (Exception e){
            e.printStackTrace();
        }
        //MAX_QUERY_SIZE = Integer.parseInt(ConfigPropertiesUtil.getConfig("elasticsearch_queryMaxSize"));
    }

    public void close() throws Exception {
        if (restHighLevelClient != null) {
            try {
                restHighLevelClient.close();
                restHighLevelClient = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断索引是否存在
     * @param indexName
     * @return
     * @throws IOException
     */
    public boolean indexExists(String indexName) throws Exception {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }
    /**
     * 判断索引是否存在
     * @param indexName
     * @return
     * @throws IOException
     */
    public boolean deleteIndex(String indexName) throws Exception {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
        return restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT).isAcknowledged();
    }
    /**
     * 根据条件删除
     * 复杂搜索
     * @param indexName
     */
    public boolean delData(String indexName, QueryBuilder boolQueryBuilder) throws Exception {
        try {
            DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
            //构建搜索条件
            request.setQuery(boolQueryBuilder);
            request.setTimeout(TimeValue.timeValueHours(1));
            BulkByScrollResponse resp = restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * 根据条件删除
     * 简单搜索
     * @param indexName
     */
    public boolean delData(String indexName, Map<String, Object> andMap
            , Map<String, Object> orMap) throws Exception {
        try {
            DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
            //构建搜索条件
            BoolQueryBuilder boolQuery = moreCondition(andMap, orMap);//构建多条件查询

            request.setQuery(boolQuery);
            request.setTimeout(TimeValue.timeValueHours(1));
            BulkByScrollResponse resp = restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
            long l = resp.getDeleted();
            System.out.println(l);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * 批量新增文档
     *
     * @param indexName
     * @param list
     */
    public boolean addData(String indexName, List<Map<String,Object>> list) throws Exception {
        if (CollectionUtils.isNotEmpty(list)) {
            BulkRequest bulkRequest = new BulkRequest();
            for (Object calculateMetaData : list) {
                IndexRequest indexRequest = new IndexRequest(indexName);
                ObjectMapper objectMapper = JacksonConfig.getMapper();
                String s = objectMapper.writeValueAsString(calculateMetaData);
                indexRequest.source(s, XContentType.JSON);
                bulkRequest.add(indexRequest);
            }
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            //是否失败 ,返回false 代表成功
            return !bulkResponse.hasFailures();
        }
        return true;
    }
    public boolean addData(String indexName, Map<String,Object> map) throws Exception {
        if (map.size() > 0) {
            BulkRequest bulkRequest = new BulkRequest();

            IndexRequest indexRequest = new IndexRequest(indexName);
            ObjectMapper objectMapper = JacksonConfig.getMapper();
            String s = objectMapper.writeValueAsString(map);
            indexRequest.source(s, XContentType.JSON);
            bulkRequest.add(indexRequest);

            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            //是否失败 ,返回false 代表成功
            if (bulkResponse.hasFailures()){
                throw new Exception(bulkResponse.buildFailureMessage());
            }
            return !bulkResponse.hasFailures();
        }
        return true;
    }

    public boolean addData(String indexName, String id,Map<String,Object> map) throws Exception {
        if (map.size() > 0) {
            BulkRequest bulkRequest = new BulkRequest();
            ObjectMapper objectMapper = JacksonConfig.getMapper();
            String s = objectMapper.writeValueAsString(map);

            IndexRequest indexRequest = new IndexRequest(indexName)
                    .id(id)
                    .source(s, XContentType.JSON);

            bulkRequest.add(indexRequest);
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            //是否失败 ,返回false 代表成功
            if (bulkResponse.hasFailures()){
                throw new Exception(bulkResponse.buildFailureMessage());
            }
            return !bulkResponse.hasFailures();
        }
        return true;
    }

    public boolean addData( BulkRequest bulkRequest) throws Exception {
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        //是否失败 ,返回false 代表成功
        return !bulkResponse.hasFailures();
    }
    /**
     * 通用查询方法(无size,默认10000)带排除字段
     * 复杂查询
     * @param indexName
     * @param includes
     * @param excludes
     * @return
     * @throws Exception
     */
    public ResultEntity search(String indexName, BoolQueryBuilder boolQueryBuilder,String[] includes, String[] excludes) throws Exception {
        SearchRequest searchRequest = new SearchRequest(indexName);
        //构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().trackTotalHits(true).size(MAX_QUERY_SIZE);
        sourceBuilder.fetchSource(includes,excludes);
        if(!Objects.isNull(boolQueryBuilder))
            sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.timeout(TimeValue.timeValueHours(1));
        searchRequest.source(sourceBuilder);
        return manageResult(searchRequest);
    }
    public List<String> searchId(String indexName, BoolQueryBuilder boolQueryBuilder) throws Exception {
        SearchRequest searchRequest = new SearchRequest(indexName);
        //构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().trackTotalHits(true).size(MAX_QUERY_SIZE);
        sourceBuilder.fetchSource("", null);
        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.timeout(TimeValue.timeValueHours(1));
        searchRequest.source(sourceBuilder);

        // 设置scroll参数
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        searchRequest.scroll(scroll);

        List<String> result = new ArrayList<>();
        // 执行第一次查询，获取scrollId
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] hits = searchResponse.getHits().getHits();
        while (hits != null && hits.length > 0) {
            // 处理当前文档列表
            for (SearchHit hit : hits) {
                result.add(hit.getId());
            }

            // 构造下一次查询时的SearchScrollRequest
            searchResponse = restHighLevelClient.scroll(new SearchScrollRequest(scrollId).scroll(scroll), RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            hits = searchResponse.getHits().getHits();

        }
        // 清理滚动上下文
        clearScroll(scrollId);

        return result;
    }
    /**
     * 通用查询方法(无size,默认10000)
     * 简单搜索,仅精准查询
     * @param indexName 索引名称
     * @param andMap    精确查询参数map  参数and连接
     * @param orMap     精确查询参数map  参数or连接
     * @return 查询结果
     * @throws Exception
     */
    public ResultEntity search(String indexName, Map<String, Object> andMap
            , Map<String, Object> orMap) throws Exception {
        SearchRequest searchRequest = new SearchRequest(indexName);
        //构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().trackTotalHits(true).size(MAX_QUERY_SIZE);
        BoolQueryBuilder boolQuery = moreCondition(andMap, orMap);//构建多条件查询
        sourceBuilder.query(boolQuery);
        sourceBuilder.timeout(TimeValue.timeValueHours(1));
        searchRequest.source(sourceBuilder);
        return manageResult(searchRequest);
    }

    /**
     * 通用查询方法(无size,默认10000)带排除字段
     * 简单搜索,仅精准查询
     * @param indexName
     * @param andMap
     * @param orMap
     * @param includes
     * @param excludes
     * @return
     * @throws Exception
     */
    public ResultEntity search(String indexName, Map<String, Object> andMap
            , Map<String, Object> orMap,String[] includes, String[] excludes) throws Exception {
        SearchRequest searchRequest = new SearchRequest(indexName);
        //构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().trackTotalHits(true).size(MAX_QUERY_SIZE);
        sourceBuilder.fetchSource(includes,excludes);
        BoolQueryBuilder boolQuery = moreCondition(andMap, orMap);//构建多条件查询
        sourceBuilder.query(boolQuery);
        sourceBuilder.timeout(TimeValue.timeValueHours(1));
        searchRequest.source(sourceBuilder);
        return manageResult(searchRequest);
    }

    public ResultEntity search(String indexName, String conditionField ,long... conditionFieldValue) throws Exception {
        SearchRequest searchRequest = new SearchRequest(indexName);
        //构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termsQuery(conditionField,conditionFieldValue)).size(MAX_QUERY_SIZE);
        searchRequest.source(sourceBuilder);
        return manageResult(searchRequest);
    }

    public void dslJsonSearch(String indexName,String dsl) throws IOException {
        WrapperQueryBuilder wqb = new WrapperQueryBuilder(dsl);
        SearchSourceBuilder ssb = new SearchSourceBuilder();

        SearchRequest request = new SearchRequest(indexName);
        request.source(ssb);
        ssb.query(wqb);
        restHighLevelClient.search(request, RequestOptions.DEFAULT);
    }
    /**
     * 处理结果方法
     *
     * @param searchRequest 搜索请求
     * @return map
     * @throws IOException 操作异常
     */
    public ResultEntity manageResult(SearchRequest searchRequest) throws IOException {
        List<Map<String, Object>> result = new ArrayList<>();
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        for (SearchHit hit : hits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            sourceAsMap.put("_id",hit.getId());
            result.add(sourceAsMap);
        }
        int total = (int) hits.getTotalHits().value; //总数
        ResultEntity re = new ResultEntity();
        re.setData(result);
        re.setTotal(total);
        return re;
    }

    /**
     * 构建多条件查询
     *
     * @param andMap 精确查询参数map  参数and连接
     * @param orMap  精确查询参数map  参数or连接
     * @return 多条件构造器
     */
    public BoolQueryBuilder moreCondition(Map<String, Object> andMap
            , Map<String, Object> orMap) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //多条件设置  参数and连接
        if (!CollUtil.isEmpty(andMap)) {
            for (Map.Entry<String, Object> entry : andMap.entrySet()) {
                MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(entry.getKey(), entry.getValue());
                boolQueryBuilder.must(matchQueryBuilder);
            }
        }
        //精确查询参数map  参数or连接
        if (!CollUtil.isEmpty(orMap)) {
            for (Map.Entry<String, Object> entry : orMap.entrySet()) {
                MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(entry.getKey(), entry.getValue());
                boolQueryBuilder.should(matchQueryBuilder);
            }
        }
        return boolQueryBuilder;
    }

    /**
     * 多条件查询--深分页,一次返回匹配条件的全部数据
     * @return
     * @throws IOException
     */
    public List<Map> getEsData(String indexName,Map<String, Object> andMap
            , Map<String, Object> orMap) throws Exception {
        SearchRequest searchRequest = new SearchRequest(indexName);
        //构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().trackTotalHits(true).size(MAX_QUERY_SIZE);
        BoolQueryBuilder boolQuery = moreCondition(andMap, orMap);//构建多条件查询
        final Scroll scroll = new Scroll(TimeValue.timeValueHours(1L));
        sourceBuilder.query(boolQuery);
        sourceBuilder.timeout(TimeValue.timeValueHours(1));
        searchRequest.source(sourceBuilder);
        searchRequest.scroll(scroll);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        List<Map> result = new LinkedList<>();
        while (searchHits != null && searchHits.length > 0) {
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                result.add(hit.getSourceAsMap());
            }
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();
        }
        // 清除滚屏
        if (scrollId != null) {
            clearScroll(scrollId);
        }
        return result;
    }

    /**
     * 清除滚屏
     * @param scrollId
     * @return
     */
    public boolean clearScroll(String scrollId) throws IOException {
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        return clearScrollResponse.isSucceeded();
    }

    /**
     * 处理先删除 再增加的情况
     * @param indexName 索引
     * @param deleteMap 删除条件 and
     * @param addData 添加的数据 list
     * @return true代表操作成功 false代表操作失败
     * @throws Exception
     */
    public boolean firstDeleteLastAdd(String indexName, Map<String, Object> deleteMap, List<Map<String, Object>> addData) throws Exception {
        //Thread.sleep(5*1000);
        SearchRequest searchRequest = new SearchRequest(indexName);
        //构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().trackTotalHits(true).size(MAX_QUERY_SIZE);
        BoolQueryBuilder boolQuery = moreCondition(deleteMap, null);//构建多条件查询
        sourceBuilder.query(boolQuery);
        sourceBuilder.timeout(TimeValue.timeValueHours(1));
        searchRequest.source(sourceBuilder);
        BulkRequest request = new BulkRequest();
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        System.out.println("删除条数"+hits.getHits().length);

        for (SearchHit hit : hits) {
            request.add(new DeleteRequest().index(indexName).id(hit.getId()));
        }
        for(Map<String, Object> addMap : addData){
            IndexRequest indexRequest = new IndexRequest(indexName);
            ObjectMapper objectMapper = JacksonConfig.getMapper();
            String s = objectMapper.writeValueAsString(addMap);
            indexRequest.source(s, XContentType.JSON);
            request.add(indexRequest);
        }
        if(hits.getTotalHits().value > 0 || addData.size() > 0){
            BulkResponse result = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
            return !result.hasFailures();
        } else
            return true;

    }

//    public void deleteAliases(String index,String aliases) throws IOException {
//        DeleteAliasRequest deleteAliasRequest = new DeleteAliasRequest(index,aliases);
//        restHighLevelClient.indices().deleteAlias(deleteAliasRequest, RequestOptions.DEFAULT);
//    }

    public void aliasUpdte(String oldIndex,String newIndex,String aliases) throws IOException {
        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        indicesAliasesRequest.addAliasAction(IndicesAliasesRequest.AliasActions.remove().index(oldIndex).alias(aliases));
        indicesAliasesRequest.addAliasAction(IndicesAliasesRequest.AliasActions.add().index(newIndex).alias(aliases));
        restHighLevelClient.indices().updateAliases(indicesAliasesRequest,RequestOptions.DEFAULT);
    }
    public void aliasUpdte(String index,String aliases) throws IOException {
        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        indicesAliasesRequest.addAliasAction(IndicesAliasesRequest.AliasActions.add().index(index).alias(aliases));
        restHighLevelClient.indices().updateAliases(indicesAliasesRequest,RequestOptions.DEFAULT);
    }
    public class ResultEntity {
        private List<Map<String, Object>> data;
        private int total;

        public List<Map<String, Object>> getData() {
            return data;
        }

        public void setData(List<Map<String, Object>> data) {
            this.data = data;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }
    }
}






















