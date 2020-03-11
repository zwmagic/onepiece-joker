package com.south.onepiece.joker.annotation;

import com.south.onepiece.joker.enums.RequestEncodeEnum;
import com.south.onepiece.joker.enums.RequestResultEnum;
import com.south.onepiece.joker.enums.RequestTypeEnum;

import java.lang.annotation.*;

/**
 * @author zhangwenming
 * @date 2016/10/20 09:53
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IRequestMethod {

    /**
     * 目标URL
     *
     * @return
     */
    String path() default "";

    /**
     * 主机地址
     *
     * @return
     */
    String host() default "";

    /**
     * 请求类型
     *
     * @return
     */
    RequestTypeEnum type() default RequestTypeEnum.POST;

    /**
     * 返回类型,目前只支持对象类型
     *
     * @return
     */
    RequestResultEnum result() default RequestResultEnum.JSON;

    /**
     * 是不是默认转码字符编码
     *
     * @return
     */
    boolean isTranscoding() default false;

    /**
     * 字符编码
     *
     * @return
     */
    RequestEncodeEnum encode() default RequestEncodeEnum.UTF8;

    /**
     * 连接超时
     *
     * @return
     */
    int connectTimeout() default -1;

}
