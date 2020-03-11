package com.south.onepiece.joker.annotation;

import java.lang.annotation.*;

/**
 * Request 请求参数注解
 *
 * @author zhangwenming
 * @date 2016/10/20 10:41
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IRequestParam {

    /**
     * 参数名称 2014年5月15日
     *
     * @return
     */
    String value();

    /**
     * 顺序 for rest OR signCal 2014年5月15日
     *
     * @return
     */
    int order() default 0;


}
