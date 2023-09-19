package com.ben.core.constance;

import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.streaming.api.CheckpointingMode;

public class ConfigKeys {

    // ***************************checkpoint****************************

    public static final ConfigOption<String> checkpoint_backend =
            ConfigOptions.key("checkpoint_backend")
                    .stringType()
                    .defaultValue("file:///opt/module/flink/checkpoints/")
                    .withDescription("checkpoint的保存路径，默认保存在/opt/module/flink/checkpoints/");

    public static final ConfigOption<Long> checkpoint_enablecheckpointing =
            ConfigOptions.key("checkpoint_enablecheckpointing")
                    .longType()
                    .defaultValue(5*60*1000L)
                    .withDescription("触发checkpoint的时间周期，默认5分钟");

    public static final ConfigOption<Long> checkpoint_interval =
            ConfigOptions.key("checkpoint_interval")
                    .longType()
                    .defaultValue(5*60*1000L)
                    .withDescription("设置checkpoint的周期，默认5分钟");

    public static final ConfigOption<Long> checkpoint_timeout =
            ConfigOptions.key("checkpoint_timeout")
                    .longType()
                    .defaultValue(10*60*1000L)
                    .withDescription("超过时间则丢弃，默认10分钟");

    public static final ConfigOption<Long> checkpoint_minPauseBetweenCheckpoints =
            ConfigOptions.key("checkpoint_minPauseBetweenCheckpoints")
                    .longType()
                    .defaultValue(60 * 1000L)
                    .withDescription("checkpoint最小间隔，默认1分钟");

    public static final ConfigOption<Integer> checkpoint_maxConcurrent =
            ConfigOptions.key("checkpoint_maxConcurrent")
                    .intType()
                    .defaultValue(1)
                    .withDescription("checkpoint并发次数，默认1");

    public static final ConfigOption<Boolean> checkpoint_enableUnaligned =
            ConfigOptions.key("checkpoint_enableUnaligned")
                    .booleanType()
                    .defaultValue(true)
                    .withDescription("启用未对齐的检查点，这将大大减少背压下的检查点时间，默认true启用");

    public static final ConfigOption<CheckpointingMode> checkpoint_mode =
            ConfigOptions.key("checkpoint_mode")
                    .enumType(CheckpointingMode.class)
                    .defaultValue(CheckpointingMode.AT_LEAST_ONCE)
                    .withDescription("指定CK的一致性语义，默认AT_LEAST_ONCE");

    public static final ConfigOption<Integer> checkpoint_fixedRestartNumber =
            ConfigOptions.key("checkpoint_fixedRestartNumber")
                    .intType()
                    .defaultValue(3)
                    .withDescription("指定从CK自动重启策略的重启次数，默认重启3次");

    public static final ConfigOption<Long> checkpoint_fixedRestartDelay =
            ConfigOptions.key("checkpoint_fixedRestartDelay")
                    .longType()
                    .defaultValue(2 * 1000L)
                    .withDescription("指定从CK自动重启策略的重启间隔，默认2秒");


    // ***************************clickhouse****************************

    public static final ConfigOption<String> clickhouse_host =
            ConfigOptions.key("clickhouse_host")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("clickhouse的连接地址");

    public static final ConfigOption<String> clickhouse_port =
            ConfigOptions.key("clickhouse_port")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("clickhouse的连接端口");

    public static final ConfigOption<String> clickhouse_username =
            ConfigOptions.key("clickhouse_username")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("clickhouse的用户名");

    public static final ConfigOption<String> clickhouse_password =
            ConfigOptions.key("clickhouse_password")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("clickhouse的密码");

    public static final ConfigOption<String> clickhouse_database =
            ConfigOptions.key("clickhouse_database")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("clickhouse的连接库");

    public static final ConfigOption<String> clickhouse_cluster =
            ConfigOptions.key("clickhouse_cluster")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("clickhouse的集群名称");

    public static final ConfigOption<Integer> clickhouse_batch_size =
            ConfigOptions.key("clickhouse_batch_size")
                    .intType()
                    .defaultValue(5000)
                    .withDescription("clickhouse 每批次发送数据量");

    public static final ConfigOption<Integer> clickhouse_max_retry =
            ConfigOptions.key("clickhouse_max_retry")
                    .intType()
                    .defaultValue(3)
                    .withDescription("clickhouse 最大重试次数");

    public static final ConfigOption<Long> clickhouse_batch_interval =
            ConfigOptions.key("clickhouse_batch_interval")
                    .longType()
                    .defaultValue(1000L)
                    .withDescription("clickhouse 发送等待时间，当时间到了，数量没到强制发送");


    public static final ConfigOption<Integer> clickhouse_maximumConnection =
            ConfigOptions.key("clickhouse_maximumConnection")
                    .intType()
                    .defaultValue(20)
                    .withDescription("clickhouse 连接池最大数量");

    public static final ConfigOption<String> clickhouse_driver =
            ConfigOptions.key("clickhouse_driver")
                    .stringType()
                    .defaultValue("ru.yandex.clickhouse.ClickHouseDriver")
                    .withDescription("clickhouse的驱动类");

    public static final ConfigOption<Long> clickhouse_socket_timeout =
            ConfigOptions.key("clickhouse_socket_timeout")
                    .longType()
                    .defaultValue(240000000L)
                    .withDescription("clickhouse 连接超时时间");

    public static final ConfigOption<Integer> clickhouse_min_evictable_idle_time_millis =
            ConfigOptions.key("clickhouse_min_evictable_idle_time_millis")
                    .intType()
                    .defaultValue(60000)
                    .withDescription("clickhouse 最小可驱逐空闲时间毫秒数");

    public static final ConfigOption<Integer> clickhouse_max_evictable_idle_time_millis =
            ConfigOptions.key("clickhouse_max_evictable_idle_time_millis")
                    .intType()
                    .defaultValue(60000)
                    .withDescription("clickhouse 最大可驱逐空闲时间毫秒数");

    public static final ConfigOption<Integer> clickhouse_keep_alive_between_time_millis =
            ConfigOptions.key("clickhouse_keep_alive_between_time_millis")
                    .intType()
                    .defaultValue(60000)
                    .withDescription("clickhouse 在两次保持活动之间的时间毫秒数");

    // ***************************mysql****************************

    public static final ConfigOption<String> mysql_host =
            ConfigOptions.key("mysql_host")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("mysql的连接地址");

    public static final ConfigOption<String> mysql_port =
            ConfigOptions.key("mysql_port")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("mysql的连接端口");

    public static final ConfigOption<String> mysql_username =
            ConfigOptions.key("mysql_username")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("mysql的用户名");

    public static final ConfigOption<String> mysql_password =
            ConfigOptions.key("mysql_password")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("mysql的密码");

    public static final ConfigOption<String> mysql_database =
            ConfigOptions.key("mysql_database")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("mysql的连接库");

    public static final ConfigOption<Integer> mysql_batch_size =
            ConfigOptions.key("mysql_batch_size")
                    .intType()
                    .defaultValue(1000)
                    .withDescription("mysql 每批次发送数据量");

    public static final ConfigOption<Integer> mysql_max_retry =
            ConfigOptions.key("mysql_max_retry")
                    .intType()
                    .defaultValue(3)
                    .withDescription("mysql 最大重试次数");

    public static final ConfigOption<Long> mysql_batch_interval =
            ConfigOptions.key("mysql_batch_interval")
                    .longType()
                    .defaultValue(200L)
                    .withDescription("mysql 发送等待时间，当时间到了，数量没到强制发送");

    public static final ConfigOption<Integer> mysql_maximumConnection =
            ConfigOptions.key("mysql_maximumConnection")
                    .intType()
                    .defaultValue(20)
                    .withDescription("mysql 连接池最大数量");

    public static final ConfigOption<String> mysql_driver =
            ConfigOptions.key("mysql_driver")
                    .stringType()
                    .defaultValue("com.mysql.cj.jdbc.Driver")
                    .withDescription("mysql的驱动类");

    public static final ConfigOption<Long> mysql_socket_timeout =
            ConfigOptions.key("mysql_socket_timeout")
                    .longType()
                    .defaultValue(240000000L)
                    .withDescription("mysql 连接超时时间");

    // ***************************elasticsearch****************************
    public static final ConfigOption<String> elasticsearch_host =
            ConfigOptions.key("elasticsearch_host")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("elasticsearch的连接地址");

    public static final ConfigOption<Integer> elasticsearch_port =
            ConfigOptions.key("elasticsearch_port")
                    .intType()
                    .noDefaultValue()
                    .withDescription("elasticsearch的连接端口");

    public static final ConfigOption<String> elasticsearch_username =
            ConfigOptions.key("elasticsearch_username")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("elasticsearch_username的用户名");

    public static final ConfigOption<String> elasticsearch_password =
            ConfigOptions.key("elasticsearch_password")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("elasticsearch的密码");

    public static final ConfigOption<String> elasticsearch_scheme =
            ConfigOptions.key("elasticsearch_scheme")
                    .stringType()
                    .defaultValue("http")
                    .withDescription("elasticsearch的scheme 默认http");

    public static final ConfigOption<Integer> elasticsearch_max_query_size =
            ConfigOptions.key("elasticsearch_max_query_size")
                    .intType()
                    .defaultValue(1000)
                    .withDescription("最大查询数,默认1000");

    public static final ConfigOption<Integer> elasticsearch_max_retries =
            ConfigOptions.key("elasticsearch_max_retries")
                    .intType()
                    .defaultValue(3)
                    .withDescription("elasticsearch_max_retries 最大重试次数");

    public static final ConfigOption<Integer> elasticsearch_bulk_flush_max_actions =
            ConfigOptions.key("elasticsearch_bulk_flush_max_actions")
                    .intType()
                    .defaultValue(1000)
                    .withDescription("elasticsearch_bulk_flush_max_actions 默认1000");

    public static final ConfigOption<Long> elasticsearch_bulk_flush_interval =
            ConfigOptions.key("elasticsearch_bulk_flush_interval")
                    .longType()
                    .defaultValue(10000L)
                    .withDescription("设置大容量刷新间隔（以毫秒为单位）。你可以通过-1来禁用它。 默认10秒");

    public static final ConfigOption<Integer> elasticsearch_socket_time_out =
            ConfigOptions.key("elasticsearch_socket_time_out")
                    .intType()
                    .defaultValue(30000)
                    .withDescription("elasticsearch_socket_time_out 默认30000");

    public static final ConfigOption<Integer> elasticsearch_connect_time_out =
            ConfigOptions.key("elasticsearch_connect_time_out")
                    .intType()
                    .defaultValue(30000)
                    .withDescription("elasticsearch_connect_time_out 默认30000");

    public static final ConfigOption<Integer> elasticsearch_connection_request_time_out =
            ConfigOptions.key("elasticsearch_connection_request_time_out")
                    .intType()
                    .defaultValue(30000)
                    .withDescription("elasticsearch_connection_request_time_out 默认30000");

    public static final ConfigOption<Integer> elasticsearch_max_conn_total =
            ConfigOptions.key("elasticsearch_max_conn_total")
                    .intType()
                    .defaultValue(100)
                    .withDescription("最大连接总数 默认100");

    public static final ConfigOption<Integer> elasticsearch_max_conn_per_route =
            ConfigOptions.key("elasticsearch_max_conn_per_route")
                    .intType()
                    .defaultValue(30)
                    .withDescription("为每条管线分配最大连接值。 默认30");


    // ***************************doris****************************
    public static final ConfigOption<String> doris_host =
            ConfigOptions.key("doris_host")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("doris的连接地址");

    public static final ConfigOption<String> doris_port =
            ConfigOptions.key("doris_port")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("doris的连接端口");

    public static final ConfigOption<String> doris_username =
            ConfigOptions.key("doris_username")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("doris的用户名");

    public static final ConfigOption<String> doris_password =
            ConfigOptions.key("doris_password")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("doris的密码");

    public static final ConfigOption<String> doris_database =
            ConfigOptions.key("doris_database")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("doris的连接库");

    public static final ConfigOption<Integer> doris_batch_size =
            ConfigOptions.key("doris_batch_size")
                    .intType()
                    .defaultValue(1000)
                    .withDescription("doris 每批次发送数据量");

    public static final ConfigOption<Integer> doris_max_retry =
            ConfigOptions.key("doris_max_retry")
                    .intType()
                    .defaultValue(3)
                    .withDescription("doris 最大重试次数");

    public static final ConfigOption<Long> doris_batch_interval =
            ConfigOptions.key("doris_batch_interval")
                    .longType()
                    .defaultValue(200L)
                    .withDescription("doris 发送等待时间，当时间到了，数量没到强制发送");

    public static final ConfigOption<Integer> doris_maximumConnection =
            ConfigOptions.key("doris_maximumConnection")
                    .intType()
                    .defaultValue(20)
                    .withDescription("mysql 连接池最大数量");

    public static final ConfigOption<String> doris_driver =
            ConfigOptions.key("doris_driver")
                    .stringType()
                    .defaultValue("com.mysql.cj.jdbc.Driver")
                    .withDescription("doris的驱动类");

    public static final ConfigOption<Long> doris_socket_timeout =
            ConfigOptions.key("doris_socket_timeout")
                    .longType()
                    .defaultValue(240000000L)
                    .withDescription("doris 连接超时时间");

    public static final ConfigOption<Integer> doris_initialSize =
            ConfigOptions.key("doris_initialSize")
                    .intType()
                    .defaultValue(5)
                    .withDescription("doris线程池 初始化连接个数");

    public static final ConfigOption<Integer> doris_minIdle =
            ConfigOptions.key("doris_minIdle")
                    .intType()
                    .defaultValue(1)
                    .withDescription("doris线程池 最小连接数量");

    public static final ConfigOption<Integer> doris_maxWait =
            ConfigOptions.key("doris_maxWait")
                    .intType()
                    .defaultValue(30000)
                    .withDescription("doris线程池 连接等待超时时间");

    public static final ConfigOption<Integer> doris_timeBetweenEvictionRunsMillis =
            ConfigOptions.key("doris_timeBetweenEvictionRunsMillis")
                    .intType()
                    .defaultValue(3000)
                    .withDescription("doris线程池 连接检测间隔时间");

    public static final ConfigOption<Integer> doris_minEvictableIdleTimeMillis =
            ConfigOptions.key("doris_minEvictableIdleTimeMillis")
                    .intType()
                    .defaultValue(30000)
                    .withDescription("doris线程池 一个连接在池中最小生存时间");

    public static final ConfigOption<Integer> doris_maxEvictableIdleTimeMillis =
            ConfigOptions.key("doris_maxEvictableIdleTimeMillis")
                    .intType()
                    .defaultValue(30000)
                    .withDescription("doris线程池 一个连接最多生存时间");

    public static final ConfigOption<Integer> doris_phyTimeoutMillis =
            ConfigOptions.key("doris_phyTimeoutMillis")
                    .intType()
                    .defaultValue(30000)
                    .withDescription("doris线程池 连接最大物理连接时长");
    public static final ConfigOption<Boolean> doris_keepAlive =
            ConfigOptions.key("doris_keepAlive")
                    .booleanType()
                    .defaultValue(true)
                    .withDescription("doris线程池 活性池是否开启");
    public static final ConfigOption<Integer> doris_keepAliveBetweenTimeMillis =
            ConfigOptions.key("doris_keepAliveBetweenTimeMillis")
                    .intType()
                    .defaultValue(60000)
                    .withDescription("doris线程池 空闲连接是否放入活性池的最大时间");
    public static final ConfigOption<Boolean> doris_testWhileIdle=
            ConfigOptions.key("doris_testWhileIdle")
                    .booleanType()
                    .defaultValue(true)
                    .withDescription("doris线程池 是否开启对空闲连接进行测试");
    public static final ConfigOption<Boolean> doris_usePingMethod=
            ConfigOptions.key("doris_usePingMethod")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("doris线程池 mysql连接是否开启ping检测");
    // ***************************binlog****************************

    public static final ConfigOption<String> binlog_host =
            ConfigOptions.key("binlog_host")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("binlog的连接地址");

    public static final ConfigOption<Integer> binlog_port =
            ConfigOptions.key("binlog_port")
                    .intType()
                    .noDefaultValue()
                    .withDescription("binlog的连接端口");

    public static final ConfigOption<String> binlog_username =
            ConfigOptions.key("binlog_username")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("binlog的用户名");

    public static final ConfigOption<String> binlog_password =
            ConfigOptions.key("binlog_password")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("binlog的密码");

    public static final ConfigOption<String> binlog_database_list =
            ConfigOptions.key("binlog_database_list")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("binlog 数据库列表 多个;号分隔");

    public static final ConfigOption<String> binlog_table_list =
            ConfigOptions.key("binlog_table_list")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("binlog 表列表 多个;号分隔");

    public static final ConfigOption<String> binlog_server_id =
            ConfigOptions.key("binlog_server_id")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("此数据库客户端的数字ID，在MySQL集群中当前运行的所有数据库进程中必须是唯一的。此连接器作为另一个服务器（具有此唯一ID）加入MySQL数据库集群，以便它可以读取binlog。默认情况下，生成的随机数介于5400和6400之间，但我们建议设置一个显式值。");

//    public static final ConfigOption<String> binlog_startup_options =
//            ConfigOptions.key("binlog_startup_options")
//                    .stringType()
//                    .defaultValue("latest")
//                    .withDescription("指定启动选项。默认latest,时间戳启动（timestamp-1000）");

    public static final ConfigOption<Boolean> binlog_scan_newly_added_table_enabled =
            ConfigOptions.key("binlog_scan_newly_added_table_enabled")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("是否扫描新加表，默认false不扫描");

    public static final ConfigOption<Boolean> binlog_include_schema_changes =
            ConfigOptions.key("binlog_include_schema_changes")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("MySqlSource是否应输出架构更改。默认false不包含");

    // ***************************kafka****************************

    public static final ConfigOption<String> kafka_bootstrap_servers =
            ConfigOptions.key("kafka_bootstrap_servers")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("kafka_bootstrap_servers 无默认");

    public static final ConfigOption<String> kafka_deliver_guarantee =
            ConfigOptions.key("kafka_deliver_guarantee")
                    .stringType()
                    .defaultValue("at-least-once")
                    .withDescription("kafka_deliver_guarantee kafka sink 一致性语义 默认at-least-once");

    // ***************************HttpClient****************************

    public static final ConfigOption<Integer> httpclient_connect_timeout =
            ConfigOptions.key("httpclient_connect_timeout")
                    .intType()
                    .defaultValue(5000)
                    .withDescription("httpclient_connect_timeout 设置客户端和服务端建立连接的超时时间");

    public static final ConfigOption<Integer> httpclient_socket_timeout =
            ConfigOptions.key("httpclient_socket_timeout")
                    .intType()
                    .defaultValue(5000)
                    .withDescription("httpclient_socket_timeout 设置客户端从服务端读取数据的超时时间");

    public static final ConfigOption<Integer> httpclient_connection_request_timeout =
            ConfigOptions.key("httpclient_connection_request_timeout")
                    .intType()
                    .defaultValue(200)
                    .withDescription("httpclient_connection_request_timeout 设置从连接池获取连接的超时时间，不宜过长");

    public static final ConfigOption<Integer> httpclient_max_total =
            ConfigOptions.key("httpclient_max_total")
                    .intType()
                    .defaultValue(100)
                    .withDescription("httpclient_max_total 连接池最大连接数");

    public static final ConfigOption<Integer> httpclient_default_max_per_route =
            ConfigOptions.key("httpclient_default_max_per_route")
                    .intType()
                    .defaultValue(100)
                    .withDescription("httpclient_default_max_per_route 设置同路由的并发数");

    public static final ConfigOption<Integer> httpclient_time_to_live =
            ConfigOptions.key("httpclient_time_to_live")
                    .intType()
                    .defaultValue(30)
                    .withDescription("httpclient_time_to_live Httpclient连接池，长连接保持30秒");

    public static final ConfigOption<Integer> httpclient_validate_after_inactivity =
            ConfigOptions.key("httpclient_validate_after_inactivity")
                    .intType()
                    .defaultValue(1000)
                    .withDescription("httpclient_validate_after_inactivity 可用空闲连接过期时间");

    // ***************************Mongodb****************************

    public static final ConfigOption<String> mongodb_uri =
            ConfigOptions.key("mongodb_uri")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("mongodb_uri 无默认");

    public static final ConfigOption<String> mongodb_host =
            ConfigOptions.key("mongodb_host")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("mongodb_host 无默认");

    public static final ConfigOption<String> mongodb_username =
            ConfigOptions.key("mongodb_username")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("mongodb_username 无默认");

    public static final ConfigOption<String> mongodb_password =
            ConfigOptions.key("mongodb_password")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("mongodb_password 无默认");

    public static final ConfigOption<Integer> mongodb_port =
            ConfigOptions.key("mongodb_port")
                    .intType()
                    .noDefaultValue()
                    .withDescription("mongodb_port 无默认");

    public static final ConfigOption<Integer> mongodb_connect_timeout =
            ConfigOptions.key("mongodb_connect_timeout")
                    .intType()
                    .defaultValue(10000)
                    .withDescription("mongodb_connect_timeout 默认10000");

    public static final ConfigOption<Integer> mongodb_max_wait_time =
            ConfigOptions.key("mongodb_max_wait_time")
                    .intType()
                    .defaultValue(5000)
                    .withDescription("mongodb_max_wait_time 默认5000");

    public static final ConfigOption<Integer> mongodb_socket_timeout =
            ConfigOptions.key("mongodb_socket_timeout")
                    .intType()
                    .defaultValue(6000)
                    .withDescription("mongodb_max_wait_time 默认3000");

    public static final ConfigOption<Integer> mongodb_connections_per_host =
            ConfigOptions.key("mongodb_connections_per_host")
                    .intType()
                    .defaultValue(100)
                    .withDescription("设置每个主机的最大连接数 mongodb_connections_per_host 默认100");

    public static final ConfigOption<Integer> mongodb_fetch_size =
            ConfigOptions.key("mongodb_fetch_size")
                    .intType()
                    .defaultValue(1000)
                    .withDescription("mongodb_fetch_size 默认1000");

    public static final ConfigOption<String> mongodb_binlog_database_list =
            ConfigOptions.key("mongodb_binlog_database_list")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("binlog 数据库列表 多个;号分隔");

    public static final ConfigOption<String> mongodb_binlog_table_list =
            ConfigOptions.key("mongodb_binlog_table_list")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("多个;号分隔");

    public static final ConfigOption<Integer> mongodb_async_threads_num =
            ConfigOptions.key("mongodb_async_threads_num")
                    .intType()
                    .defaultValue(10)
                    .withDescription("异步处理函数线程个数，默认10");


    // ***************************JDBC连接****************************

    public static final ConfigOption<Integer> connectTimeout =
            ConfigOptions.key("connectTimeout")
                    .intType()
                    .defaultValue(60000)
                    .withDescription("创建jdbc连接时，连接超时时间");
    public static final ConfigOption<Integer> socketTimeout =
            ConfigOptions.key("socketTimeout")
                    .intType()
                    .defaultValue(60000)
                    .withDescription("创建jdbc连接时，写入超时时间");
}
