package org.lili.forfun.infra.domain.constants;

public enum Env {
    /**
     * 日常
     */
    DAILY(0, "daily"),
    /**
     * 预发
     */
    PREPUB(1, "prepub"),
    /**
     * 生产
     */
    PUBLISH(2, "publish"),
    /**
     * 本地调试
     */
    TEST(3, "test");


    private final String stringValue;
    private final int intValue;

    Env(int i, String env) {
        this.intValue = i;
        this.stringValue = env;
    }

    public int intValue() {
        return this.intValue;
    }

    public String stringValue() {
        return stringValue;
    }

    public boolean equals(String s) {
        return this.stringValue.equals(s.toLowerCase());
    }
}
