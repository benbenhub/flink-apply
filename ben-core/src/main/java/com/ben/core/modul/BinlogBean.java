package com.ben.core.modul;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONType;
import com.ben.core.enums.SqlOperate;

import java.util.Date;
import java.util.Map;

/**
 * binlog日志数据实体类
 */
@JSONType(naming= PropertyNamingStrategy.SnakeCase)
public class BinlogBean {
    /**
     * 操作的时间戳
     */
    private Long TSMS;

    /**
     * 库名
     */
    private String database;
    /**
     * 表名
     */
    private String tableName;
    /**
     * insert;update;delete
     */
    private SqlOperate operationType;

    private Map<String,Object> dataBefore;
    private Map<String,Object> dataAfter;
    /**
     * 日志类型 DDL DML
     */
    private SqlOperate sqlLanguageType;
    /**
     * ddl语句
     */
    private String ddl;
    /**
     * 主键
     */
    private String tableKey;

    public String getTableKey() {
        return tableKey;
    }

    public void setTableKey(String tableKey) {
        this.tableKey = tableKey;
    }

    public Long getTSMS() {
        return TSMS;
    }

    public void setTSMS(Long TSMS) {
        this.TSMS = TSMS;
    }
    public Date getDataTime() {
        return new Date(getTSMS());
    }
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public SqlOperate getOperationType() {
        return operationType;
    }

    public void setOperationType(SqlOperate operationType) {
        this.operationType = operationType;
    }

    public Map<String,Object> getDataBefore() {
        return dataBefore;
    }

    public void setDataBefore(Map<String,Object> dataBefore) {
        this.dataBefore = dataBefore;
    }

    public Map<String,Object> getDataAfter() {
        return dataAfter;
    }

    public void setDataAfter(Map<String,Object> dataAfter) {
        this.dataAfter = dataAfter;
    }

    public SqlOperate getSqlLanguageType() {
        return sqlLanguageType;
    }

    public void setSqlLanguageType(SqlOperate sqlLanguageType) {
        this.sqlLanguageType = sqlLanguageType;
    }

    public String getDdl() {
        return ddl;
    }

    public void setDdl(String ddl) {
        this.ddl = ddl;
    }

    @Override
    public String toString() {
        return "BinlogBeen{" +
                "TSMS=" + TSMS +
                ", database='" + database + '\'' +
                ", tableName='" + tableName + '\'' +
                ", operationType='" + operationType + '\'' +
                ", dataBefore='" + dataBefore + '\'' +
                ", dataAfter='" + dataAfter + '\'' +
                ", sqlLanguageType='" + sqlLanguageType + '\'' +
                ", ddl='" + ddl + '\'' +
                '}';
    }
}
