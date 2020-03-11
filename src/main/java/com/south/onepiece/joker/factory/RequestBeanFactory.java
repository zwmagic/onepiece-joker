package com.south.onepiece.joker.factory;

import com.south.onepiece.joker.proxy.RequestProxyHandler;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * 代理工厂
 *
 * @author zhangwenming
 * @date 2016/10/20 14:57
 * version: 1.0
 */
public class RequestBeanFactory<T> implements FactoryBean<T> {

    private Class<T> requestInterface;

    private RequestProxyHandler proxy;

    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(requestInterface.getClassLoader(), new Class[]{requestInterface}, proxy);
    }

    @Override
    public Class<?> getObjectType() {
        return requestInterface;
    }

    public RequestProxyHandler getProxy() {
        return proxy;
    }

    public void setProxy(RequestProxyHandler proxy) {
        this.proxy = proxy;
    }

    public Class<T> getRequestInterface() {
        return requestInterface;
    }

    public void setRequestInterface(Class<T> requestInterface) {
        this.requestInterface = requestInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
