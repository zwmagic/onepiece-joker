package com.south.onepiece.joker.util;

import com.alibaba.fastjson.JSON;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP GET POST 请求工具类
 *
 * @author zhangwenming
 * @date 2016/10/26 15:40
 * version: 1.0
 */
@Component
public class HttpClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    private static RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

    private CloseableHttpClient httpClient;

    private int connectTimeout;
    private int connectionRequestTimeout;
    private int socketTimeout;


    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        requestConfigBuilder.setConnectTimeout(this.connectTimeout);
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
        requestConfigBuilder.setConnectionRequestTimeout(this.connectionRequestTimeout);
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        requestConfigBuilder.setSocketTimeout(this.socketTimeout);
    }

    private RequestConfig newRequestConfig(int socketTimeout) {
        if (socketTimeout == -1) {
            return requestConfigBuilder.build();
        }
        return newRequestConfig(this.connectTimeout, this.connectionRequestTimeout, socketTimeout);
    }

    private RequestConfig newRequestConfig(int connectTimeout, int connectionRequestTimeout, int socketTimeout) {

        return RequestConfig.custom().setConnectTimeout(connectTimeout != -1 ? connectTimeout : this.connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout != -1 ? connectionRequestTimeout : this.connectionRequestTimeout)
                .setSocketTimeout(socketTimeout != -1 ? socketTimeout : this.socketTimeout)
                .build();
    }


    /**
     * 执行get请求,200返回响应内容，其他状态码返回null
     *
     * @param url
     * @return
     */
    public String doGet(String url) {
        return doGet(url, -1);
    }

    /**
     * 执行带有参数的get请求
     *
     * @param url
     * @param paramMap
     * @return
     */
    public String doGet(String url, Map<String, String> paramMap) {
        return doGet(url, paramMap, -1);
    }

    /**
     * @param url
     * @param paramMap
     * @param timeout
     * @return
     */
    public String doGet(String url, Map<String, String> paramMap, int timeout) {
        String urlpath = null;
        URIBuilder builder = null;
        try {
            builder = new URIBuilder(url);
            for (String s : paramMap.keySet()) {
                builder.addParameter(s, paramMap.get(s));
            }
            urlpath = builder.build().toString();
        } catch (URISyntaxException e) {
            logger.error("发送http GET 请求构建参数失败，URL:{}, 参数：{}, 信息：{}", url, JSON.toJSONString(paramMap), e.getMessage());
            e.printStackTrace();
        }
        return doGet(urlpath, timeout);
    }

    /**
     * @param url
     * @param timeout
     * @return
     */
    public String doGet(String url, int timeout) {
        if (logger.isDebugEnabled()) {
            logger.debug("请求URL:{}", url);
        }

        //创建httpClient对象
        CloseableHttpResponse response = null;
        HttpGet httpGet = new HttpGet(url);
        //设置请求参数
        httpGet.setConfig(newRequestConfig(timeout));
        try {
            //执行请求
            response = httpClient.execute(httpGet);
            //判断返回状态码是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (IOException e) {
            logger.error("发送http GET 请求失败，URL:{}, 信息：{}", url, e.getMessage());
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * 执行post请求
     *
     * @param url
     * @return
     */
    public String doPost(String url) {
        return doPost(url, null);
    }


    /**
     * 执行post请求
     *
     * @param url
     * @param paramMap
     * @return
     */
    public String doPost(String url, Map<String, String> paramMap) {
        return doPost(url, paramMap, -1);
    }

    /**
     * 执行post请求
     *
     * @param url
     * @param paramMap
     * @param timeout
     * @return
     */
    public String doPost(String url, Map<String, String> paramMap, int timeout) {
        if (logger.isDebugEnabled()) {
            logger.debug("请求URL:{}", url);
        }

        HttpPost httpPost = new HttpPost(url);
        //设置请求参数
        httpPost.setConfig(newRequestConfig(timeout));
        if (paramMap != null) {
            List<NameValuePair> parameters = new ArrayList<>();
            for (String s : paramMap.keySet()) {
                parameters.add(new BasicNameValuePair(s, paramMap.get(s)));
            }
            //构建一个form表单式的实体
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters, Charset.forName("UTF-8"));
            //将请求实体放入到httpPost中
            httpPost.setEntity(formEntity);
        }
        //创建httpClient对象
        CloseableHttpResponse response = null;
        try {
            //执行请求
            response = httpClient.execute(httpPost);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            logger.error("发送http POST 请求构建参数失败，状态: {}, URL:{}, 参数：{}, 信息：{}", response == null ? null : response.getStatusLine().getStatusCode(), url, JSON.toJSONString(paramMap, true), e.getMessage());
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
