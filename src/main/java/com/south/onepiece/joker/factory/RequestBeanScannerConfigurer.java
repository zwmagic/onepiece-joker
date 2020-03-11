package com.south.onepiece.joker.factory;

import com.south.onepiece.joker.annotation.IRequest;
import com.south.onepiece.joker.proxy.RequestProxyHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;

/**
 * 扫描配置文件
 *
 * @author zhangwenming
 * @date 2016/10/20 18:39
 * version: 1.0
 */
public class RequestBeanScannerConfigurer implements BeanDefinitionRegistryPostProcessor {
    /**
     * ,; \t\n
     */
    private String basePackage;

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        registerRequestProxyHandler(registry);
        RequestClassPathMapperScanner scanner = new RequestClassPathMapperScanner(registry);
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    /**
     * RequestProxyHandler 手工注册代理类,减去了用户配置XML的烦恼
     *
     * @param registry
     */
    private void registerRequestProxyHandler(BeanDefinitionRegistry registry) {
        GenericBeanDefinition requestProxyDefinition = new GenericBeanDefinition();
        requestProxyDefinition.setBeanClass(RequestProxyHandler.class);
        registry.registerBeanDefinition("requestProxyHandler", requestProxyDefinition);
    }

    /**
     * 请求代理扫描类
     */
    private class RequestClassPathMapperScanner extends ClassPathBeanDefinitionScanner {

        public RequestClassPathMapperScanner(BeanDefinitionRegistry registry) {
            super(registry, false);
            addIncludeFilter(new AnnotationTypeFilter(IRequest.class));
        }

        @Override
        public Set<BeanDefinitionHolder> doScan(String... basePackages) {
            Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

            if (beanDefinitions.isEmpty()) {
                logger.warn("No IRequest mapper was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
            }
            GenericBeanDefinition definition;
            for (BeanDefinitionHolder holder : beanDefinitions) {
                definition = (GenericBeanDefinition) holder.getBeanDefinition();
                definition.getPropertyValues().add("proxy", getRegistry().getBeanDefinition("requestProxyHandler"));
                definition.getPropertyValues().add("requestInterface", definition.getBeanClassName());
                definition.setBeanClass(RequestBeanFactory.class);
            }

            return beanDefinitions;
        }

        /**
         * 默认不允许接口的,这里重写,覆盖下
         */
        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
        }

    }

}
