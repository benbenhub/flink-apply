package com.ben.core.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author learn
 * @date 2022/8/24 10:45
 */
public enum JdbcConnectionType {

    clickhouse(0, "clickhouse"),

    mysql(1, "mysql"),
    doris(2,"doris");
    private int type;

    private String name;

    JdbcConnectionType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public static JdbcConnectionType getByName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("没有此类型！");
        }
        switch (name) {
            case "doris":
                return doris;
            case "mysql":
                return mysql;
            case "clickhouse":
            default:
                return clickhouse;

        }
    }
}
