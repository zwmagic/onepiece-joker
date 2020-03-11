package com.south.onepiece.joker.enums;

/**
 * 字符编码枚举
 *
 * @author zhangwenming
 * @date 2016/10/20 11:22
 * version: 1.0
 */
public enum RequestEncodeEnum {

    /**
     *
     */
    UTF8("utf-8"),
    GBK("gbk"),
    GB2312("gb2312"),
    ISO("ISO-8859-1");

    private String value;

    RequestEncodeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
