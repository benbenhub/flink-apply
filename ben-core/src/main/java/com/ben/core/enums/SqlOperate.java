package com.ben.core.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author learn
 * @date 2023/5/26 16:57
 */
public enum SqlOperate {

    insert(0, "insert"),
    update(1, "update"),
    delete(2,"delete"),
    ddl(3,"ddl"),
    dml(4,"dml");

    private final int type;

    private final String name;

    SqlOperate(int type, String name) {
        this.type = type;
        this.name = name;
    }
    public static SqlOperate getByName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("没有此类型！");
        }
        switch (name) {
            case "update":
                return update;
            case "delete":
                return delete;
            case "ddl":
                return ddl;
            case "dml":
                return dml;
            case "insert":
            default:
                return insert;
        }
    }
    public int getType() {
        return type;
    }
    public String getName() {
        return name;
    }
}
