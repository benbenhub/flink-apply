package com.ben.core.connector.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.apache.flink.api.common.io.RichOutputFormat;
import org.apache.flink.configuration.Configuration;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;

/**
 * @author learn
 * @date 2023/2/9 22:13
 */
public class MongodbUpdateOutputFormat extends RichOutputFormat<Document> {

    private transient MongoClient mongoClient;
    private transient MongoCollection mongoCollection;

    private final String database;
    private final String collection;
    private final String[] whereArray;

    public MongodbUpdateOutputFormat(String database, String collection, String[] whereArray){
        this.database = database;
        this.collection = collection;
        this.whereArray = whereArray;
    }


    @Override
    public void configure(Configuration configuration) {

    }

    @Override
    public void open(int i, int i1) throws IOException {
        Configuration parameters = (Configuration) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        mongoClient = MongodbClientBase.getClient(parameters,database);
        // 连接到数据库
        MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
        // 创建集合
        mongoCollection = mongoDatabase.getCollection(collection);
    }

    @Override
    public void writeRecord(Document t) throws IOException {
        try {
            // 修改文档
            Bson eq = Filters.eq(whereArray[0], t.get(whereArray[0]).toString());
            for (int i = 1; i < whereArray.length; i++) {
                eq = Filters.and(eq, Filters.eq(whereArray[i], t.get(whereArray[i]).toString()));
            }
            mongoCollection.updateOne(eq, new Document("$set",t),new UpdateOptions().upsert(true));
            //mongoCollection.insertOne(t);

        } catch (Exception e) {
            throw  new IOException("Writer data to mongodb error", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (mongoClient != null) mongoClient.close();
    }
}
