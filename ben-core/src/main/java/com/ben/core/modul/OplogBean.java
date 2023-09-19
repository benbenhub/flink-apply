package com.ben.core.modul;

import com.alibaba.fastjson.JSONObject;
import com.ben.core.enums.SqlOperate;

import java.io.Serializable;
import java.util.Date;


/**
 * @author learn
 * @date 2023/6/6 14:30
 */
public class OplogBean implements Serializable {
    /**
     * 操作的时间戳
     */
    private Long TSMS;

    /**
     * mongodb 唯一值 _id
     */
    private String documentKey;
    /**
     * mongodb 完整json数据
     */
    private String fullDocument;
    /**
     * 更新说明 具体修改了那个字段alibaba.fastjson.JSONObject
     */
    private JSONObject updateDescription;
    /**
     * 库名
     */
    private String database;
    /**
     * 表名
     */
    private String collection;
    /**
     * insert;update;delete
     */
    private SqlOperate operationType;

    public Long getTSMS() {
        return TSMS;
    }

    public void setTSMS(Long TSMS) {
        this.TSMS = TSMS;
    }
    /**
     * 操作的时间，TSMS转为java.util.Date 不用赋值
     */
    public Date getDataTime() {
        return new Date(getTSMS());
    }

    public String getDocumentKey() {
        return documentKey;
    }

    public void setDocumentKey(String documentKey) {
        this.documentKey = documentKey;
    }

    public String getFullDocument() {
        return fullDocument;
    }

    public void setFullDocument(String fullDocument) {
        this.fullDocument = fullDocument;
    }

    public JSONObject getUpdateDescription() {
        return updateDescription;
    }

    public void setUpdateDescription(JSONObject updateDescription) {
        this.updateDescription = updateDescription;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public SqlOperate getOperationType() {
        return operationType;
    }

    public void setOperationType(SqlOperate operationType) {
        this.operationType = operationType;
    }

    @Override
    public String toString() {
        return "OplogBenn{" +
                "TSMS=" + TSMS +
                ", dataTime=" + getDataTime().toString() +
                ", documentKey='" + documentKey + '\'' +
                ", fullDocument='" + fullDocument + '\'' +
                ", updateDescription='" + updateDescription + '\'' +
                ", database='" + database + '\'' +
                ", collection='" + collection + '\'' +
                ", operationType='" + operationType + '\'' +
                '}';
    }

}
