package com.south.onepiece.joker.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 请求代理接口
 *
 * @author zhangwenming
 * @date 2016/10/20 09:42
 * version: 1.0
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface IRequest {

    String value() default "";
}
