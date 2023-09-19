package com.ben.core.connector.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import com.ben.core.constance.ConfigKeys;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.io.DefaultInputSplitAssigner;
import org.apache.flink.api.common.io.RichInputFormat;
import org.apache.flink.api.common.io.statistics.BaseStatistics;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.io.InputSplit;
import org.apache.flink.core.io.InputSplitAssigner;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author learn
 * @date 2023/2/10 9:36
 */
@Slf4j
public class MongodbInputFormat extends RichInputFormat<Map<String, Object>, InputSplit> {

    private final String filter;
    private final int fetchSize;

    private transient MongoClient mongoClient;
    private transient MongoCursor<Document> cursor;

    private final String database;
    private final String collection;

    private final Configuration parameters;

    public MongodbInputFormat(String database, String collection, String filter, Configuration parameters){
        this.database = database;
        this.collection = collection;
        this.filter = filter;
        this.parameters = parameters;
        this.fetchSize = parameters.getInteger(ConfigKeys.mongodb_fetch_size);
    }

    @Override
    public void openInputFormat() throws IOException {

    }

    @Override
    public void closeInputFormat() throws IOException {
        closeMongo(mongoClient, cursor);
    }

    @Override
    public InputSplit[] createInputSplits(int minNumSplits) {

        ArrayList<MongodbInputSplit> splits = new ArrayList<>();

        MongoClient client = null;
        try {
            client = MongodbClientBase.getClient(parameters, database);

            MongoDatabase db = client.getDatabase(database);

            MongoCollection<Document> collectionTable = db.getCollection(collection);

            // 不使用 collection.countDocuments() 获取总数是因为这个方法在大数据量时超时，导致出现超时异常结束任务
            long docNum = collectionTable.estimatedDocumentCount();
            if (docNum <= minNumSplits) {
                splits.add(new MongodbInputSplit(0, (int) docNum));
                return splits.toArray(new MongodbInputSplit[0]);
            }

            long size = Math.floorDiv(docNum, minNumSplits);
            for (int i = 0; i < minNumSplits; i++) {
                splits.add(new MongodbInputSplit((int) (i * size), (int) size));
            }

            if (size * minNumSplits < docNum) {
                splits.add(
                        new MongodbInputSplit(
                                (int) (size * minNumSplits), (int) (docNum - size * minNumSplits)));
            }
        } catch (Exception e) {
            log.error("error to create inputSplits, e = {}", e.getMessage());
            throw e;
        } finally {
            closeMongo(client, null);
        }

        return splits.toArray(new MongodbInputSplit[0]);

    }

    @Override
    public void open(InputSplit inputSplit) throws IOException {
        log.info("inputSplit = {}", inputSplit);
        MongodbInputSplit split = (MongodbInputSplit) inputSplit;
        FindIterable<Document> findIterable;

        mongoClient = MongodbClientBase.getClient(parameters, database);

        MongoDatabase db = mongoClient.getDatabase(database);

        MongoCollection<Document> collectionTable = db.getCollection(collection);

        if (filter == null || filter.isEmpty()) {
            findIterable = collectionTable.find();
        } else {
            findIterable = collectionTable.find(BasicDBObject.parse(filter));
        }

        findIterable =
                findIterable.skip(split.getSkip()).limit(split.getLimit()).batchSize(fetchSize);
        cursor = findIterable.iterator();
    }

    @Override
    public boolean reachedEnd() {
        return !cursor.hasNext();
    }

    @Override
    public Map<String, Object> nextRecord(Map<String, Object> stringObjectMap) {
        Document document = cursor.next();
        try {
            return document;
        } catch (Exception e) {
            throw new RuntimeException("describing errors when read a record ", e);
        }
    }

    @Override
    public void close() throws IOException {
        closeMongo(mongoClient, cursor);
    }

    private void closeMongo(MongoClient mongoClient, MongoCursor<Document> cursor) {
        if (cursor != null) {
            log.info("Start close mongodb cursor");
            cursor.close();
            log.info("Close mongodb cursor successfully");
        }

        if (mongoClient != null) {
            log.info("Start close mongodb client");
            mongoClient.close();
            log.info("Close mongodb client successfully");
        }
    }


    @Override
    public InputSplitAssigner getInputSplitAssigner(InputSplit[] inputSplits) {
        return new DefaultInputSplitAssigner(inputSplits);
    }
    @Override
    public void configure(Configuration configuration) {

    }

    @Override
    public BaseStatistics getStatistics(BaseStatistics baseStatistics) throws IOException {
        return baseStatistics;
    }
}
