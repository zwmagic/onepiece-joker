package com.south.onepiece.joker.entity;

/**
 * @author zhangwenming
 * @date 2016/10/20 18:56
 */
public class ParamsEntity {

    private String name = "";

    private String value;

    private int order = 0;

    public ParamsEntity() {
    }

    public ParamsEntity(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
