package com.fengchao.miniapp.utils;

import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.constant.WeChat;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpClient431Util {

    private static final RequestConfig config;

    private static final String DEFAULT_SEND_CHARSET = "UTF-8";
    private static final String DEFAULT_RES_CHARSET = "UTF-8";

    static {
        config = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
    }

    /**
     * HTTP Post 获取内容
     *
     * @param params     请求的参数
     * @param url        请求的url地址 ?之前的地址
     * @param reqCharset 编码格式
     * @param resCharset 编码格式
     * @return 页面内容
     * @throws Exception exception
     */
    public static String doPostJSON(Map<String, String> params, String url,
                                String reqCharset, String resCharset) throws Exception {
        CloseableHttpClient httpClient = getSingleSSLConnection(null);
        CloseableHttpResponse response = null;
        if (isBlank(url)) {
            return null;
        }
        try {
            List<NameValuePair> pairs = null;
            if (params != null && !params.isEmpty()) {
                pairs = new ArrayList<>(params.size());
                for (Map.Entry<String, String> entry : params.entrySet()){
                    String value = entry.getValue();
                    if (value != null) {
                        pairs.add(new BasicNameValuePair(entry.getKey(), value));
                    }
                }
            }
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");
            httpPost.addHeader("Connection", "close");
            if (pairs != null && pairs.size() > 0) {
                httpPost.setEntity(new UrlEncodedFormEntity(pairs, reqCharset == null ? DEFAULT_SEND_CHARSET : reqCharset));
            }

            // 执行请求
            response = httpClient.execute(httpPost);
            if (log.isDebugEnabled()) {
                log.debug("http post 返回:{}", JSONUtil.toJsonString(response));
            }

            //
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                Header header = response.getFirstHeader("location");
                return header.getValue();
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, resCharset == null ? DEFAULT_RES_CHARSET : resCharset);
            }
            EntityUtils.consume(entity);

            return result;
        } catch (Exception e) {
            log.error("http post 异常:{}", e.getMessage(), e);
            throw new Exception(MyErrorCode.HTTP_ERROR+e.getMessage());
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }

                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.error("http post 关闭资源异常:{}", e.getMessage(), e);
            }
        }
    }

    public static String doPostSSLXml(String params, String url) throws Exception{

        if (isBlank(url)) {
            throw new Exception(MyErrorCode.ADDRESS_BLANK);
        }

        return wxRefundLink(params, url);
    }

    public static String doPostXml(String params, String url) {

        String _func = "doPostXml";
        CloseableHttpClient httpClient;
        CloseableHttpResponse response = null;
        if (isBlank(url)) {
            throw new RuntimeException(MyErrorCode.ADDRESS_BLANK);
        }
        String result = null;
        try {
            httpClient = getSingleSSLConnection(null);
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");
            httpPost.addHeader("Connection", "close");
            httpPost.addHeader("Content-Type", "text/xml");

            StringEntity stringEntity = new StringEntity(params, StandardCharsets.UTF_8);
            stringEntity.setContentEncoding("UTF-8");
            httpPost.setEntity(stringEntity);

            // 执行请求
            try {
                response = httpClient.execute(httpPost);
            }catch (Exception e){
                log.error("{} {}",_func,e.getMessage(),e);
                throw new RuntimeException(MyErrorCode.HTTP_ERROR + e.getMessage());
            }
            if (null == response || null == response.getStatusLine()) {
                log.error("{} {} no response", _func,url);
                throw new RuntimeException(MyErrorCode.HTTP_NO_RESPONSE);
            }
            int statusCode = response.getStatusLine().getStatusCode();
            if (log.isDebugEnabled()) {
                log.debug("{} http post 返回:{},{}", _func, statusCode, JSONUtil.toJsonString(response));
            }
            if (statusCode != 200) {
                Header header = response.getFirstHeader("location");
                log.info("{} http post 返回:{},{}", _func, statusCode, header.getValue());
                return header.getValue();
            }
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                result = EntityUtils.toString(entity, DEFAULT_RES_CHARSET);
                EntityUtils.consume(entity);
            }
            log.info("http post 返回:{},{}", statusCode, result);

        } catch (Exception e) {
            String msg = MyErrorCode.HTTP_ERROR + e.getMessage();
            log.error("{} http post 异常:{}", _func, e.getMessage(), e);
            throw new RuntimeException(msg);
        }
        try {
            httpClient.close();
            response.close();
        } catch (IOException e) {
            log.info("{} http post 关闭资源异常:{}", _func,e.getMessage(), e);
        }

        return result;
    }

    public static String doGet(Map<String, String> params, String url) throws Exception {
        String _func = "Http_doGet ";
        //if (log.isDebugEnabled()) {
            log.info("{} 参数 {}", _func, JSON.toJSONString(params));
        //}
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        if (isBlank(url)) {
            return null;
        }

        URIBuilder uriBuilder = new URIBuilder(url);
        try {
            httpClient = getSingleSSLConnection(null);
            List<NameValuePair> pairs = null;
            if (params != null && !params.isEmpty()) {
                pairs = new ArrayList<>(params.size());
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    String value = entry.getValue();
                    if (value != null) {
                        pairs.add(new BasicNameValuePair(entry.getKey(), value));
                    }
                }
            }
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");
            httpGet.addHeader("Connection", "close");

            if (pairs != null && pairs.size() > 0) {
                uriBuilder.addParameters(pairs);
            }
            httpGet.setURI(uriBuilder.build());
            if (log.isDebugEnabled()) {
                log.info("http Get 参数： {}", JSON.toJSONString(httpGet));
            }
            // 执行请求
            response = httpClient.execute(httpGet);
            if (log.isDebugEnabled()) {
                log.debug("http Get 返回:{}", JSONUtil.toJsonString(response));
            }
            //
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                Header header = response.getFirstHeader("location");
                return header.getValue();
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, DEFAULT_RES_CHARSET);
            }
            EntityUtils.consume(entity);
            if (log.isDebugEnabled()) {
                log.info("http Get entity = {}", JSON.toJSONString(result));
            }
            return result;
        } catch (Exception e) {
            log.info("http_doGet {} 异常:{}", url,e.getMessage(), e);
            throw new Exception(MyErrorCode.HTTP_ERROR+e.getMessage());
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }

                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.info("http_doGet 关闭资源异常:{}", e.getMessage(), e);
            }
        }
    }

    private static KeyStore getCertificate() throws Exception {
        //try-with-resources 关流
        InputStream in;
        KeyStore keyStore;

        try {
            in = HttpClient431Util.class.getClassLoader().getResourceAsStream(WeChat.MINI_APP_CERT_FILE);
            keyStore = KeyStore.getInstance("PKCS12");

            // 指定PKCS12的密码(商户ID)
            keyStore.load(in, WeChat.MINI_APP_PAYMENT_MCH_ID.toCharArray());
        } catch (Exception e) {
            log.error("获取证书失败 {}", e.getMessage(), e);
            throw e;
        }
        log.info("{} 获取证书完成");
        return keyStore;
    }

    //微信退款 双向SSL通道
    private static String  wxRefundLink(String postDataXML,String url) throws Exception{
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        try{
            //获取微信退款证书
            KeyStore keyStore = getCertificate();
            SSLContext sslContext = SSLContexts.custom().loadKeyMaterial(keyStore, WeChat.MINI_APP_PAYMENT_MCH_ID.toCharArray()).build();
            SSLConnectionSocketFactory sslf = new SSLConnectionSocketFactory(sslContext);
            httpClient = HttpClients.custom().setSSLSocketFactory(sslf).build();
            HttpPost httpPost = new HttpPost(url);//退款接口

            StringEntity reqEntity = new StringEntity(postDataXML);
            // 设置类型
            reqEntity.setContentType("application/x-www-form-urlencoded");
            httpPost.setEntity(reqEntity);
            httpResponse = httpClient.execute(httpPost);

            if (null == httpResponse || null == httpResponse.getStatusLine()){
                String msg = url+" post "+MyErrorCode.HTTP_NO_RESPONSE;
                log.error("doPostXml {}",msg);
                throw new Exception(msg);
            }
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (log.isDebugEnabled()) {
                log.debug("http post 返回:{},{}", statusCode, JSONUtil.toJsonString(httpResponse));
            }
            if (statusCode != 200) {
                Header header = httpResponse.getFirstHeader("location");
                log.info("http post 返回:{},{}", statusCode,header.getValue());
                return header.getValue();
            }
            HttpEntity entity = httpResponse.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, DEFAULT_RES_CHARSET);
                EntityUtils.consume(entity);
            }
            log.info("http post 返回:{},{}", statusCode,result);
            return result;

        }finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }

                if (httpResponse != null) {
                    httpResponse.close();
                }
            } catch (IOException e) {
                log.info("http post 关闭资源异常:{}", e.getMessage(), e);
            }
        }

    }

    /**
     * 创建单向ssl的连接
     *
     * @return CloseableHttpClient
     * @throws Exception exception
     */
    private static CloseableHttpClient getSingleSSLConnection(KeyStore truststore) throws Exception {
        CloseableHttpClient httpClient ;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(truststore, /*new TrustStrategy() {                @Override
                public boolean isTrusted(X509Certificate[] paramArrayOfX509Certificate,
                                         String paramString) throws CertificateException {
                    return true;
                }
            }*/(X509Certificate[] paramArrayOfX509Certificate,String paramString)->true).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultRequestConfig(config).build();
            return httpClient;
        } catch (Exception e) {
            log.error("{}",e.getMessage(),e);
            throw e;
        }
    }

    private static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
