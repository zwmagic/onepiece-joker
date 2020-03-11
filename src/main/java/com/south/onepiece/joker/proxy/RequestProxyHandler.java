package com.south.onepiece.joker.proxy;

import com.south.onepiece.joker.annotation.IRequestMethod;
import com.south.onepiece.joker.annotation.IRequestParam;
import com.south.onepiece.joker.entity.BaseAnnotation;
import com.south.onepiece.joker.entity.IRequestHost;
import com.south.onepiece.joker.entity.ParamsEntity;
import com.south.onepiece.joker.enums.RequestResultEnum;
import com.south.onepiece.joker.enums.RequestTypeEnum;
import com.south.onepiece.joker.util.ClassUtil;
import com.south.onepiece.joker.util.HttpClientUtil;
import com.south.onepiece.joker.util.JsonSerializer;
import com.south.onepiece.joker.util.ReflectorUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请求切面代理
 *
 * @author zhangwenming
 * @date 2016/10/20 16:13
 * version: 1.0
 */
public class RequestProxyHandler implements InvocationHandler, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestProxyHandler.class);

    private static IRequestMethod defaultMethodParams;

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private IRequestHost requestHost;

    private IRequestMethod getDefaltMethodParams() {
        if (defaultMethodParams != null) {
            return defaultMethodParams;
        }
        try {
            return BaseAnnotation.class.getMethod("requestMethod").getAnnotation(IRequestMethod.class);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException("获取默认注解失败");
        }

    }

    private Class<?> getListRealType(String genericReturnType) {
        String realType = genericReturnType.replace("java.util.List<", "").replace(">", "");
        try {
            return Class.forName(realType);
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取方法的传送参数list
     *
     * @param parameters
     * @param method
     * @param iRequest
     * @return
     * @throws Exception
     */
    private List<ParamsEntity> getParamsEntityList(Object[] parameters, Method method, IRequestMethod iRequest) throws Exception {
        Annotation[][] annotations = method.getParameterAnnotations();
        List<ParamsEntity> list = new ArrayList<ParamsEntity>();
        if (parameters != null) {
            for (int i = 0, le = parameters.length; i < le; i++) {
                Object parameter = parameters[i];
                if (parameter == null) {
                    addJavaClassValue(annotations[i], parameter, iRequest, list);
                } else {
                    if (ClassUtil.isJavaClass(parameter.getClass())) {
                        addJavaClassValue(annotations[i], parameter, iRequest, list);
                    } else {
                        addUserClassValue(annotations[i], parameter, iRequest, list);
                    }
                }
            }
        }
        return list;
    }

    /**
     * 添加用户自定义类型参数
     *
     * @param annotations
     * @param object
     * @param iRequest
     * @param list
     * @throws Exception
     */
    private void addUserClassValue(Annotation[] annotations, Object object, IRequestMethod iRequest, List<ParamsEntity> list) throws Exception {
        ParamsEntity prefix = new ParamsEntity();
        for (int k = 0, ale = annotations.length; k < ale; k++) {
            if (annotations[k] instanceof IRequestParam) {
                prefix.setName(((IRequestParam) annotations[k]).value());
                prefix.setOrder(((IRequestParam) annotations[k]).order());
            }
        }
        addUserClassValueRecycle(prefix, object, list, iRequest);
    }

    /**
     * 循环入参
     *
     * @param prefix
     * @param object
     * @param list
     * @param iRequest
     * @throws Exception
     */
    private void addUserClassValueRecycle(ParamsEntity prefix, Object object, List<ParamsEntity> list, IRequestMethod iRequest) throws Exception {
        ReflectorUtil reflectorUtil = ReflectorUtil.fromCache(object.getClass());
        Field filed;
        ParamsEntity entity;
        for (int i = 0; i < reflectorUtil.getFieldList().size(); i++) {
            filed = reflectorUtil.getFieldList().get(i);
            entity = new ParamsEntity();
            if (filed.getAnnotation(IRequestParam.class) != null) {
                IRequestParam params = filed.getAnnotation(IRequestParam.class);
                entity.setName(prefix.getName() + params.value());
                entity.setOrder(params.order());
            } else {
                entity.setName(prefix.getName() + filed.getName());
            }
            if (ClassUtil.isJavaClass(filed.getType())) {
                if (iRequest.isTranscoding()) {
                    entity.setValue(URLEncoder.encode(String.valueOf(reflectorUtil.getValue(object, filed.getName())), iRequest.encode().getValue()));
                } else {
                    entity.setValue(String.valueOf(reflectorUtil.getValue(object, filed.getName())));
                }
                list.add(entity);
            } else {
                addUserClassValueRecycle(entity, reflectorUtil.getValue(object, filed.getName()), list, iRequest);
            }
        }
    }

    /**
     * 添加java 自带类型参数
     *
     * @param annotations
     * @param parameters
     * @param iRequest
     * @param list
     * @throws Exception
     */
    private void addJavaClassValue(Annotation[] annotations, Object parameters, IRequestMethod iRequest, List<ParamsEntity> list) throws Exception {
        ParamsEntity entity = new ParamsEntity();
        for (int k = 0, ale = annotations.length; k < ale; k++) {
            if (annotations[k] instanceof IRequestParam) {
                entity.setName(((IRequestParam) annotations[k]).value());
                entity.setOrder(((IRequestParam) annotations[k]).order());
            }
        }
        if (StringUtils.isEmpty(entity.getName())) {
            throw new RuntimeException("这个参数没有注释名称:" + parameters.toString());
        }
        if (parameters == null) {
            entity.setValue(null);
        } else {
            entity.setValue(String.valueOf(parameters));
        }
        list.add(entity);
    }


    private String sendRequestUrl(IRequestMethod iRequest, List<ParamsEntity> paramsEntityList) throws Exception {

        String baseUrl = null;
        if (iRequest != null && StringUtils.isNotBlank(iRequest.path())) {
            String urlpath = iRequest.path();
            // 如果 path 直接以http:// 开头，则直接作为 url 发送请求
            if (urlpath.startsWith("http://") || urlpath.startsWith("https://")) {
                baseUrl = urlpath;
            } else {
                // 如果 path 不是直接的url, 需要查询 host，两个进行拼装组成 完整的url
                // 如果 host() 为空，则取默认的值
                // 如果 host() 存在值，作为key，去查询对应的value
                String hostpath;
                if (StringUtils.isNotBlank(iRequest.host())) {
                    hostpath = requestHost.getHost(iRequest.host());
                } else {
                    hostpath = requestHost.getHost();
                }
                baseUrl = hostpath + iRequest.path();
            }
        }

        if (StringUtils.isBlank(baseUrl)) {
            return null;
        }

        int timeout = iRequest.connectTimeout() * 1000;
        String encode = iRequest.encode().getValue();

        if (iRequest.type().equals(RequestTypeEnum.POST)) {
            return sendHttpPost(baseUrl, timeout, encode, paramsEntityList);
        } else if (iRequest.type().equals(RequestTypeEnum.GET)) {
            return sendHttpGet(baseUrl, timeout, encode, paramsEntityList);
        }
        return null;
    }

    /**
     * 获取数据get请求
     *
     * @param url
     * @param timeout
     * @param encode
     * @param paramsEntityList
     * @return
     * @throws IOException
     */
    private String sendHttpGet(String url, int timeout, String encode, List<ParamsEntity> paramsEntityList) throws IOException {

        Map<String, String> paramMap = new HashMap<>();
        for (ParamsEntity param : paramsEntityList) {
            paramMap.put(param.getName(), param.getValue());
        }
        return httpClientUtil.doGet(url, paramMap, timeout);
    }

    /**
     * 获取数据 post请求
     *
     * @param url
     * @param timeout
     * @param encode
     * @param paramsEntityList
     * @return
     */
    private String sendHttpPost(String url, int timeout, String encode, List<ParamsEntity> paramsEntityList) {

        Map<String, String> paramMap = new HashMap<>();
        for (ParamsEntity param : paramsEntityList) {
            paramMap.put(param.getName(), param.getValue());
        }

        return httpClientUtil.doPost(url, paramMap, timeout);
    }


    private Object parserResponseResult(Method method, String responseResult, IRequestMethod iRequest) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("responseResult is  : {}", responseResult);
        }
        if (responseResult == null) {
            return null;
        }
        Class<?> returnType = method.getReturnType();
        if (returnType != null) {
            if (returnType.isAssignableFrom(String.class)) {
                return responseResult;
            }
            // 基本类型
            if (returnType.isPrimitive()) {
                Number number = JsonSerializer.parseJson(responseResult, BigDecimal.class);
                if ("int".equals(returnType.getSimpleName())) {
                    return number.intValue();
                }
                if ("long".equals(returnType.getSimpleName())) {
                    return number.longValue();
                }
                if ("double".equals(returnType.getSimpleName())) {
                    return number.doubleValue();
                }
            }
            if (iRequest.result().equals(RequestResultEnum.JSON)) {
                // List类型
                if (returnType.isAssignableFrom(List.class)) {
                    returnType = getListRealType(method.getGenericReturnType().toString());
                    return JsonSerializer.parseJsonList(responseResult, returnType);
                }
                return JsonSerializer.parseJson(responseResult, returnType);
            }
        }
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            IRequestMethod iRequest = method.getAnnotation(IRequestMethod.class);
            if (iRequest == null) {
                iRequest = getDefaltMethodParams();
            }
            List<ParamsEntity> paramsEntityList = getParamsEntityList(args, method, iRequest);
            String responseResult = sendRequestUrl(iRequest, paramsEntityList);
            return parserResponseResult(method, responseResult, iRequest);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

}
