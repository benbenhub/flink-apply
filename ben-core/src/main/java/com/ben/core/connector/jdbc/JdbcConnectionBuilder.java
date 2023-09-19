package com.ben.core.connector.jdbc;

/**
 * @author learn
 * @date 2022/8/22 17:53
 */
public class JdbcConnectionBuilder {
    private String jdbcUrl ;
    private String username ;
    private String password ;
    private String driverName ;
    /*连接池最大线程数*/
    private int maxPoolConn;
    private int initialSize;
    private int minIdle;
    // 配置获取连接等待超时的时间
    private int maxWait;
    //配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
    private int timeBetweenEvictionRunsMillis;
    // 配置一个连接在池中最小生存的时间，单位是毫秒 超过这个时间每次会回收默认连接
    private int minEvictableIdleTimeMillis;
    //配置一个连接在池中最大生存的时间，单位是毫秒
    private int maxEvictableIdleTimeMillis;
    private int phyTimeoutMillis;
    private int keepAliveBetweenTimeMillis;
    private boolean keepAlive;
    private boolean testWhileIdle;
    private boolean usePingMethod;


    public JdbcConnectionBuilder setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }
    public JdbcConnectionBuilder setUserName(String username) {
        this.username = username;
        return this;
    }
    public JdbcConnectionBuilder setPassword(String password) {
        this.password = password;
        return this;
    }
    public JdbcConnectionBuilder setDriverName(String driverName) {
        this.driverName = driverName;
        return this;
    }
    public JdbcConnectionBuilder setMaxPoolConn(int maxPoolConn) {
        this.maxPoolConn = maxPoolConn;
        return this;
    }
    public JdbcConnectionBuilder setInitialSize(int initialSize) {
        this.initialSize = initialSize;
        return this;
    }
    public JdbcConnectionBuilder setMinIdle(int minIdle) {
        this.minIdle = minIdle;
        return this;
    }
    public JdbcConnectionBuilder setMaxWait(int maxWait) {
        this.maxWait = maxWait;
        return this;
    }
    public JdbcConnectionBuilder setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        return this;
    }
    public JdbcConnectionBuilder setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        return this;
    }
    public JdbcConnectionBuilder setMaxEvictableIdleTimeMillis(int maxEvictableIdleTimeMillis) {
        this.maxEvictableIdleTimeMillis = maxEvictableIdleTimeMillis;
        return this;
    }
    public JdbcConnectionBuilder setPhyTimeoutMillis(int phyTimeoutMillis) {
        this.phyTimeoutMillis = phyTimeoutMillis;
        return this;
    }

    public JdbcConnectionBuilder setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }
    public JdbcConnectionBuilder setTestWhileIdle(Boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
        return this;
    }
    public JdbcConnectionBuilder setKeepAliveBetweenTimeMillis(int keepAliveBetweenTimeMillis) {
        this.keepAliveBetweenTimeMillis = keepAliveBetweenTimeMillis;
        return this;
    }
    public JdbcConnectionBuilder setUsername(String username) {
        this.username = username;
        return this ;
    }
    public JdbcConnectionBuilder setUsePingMethod(Boolean usePingMethod) {
        this.usePingMethod = usePingMethod;
        return this ;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getDriverName() {
        return driverName;
    }
    public int getMaxPoolConn() {
        return maxPoolConn;
    }
    public int getInitialSize() { return initialSize; }
    public boolean getUsePingMethod() { return usePingMethod; }
    public boolean getKeepAlive() { return keepAlive; }
    public boolean getTestWhileIdle() { return testWhileIdle; }
    public int getKeepAliveBetweenTimeMillis() {
        return keepAliveBetweenTimeMillis;
    }
    public int getMinIdle() {
        return minIdle;
    }
    public int getMaxWait() {
        return maxWait;
    }
    public int getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }
    public int getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }
    public int getMaxEvictableIdleTimeMillis() {
        return maxEvictableIdleTimeMillis;
    }
    public int getPhyTimeoutMillis() {
        return phyTimeoutMillis;
    }
    public JdbcConnectionPool finish() throws Exception {
        return new JdbcConnectionPool(this);
    }
}
