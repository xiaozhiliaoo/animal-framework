package org.lili.forfun.infra.domain.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.Request.Builder;
import org.lili.forfun.infra.domain.config.HttpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class HttpProcessor {

    private static final MediaType JSON_CONTENT_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final String HTTP_PREFIX = "http://";
    private OkHttpClient client;

    @Autowired
    private HttpConfig httpConfig;

    @PostConstruct
    public void init() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(httpConfig.getConnectTimeout(), TimeUnit.SECONDS)
            .writeTimeout(httpConfig.getWriteTimeout(), TimeUnit.SECONDS)
            .readTimeout(httpConfig.getReadTimeout(), TimeUnit.SECONDS)
            .build();
    }

    public void setHttpConfig(HttpConfig httpConfig) {
        this.httpConfig = httpConfig;
    }

    public String get(String url) throws IOException {
        Request request = new Builder()
            .url(url)
            .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public String get(String url, Map<String, String> params) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (!url.startsWith(HTTP_PREFIX)) {
            sb.append(HTTP_PREFIX).append(url).append("?");
        } else {
            sb.append(url).append("?");
        }
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        String url0 = sb.deleteCharAt(sb.length() - 1).toString();
        return get(url0);
    }

    public String get(String url, String payload, String akId, String akSecret) throws UnsupportedEncodingException {
        return request(url, payload, akId, akSecret, "GET");
    }

    public String getWithHead(String url, Map<String, String> headMap) throws IOException {
        Headers headers = Headers.of(headMap);
        Request request = new Builder()
            .url(url)
            .headers(headers)
            .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public String post(String url) {
        String payload = null;
        return post(url, null, payload);
    }

    public String postWithQuery(String url, Map<String, String> params) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (!url.startsWith(HTTP_PREFIX)) {
            sb.append(HTTP_PREFIX).append(url).append("?");
        } else {
            sb.append(url).append("?");
        }
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        String url0 = sb.deleteCharAt(sb.length() - 1).toString();
        return post(url0);
    }

    public String post(String url, Map<String, String> params) {
        FormBody.Builder builder = new FormBody.Builder();
        params.forEach(builder::add);
        RequestBody body = builder.build();
        return post(url, null, body);
    }

    public String post(String url, List<PostData> dataList) {
        FormBody.Builder builder = new FormBody.Builder();
        dataList.forEach(d -> builder.add(d.getKey(), d.getValue()));
        RequestBody body = builder.build();
        return post(url, null, body);
    }

    public String post(String url, String payload) {
        return post(url, null, payload);
    }

    public String post(String url, Map<String, String> headerMap, String payload) {
        RequestBody body = RequestBody.create(JSON_CONTENT_TYPE, payload == null ? "" : payload);
        return post(url, headerMap, body);
    }

    private String post(String url, Map<String, String> headerMap, RequestBody formBody) {
        Builder builder = new Builder()
            .url(url)
            .post(formBody);
        if (headerMap != null) {
            headerMap.forEach(builder::header);
        }
        Request request = builder.build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                return body == null ? "" : body.string();
            } else {
                ResponseBody body = response.body();
                return body == null ? response.message() : body.string();
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return "";
    }

    public String post(String url, String payload, String akId, String akSecret) throws UnsupportedEncodingException {
        return request(url, payload, akId, akSecret, "POST");
    }

    public String postWithHead(String url, Map<String, String> headerMap, Map<String, String> paramMap,
                               String payload) {
        FormBody.Builder builder = new FormBody.Builder();
        paramMap.forEach(builder::add);
        RequestBody body = builder.build();
        return post(url, headerMap, body);
    }

    public String postWithHead(String url, Map<String, String> headerMap, String payload) {
        RequestBody body = RequestBody.create(JSON_CONTENT_TYPE, payload == null ? "" : payload);
        return post(url, headerMap, body);
    }

    public String put(String url, String json) throws IOException {
        if (json == null) {
            log.warn("PUT payload shouldn't be empty.");
            return "";
        }
        RequestBody body = RequestBody.create(JSON_CONTENT_TYPE, json);
        Request request = new Builder()
            .url(url)
            .put(body)
            .build();
        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            return responseBody == null ? "" : responseBody.string();
        }
    }

    public String put(String url, String payload, String akId, String akSecret) throws UnsupportedEncodingException {
        return request(url, payload, akId, akSecret, "PUT");
    }

    public String delete(String url, String json) throws IOException {
        Request request;
        if (json != null) {
            RequestBody body = RequestBody.create(JSON_CONTENT_TYPE, json);
            request = new Builder()
                .url(url)
                .delete(body)
                .build();
        } else {
            request = new Builder()
                .url(url)
                .delete()
                .build();
        }
        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            return responseBody == null ? "" : responseBody.string();
        }
    }

    public String delete(String url, String payload, String akId, String akSecret) throws UnsupportedEncodingException {
        return request(url, payload, akId, akSecret, "DELETE");
    }

    private String request(String url, String payload, String akId, String akSecret, String method)
        throws UnsupportedEncodingException {
        /**
         * http header 参数
         */
        String accept = "application/json";
        String contentType = "application/json";
        String date = toGMTString(new Date());

        // 1.对body做MD5+BASE64加密
        String bodyMd5 = md5Base64(payload);
        String stringToSign = method + "\n" + accept + "\n" + bodyMd5 + "\n" + contentType + "\n" + date;
        // 2.计算 HMAC-SHA1
        String signature = hmacSha1(stringToSign, akSecret);
        // 3.得到 authorization header
        String authHeader = "Dataplus " + akId + ":" + signature;
        RequestBody body = RequestBody.create(JSON_CONTENT_TYPE, payload);
        Builder builder0 = new Builder()
            .url(url)
            .header("accept", accept)
            .header("content-type", contentType)
            .header("date", date)
            .header("Authorization", authHeader);
        Builder builder;
        switch (method) {
            case "DELETE":
                builder = builder0.delete(body);
                break;
            case "POST":
                builder = builder0.post(body);
                break;
            case "PUT":
                builder = builder0.put(body);
                break;
            default:
                builder = builder0.get();
        }
        Request request = builder.build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                return responseBody == null ? "" : responseBody.string();
            } else {
                ResponseBody t = response.body();
                return t == null ? response.message() : t.string();
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return "";
    }

    /**
     * 计算MD5+BASE64
     */
    private static String md5Base64(String s) throws UnsupportedEncodingException {
        if (s == null) {
            return null;
        }
        String encodeStr;

        // string 编码必须为utf-8
        byte[] utfBytes = s.getBytes("UTF-8");

        MessageDigest mdTemp;
        try {
            mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(utfBytes);
            byte[] md5Bytes = mdTemp.digest();
            encodeStr =Base64.getEncoder().encodeToString(md5Bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate MD5 : " + e.getMessage());
        }
        return encodeStr;
    }

    /**
     * 计算 HMAC-SHA1
     */
    private static String hmacSha1(String data, String key) {
        String result;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            result = Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC : " + e.getMessage());
        }
        return result;
    }

    /**
     * 等同于javaScript中的 new Date().toUTCString();
     */
    private static String toGMTString(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.UK);
        df.setTimeZone(new java.util.SimpleTimeZone(0, "GMT"));
        return df.format(date);
    }
}