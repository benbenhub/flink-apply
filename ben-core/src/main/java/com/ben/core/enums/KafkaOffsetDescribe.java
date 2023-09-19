package com.ben.core.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author learn
 * @date 2022/3/21 14:09
 */
public enum KafkaOffsetDescribe {
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
    committed(2, "committed"),
    /**
     * 从提交的偏移量开始，如果提交的偏移量不存在，也使用最早作为重置策略
     */
    committedEarliest(3, "committedEarliest");


    private int type;

    private String name;

    KafkaOffsetDescribe(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public static KafkaOffsetDescribe getByName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("没有此类型！");
        }
        switch (name) {
            case "latest":
                return latest;
            case "committed":
                return committed;
            case "committedEarliest":
                return committedEarliest;
            case "earliest":
            default:
                return earliest;
        }
    }

}
