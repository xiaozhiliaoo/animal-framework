package org.lili.forfun.infra.domain.info;

/**
 * 以12生肖为服务名字
 */
public enum ServiceName {
    /**
     * 老鼠
     */
    RAT("rat"),
    /**
     * 牛
     */
    OX("ox"),
    /**
     * 虎
     */
    TIGER("tiger"),
    /**
     * 兔
     */
    HARE("hare"),
    /**
     * 龙
     */
    DRAGON("dragon"),
    /**
     * 蛇
     */
    SNAKE("snake"),
    /**
     * 马
     */
    HORSE("horse"),
    /**
     * 羊
     */
    SHEEP("sheep"),
    /**
     * 猴
     */
    MONKEY("monkey"),

    /**
     * 鸡
     */
    ROOSTER("rooster"),

    /**
     * 狗
     */
    DOG("dog"),

    /**
     * 猪
     */
    PIG("pig");

    private final String value;

    ServiceName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean equals(String s) {
        return this.value.equals(s.toLowerCase());
    }

}
