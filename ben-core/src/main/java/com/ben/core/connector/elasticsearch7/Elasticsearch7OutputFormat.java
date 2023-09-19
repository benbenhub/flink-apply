package com.ben.core.connector.elasticsearch7;

import com.alibaba.fastjson.JSONObject;
import com.ben.core.constance.ConfigKeys;
import org.apache.flink.api.common.io.RichOutputFormat;
import org.apache.flink.configuration.Configuration;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class Elasticsearch7OutputFormat<T> extends RichOutputFormat<T> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(Elasticsearch7OutputFormat.class);

    private transient RestHighLevelClient restHighLevelClient;
    private transient BulkRequest bulkRequest;

    private final String index;
    private int batchCount = 0;
    private int batchSize = 0;
    private int maxRetries = 0;

    private final HttpHost[] hosts;

    public Elasticsearch7OutputFormat(String index, HttpHost... httpHosts) {
        this.hosts = httpHosts;
        this.index = index;
    }

    /**
     * 批量增加文档，可以支持同时增加不同索引文档，格式 [{"index":"xxx","id":"xx","json":"xxx"}, {"index":"xxx","id":"xx","json":"xxx"}]
     *
     * @throws IOException
     */
    public synchronized void flush() throws IOException {

        for (int i = 0; i <= maxRetries; i++) {
            try {
                attemptFlush();
                bulkRequest = new BulkRequest();
                batchCount = 0;
                break;
            } catch (Exception e) {
                LOG.error("ElasticSearch executeBatch error, retry times = {}", i, e);
                if (i >= maxRetries) {
                    throw new IOException(e);
                }
                try {
                    Thread.sleep(1000L * i);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IOException(
                            "unable to flush; interrupted while doing another attempt", e);
                }
            }
        }


    }

    private void attemptFlush() throws IOException {
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        // 全部操作成功
        if (!bulkResponse.hasFailures()) {
//            LOG.info("批量增加操作成功");
        } else {
            for (BulkItemResponse bulkItemResponse : bulkResponse) {
                if (bulkItemResponse.isFailed()) {
                    BulkItemResponse.Failure failure = bulkItemResponse.getFailure();
                    throw new IOException("批量增加失败，原因：{}" + failure.getMessage());
                } else {
//                    LOG.info("文档批量增加成功");
                }
            }
        }
    }


    @Override
    public void configure(Configuration esConfigProperties) {

    }

    @Override
    public void open(int i, int i1) throws IOException {
        Configuration parameters = (Configuration) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();

        batchSize = parameters.getInteger(ConfigKeys.elasticsearch_bulk_flush_max_actions);
        maxRetries = parameters.getInteger(ConfigKeys.elasticsearch_max_retries);

        try {
            restHighLevelClient = new RestHighLevelClient(Elasticsearch7ClientBase.getRestClientBuilder(hosts,parameters));
        } catch (Exception e) {
            throw new RuntimeException("Connect to ElasticSearch failed.", e);
        }
        bulkRequest = new BulkRequest();
    }

    /**
     * 新数据写入记录
     * 处理之后调用 addToBatch添加到批处理队列中
     * @param t 从算子过来的新数据
     * @update 2020/7/30
     * 将原有Map修改为JSONObject，实现泛型真正使用起来，原有强转map将其锁死在map类中，而
     * 传参为泛型，具有迷惑性，存在潜在风险。 JSONobject可以接受任何类型enty实体类，包含Map
     * 对原有逻辑无影响
     * */
    @Override
    public void writeRecord(T t) throws IOException {
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(t);
        String _id = "";
        if (jsonObject.containsKey("_id")) {
            _id = jsonObject.getString("_id");
            jsonObject.remove("_id");
        }
        String s = jsonObject.toJSONString();
        addToBatch(s, _id);
    }

    protected final synchronized void addToBatch(String json, String _id) throws IOException {
        IndexRequest request = new IndexRequest(index).source(json, XContentType.JSON);
        if (!_id.isEmpty())
            request.id(_id);

        bulkRequest.add(request);
        batchCount++;
        if (batchSize > 0 && batchCount >= batchSize) {
            try {
                flush();
            } catch (IOException e) {
                throw new IOException("Writing records to ElasticSearch failed.", e);
            }
        }
    }

    @Override
    public void close() throws IOException {

        if (batchCount > 0) {
            try {
                flush();
            } catch (Exception e) {
                LOG.warn("Writing records to ElasticSearch failed.", e);
                throw new RuntimeException("Writing records to ElasticSearch failed.", e);
            }
        }
        restHighLevelClient.close();
    }

}
