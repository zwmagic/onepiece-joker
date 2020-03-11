package com.south.onepiece.joker.entity;

import com.south.onepiece.joker.annotation.IRequestMethod;

/**
 * 获取默认注解,减少注解的写入
 *
 * @author zhangwenming
 * @date 2016/10/22 12:25
 */
public class BaseAnnotation {

    @IRequestMethod(connectTimeout = 60)
    public void requestMethod() {
        throw new UnsupportedOperationException();
    }

}
