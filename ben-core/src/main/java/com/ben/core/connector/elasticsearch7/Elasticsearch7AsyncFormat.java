package com.ben.core.connector.elasticsearch7;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

/**
 * @author learn
 * @date 2022/5/5 16:23
 */
public abstract class Elasticsearch7AsyncFormat<IN,OUT> extends RichAsyncFunction<IN, OUT> {

    private Elasticsearch7Function e7f;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        parameters = (Configuration) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        e7f = new Elasticsearch7Function();
        e7f.open(parameters);
    }

    @Override
    public void close() throws Exception {
        super.close();
        if (e7f != null)
            e7f.close();
    }
    public Elasticsearch7Function getFunction() {
        return e7f;
    }
}
