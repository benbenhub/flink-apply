package com.ben.core.connector.elasticsearch7;

import com.ben.core.constance.ConfigKeys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.flink.api.common.io.DefaultInputSplitAssigner;
import org.apache.flink.api.common.io.RichInputFormat;
import org.apache.flink.api.common.io.statistics.BaseStatistics;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.io.GenericInputSplit;
import org.apache.flink.core.io.InputSplit;
import org.apache.flink.core.io.InputSplitAssigner;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.slice.SliceBuilder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author learn
 * @date 2022/7/8 9:54
 */
@Slf4j
public class Elasticsearch7InputFormat extends RichInputFormat<Map<String, Object>, InputSplit> {

    long keepAlive = 10;
    private static final long serialVersionUID = 1L;
//    private static final Logger LOG = LoggerFactory.getLogger(Elasticsearch7InputFormat.class);

    private transient RestHighLevelClient restHighLevelClient;
    private transient SearchRequest searchRequest;

    private final String query;
    private final String index;
    private final String[] fields;
    private final HttpHost[] hosts;

    private int fetchSize;

    private transient Scroll scroll;

    private String scrollId;

    private Iterator<Map<String, Object>> iterator;

    public Elasticsearch7InputFormat(HttpHost[] hosts, String index, String in, String... fields) throws IOException {
        this.hosts = hosts;
        this.index = index;
        this.query = in;
        this.fields = fields;
    }

    @Override
    public void configure(Configuration parameters) {

    }

    @Override
    public void openInputFormat() {
        Configuration parameters = (Configuration) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();

        fetchSize = parameters.getInteger(ConfigKeys.elasticsearch_bulk_flush_max_actions);

        try {
            restHighLevelClient = new RestHighLevelClient(Elasticsearch7ClientBase.getRestClientBuilder(hosts,parameters));
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void closeInputFormat() throws IOException {
        if(restHighLevelClient != null) {
            restHighLevelClient.close();
        }
    }

    @Override
    public BaseStatistics getStatistics(BaseStatistics cachedStatistics) throws IOException {
        return cachedStatistics;
    }

    @Override
    public InputSplit[] createInputSplits(int minNumSplits) throws IOException {
        InputSplit[] splits = new InputSplit[minNumSplits];
        for (int i = 0; i < minNumSplits; i++) {
            splits[i] = new GenericInputSplit(i, minNumSplits);
        }
        return splits;
    }

    @Override
    public InputSplitAssigner getInputSplitAssigner(InputSplit[] inputSplits) {
        return new DefaultInputSplitAssigner(inputSplits);
    }

    @Override
    public void open(InputSplit split) throws IOException {
        GenericInputSplit genericInputSplit = (GenericInputSplit) split;

        scroll = new Scroll(TimeValue.timeValueMinutes(keepAlive));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().trackTotalHits(true);
        searchSourceBuilder.size(fetchSize);

        if (genericInputSplit.getTotalNumberOfSplits() > 1) {
            searchSourceBuilder.slice(
                    new SliceBuilder(
                            genericInputSplit.getSplitNumber(),
                            genericInputSplit.getTotalNumberOfSplits()));
        }
        searchSourceBuilder.fetchSource(fields, new String[]{});
        searchSourceBuilder.query(QueryBuilders.wrapperQuery(query));

        searchRequest = new SearchRequest(index);
        searchRequest.scroll(scroll);
        searchRequest.source(searchSourceBuilder);
    }

    @Override
    public boolean reachedEnd() throws IOException {
        if (iterator != null && iterator.hasNext()) {
            return false;
        } else {
            return searchScroll();
        }
    }

    @Override
    public Map<String, Object> nextRecord(Map<String, Object> reuse) throws IOException {
        reuse = iterator.next();
        return reuse;
    }

    @Override
    public void close() throws IOException {
        if (scrollId == null) {
            return;
        }

        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse =
                restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        boolean succeeded = clearScrollResponse.isSucceeded();
        log.info("Clear scroll response:{}", succeeded);
    }

    private boolean searchScroll() throws IOException {
        SearchHit[] searchHits;
        if (scrollId == null) {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();
        } else {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            SearchResponse searchResponse =
                    restHighLevelClient.searchScroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();
        }

        List<Map<String, Object>> resultList = Lists.newArrayList();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> source = searchHit.getSourceAsMap();
            resultList.add(source);
        }

        iterator = resultList.iterator();
        return !iterator.hasNext();
    }
}
