package com.lrhealth.bcos.blockchainlogger.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author YangChao
 * @create 2020-11-19 9:35
 **/
public class RestTemplateUtil {

    /**
     * 类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例 没有绑定关系，而且只有被调用到才会装载，从而实现了延迟加载
     */
    private static class SingletonHolder {

        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static RestTemplate INSTANCE = new RestTemplateBuilder().additionalMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8)).build();

        static {
            try {
                // 添加跳过https签名验证
                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, (X509Certificate[] x509Certificates, String s) -> true);
                SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(builder.build(), new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2", "TLSv1.1"}, null, NoopHostnameVerifier.INSTANCE);
                Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", new PlainConnectionSocketFactory()).register("https", socketFactory).build();

                // 长连接保持，默认30秒
                PoolingHttpClientConnectionManager clientConnectionManager = new PoolingHttpClientConnectionManager(registry, null, null, null, 30, TimeUnit.SECONDS);
                // 总连接数，默认1000
                clientConnectionManager.setMaxTotal(1000);
                // 同路由的并发数，默认1000
                clientConnectionManager.setDefaultMaxPerRoute(1000);
                // 可用空闲连接过期时间,重用空闲连接时会先检查是否空闲时间超过这个时间，如果超过，释放socket重新建立
                clientConnectionManager.setValidateAfterInactivity(30 * 1000);

                HttpClientBuilder httpClientBuilder = HttpClients.custom();
                httpClientBuilder.setConnectionManager(clientConnectionManager);

                // 保持长连接配置，需要在头添加Keep-Alive
                httpClientBuilder.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE);

                List<Header> headers = new ArrayList<>();
                headers.add(new BasicHeader("Connection", "keep-alive"));
                httpClientBuilder.setDefaultHeaders(headers);
                httpClientBuilder.setSSLSocketFactory(socketFactory).setConnectionManager(clientConnectionManager).setConnectionManagerShared(true);

                HttpClient httpClient = httpClientBuilder.build();

                // httpClient连接配置，底层是配置RequestConfig
                HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
                // 连接超时
                clientHttpRequestFactory.setConnectTimeout(30000);
                // 数据读取超时时间，即SocketTimeout
                clientHttpRequestFactory.setReadTimeout(30000);
                // 连接不够用的等待时间，不宜过长，必须设置，比如连接不够用时，时间过长将是灾难性的
                clientHttpRequestFactory.setConnectionRequestTimeout(200);

                INSTANCE.setRequestFactory(clientHttpRequestFactory);
                INSTANCE.setErrorHandler(new DefaultResponseErrorHandler());
            } catch (Exception e) {

            }
        }
    }

    /**
     * 获取单例
     */
    public static RestTemplate getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * get请求
     *
     * @param url
     * @return
     */
    public static String get(String url, Map<String, Object> paramsMap) {
        ResponseEntity<String> response = exchange(url, HttpMethod.GET, paramsMap);
        if (null == response) {
            return null;
        }
        return response.getBody();
    }

    /**
     * get请求
     *
     * @param url
     * @return
     */
    public static <S> S get(String url, Map<String, Object> paramsMap, Class<S> responseType) {
        String body = get(url, paramsMap);
        if (StrUtil.isEmpty(body)) {
            return null;
        }
        S result = null;
        try {
            result = JSON.parseObject(body, responseType);
        } catch (Exception exception) {

        }
        return result;
    }

    /**
     * post 请求
     *
     * @param url
     * @param body
     * @param <T>
     * @return
     */
    public static <T> String post(String url, T body) {
        ResponseEntity<String> response = exchange(url, HttpMethod.POST, body, null, String.class);
        if (null == response) {
            return null;
        }
        return response.getBody();
    }

    /**
     * post 请求
     *
     * @param url
     * @param body
     * @param responseType
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T> S post(String url, T body, Class<S> responseType) {
        String response = post(url, body);
        if (null == response) {
            return null;
        }
        S result = null;
        try {
            result = JSON.parseObject(response, responseType);
        } catch (Exception exception) {

        }
        return result;
    }

    /**
     * http请求
     *
     * @param url
     * @param httpMethod
     * @param body
     * @return
     */
    public static <T> ResponseEntity<String> exchange(String url, HttpMethod httpMethod, T body) {
        return exchange(url, httpMethod, body, null, String.class);
    }

    /**
     * http请求
     *
     * @param url
     * @param httpMethod
     * @param body
     * @param headers
     * @return
     */
    public static <T> ResponseEntity<String> exchange(String url, HttpMethod httpMethod, T body, HttpHeaders headers) {
        return exchange(url, httpMethod, body, headers, String.class);
    }

    /**
     * http请求
     *
     * @param url
     * @param httpMethod
     * @param body
     * @param headers
     * @return
     */
    public static <S, T> ResponseEntity<S> exchange(String url, HttpMethod httpMethod, T body, HttpHeaders headers, Class<S> responseType) {
        HttpEntity<String> requestEntity;
        if (null == headers) {
            headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        if (httpMethod.matches(HttpMethod.GET.name())) {
            requestEntity = new HttpEntity<>(headers);
            if (body instanceof Map) {
                url = getUrl(url, (Map<String, Object>) body);
            }
        } else {
            requestEntity = new HttpEntity<>(JSON.toJSONString(body), headers);
        }
        return exchange(url, httpMethod, requestEntity, responseType);
    }

    /**
     * http请求
     *
     * @param url
     * @param httpMethod
     * @return
     */
    public static <S> ResponseEntity<S> exchange(String url, HttpMethod httpMethod, HttpEntity<?> requestEntity, Class<S> responseType) {
        ResponseEntity<S> response = null;
        try {
            response = getInstance().exchange(url, httpMethod, requestEntity, responseType);
        } catch (HttpClientErrorException | HttpServerErrorException httpClientErrorException) {
            HttpStatus statusCode = httpClientErrorException.getStatusCode();

            String bodyAsString = httpClientErrorException.getResponseBodyAsString();

            response = ResponseEntity.status(statusCode).body(JSON.parseObject(bodyAsString, responseType));
        } catch (Exception exception) {

        }
        return response;
    }


    /**
     * 拼接url + 上送参数，返回完整的url请求
     *
     * @param url
     * @param paramMap
     * @return
     */
    public static String getUrl(String url, Map<String, Object> paramMap) {
        if (MapUtil.isEmpty(paramMap)) {
            return url;
        }
        Object value;
        List<String> list = CollectionUtil.newArrayList();
        for (String key : paramMap.keySet()) {
            value = paramMap.get(key);
            if (null == value) {
                continue;
            }
            list.add(key + "=" + join(value));
        }
        return build(url, "?", CollectionUtil.join(list, "&"));
    }

    public static String join(Object object) {
        if (object instanceof Collection) {
            return CollectionUtil.join((Collection) object, ",");
        } else if (object instanceof Arrays) {
            return ArrayUtil.join(object, ",");
        }
        return StrUtil.toString(object);
    }

    public static String build(CharSequence... charSequences) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence charSequence : charSequences) {
            sb.append(charSequence);
        }
        return sb.toString();
    }

}