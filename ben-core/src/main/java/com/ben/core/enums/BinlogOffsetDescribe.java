package com.ben.core.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author learn
 * @date 2022/4/2 13:52
 */
public enum BinlogOffsetDescribe {
    /**
     * 从最早的偏移开始
     */
    earliest(0, "earliest"),
    /**
     * 从最新偏移量开始
     */
    latest(1, "latest"),
    /**
     * 从消费群体的承诺补偿开始，无重置策略
     */
    initial(2, "initial");

    private int type;

    private String name;

    BinlogOffsetDescribe(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public static BinlogOffsetDescribe getByName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("没有此类型！");
        }
        switch (name) {
            case "latest":
                return latest;
            case "initial":
                return initial;
            case "earliest":
            default:
                return earliest;
        }
    }
}
