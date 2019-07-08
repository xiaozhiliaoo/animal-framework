package org.lili.forfun.infra.domain.constants;


public enum ProtoType {
    /**
     * sm
     */
    SM("sm"),
    /**
     * hsf
     */
    HSF("hsf"),
    /**
     * vipserver
     */
    VIPSERVER("vipserver");

    private String value;

    ProtoType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean equals(String s) {
        return this.value.equals(s.toLowerCase());
    }
}
