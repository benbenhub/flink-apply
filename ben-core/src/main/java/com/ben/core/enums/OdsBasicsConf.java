package com.ben.core.enums;

/**
 * @author learn
 * @date 2023/5/19 16:02
 */
public class OdsBasicsConf {
    /**
     * binlog写入到kafka的topic
     */
    public static final String binlogTopic = "realKF-binlog-kafka11";
    /**
     * binlog数据同步完ods后再写入kafka的topic
     */
    public static final String odsTopic = "realKF-mysql-ods11";
    /**
     * oplog写入到kafka的topic
     */
    public static final String oplogTopic = "realKF-oplog-kafka";
    /**
     * oplog写入到kafka的topic
     */
    public static final String oplogOdsTopic = "realKF-mongodb-ods";

}
