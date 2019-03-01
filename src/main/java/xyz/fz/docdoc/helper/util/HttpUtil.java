package xyz.fz.docdoc.helper.util;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by fz on 2015/8/30.
 */
public class HttpUtil {

    static {
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final MediaType XML = MediaType.parse("application/xml; charset=utf-8");

    private static final MediaType JPEG = MediaType.parse("image/jpeg");

    private static final MediaType GIF = MediaType.parse("image/gif");

    private static final MediaType PNG = MediaType.parse("image/png");

    private static final MediaType XLS = MediaType.parse("application/vnd.ms-excel");

    private static final MediaType TXT = MediaType.parse("text/plain");

    private static final MediaType BIN = MediaType.parse("application/octet-stream");

    private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
    };

    private static final SSLContext TRUST_ALL_SSL_CONTEXT;

    static {
        try {
            TRUST_ALL_SSL_CONTEXT = SSLContext.getInstance("SSL");
            TRUST_ALL_SSL_CONTEXT.init(null, TRUST_ALL_CERTS, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private static final SSLSocketFactory TRUST_ALL_SSL_SOCKET_FACTORY = TRUST_ALL_SSL_CONTEXT.getSocketFactory();

    private static OkHttpClient client = new OkHttpClient
            .Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .sslSocketFactory(TRUST_ALL_SSL_SOCKET_FACTORY, (X509TrustManager) TRUST_ALL_CERTS[0])
            .hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            })
            .build();

    public static boolean serverTest(String url) {
        Response ignored = null;
        try {
            String queryUrl = queryUrlBuild(url, null);
            Request request = requestBuild(queryUrl);
            ignored = client.newCall(request).execute();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (ignored != null) {
                ignored.close();
            }
        }
    }

    public static String httpGet(String url, LinkedHashMap queryParams) {
        String queryUrl = queryUrlBuild(url, queryParams);
        Request request = requestBuild(queryUrl);
        logger.debug("Http Get Url: {}", request.url().toString());
        return responseResult(request);
    }

    private static String queryUrlBuild(String url, LinkedHashMap queryParams) {
        StringBuilder urlBuilder = new StringBuilder(url);
        if (queryParams != null) {
            urlBuilder.append("?");
            for (Object o : queryParams.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            url = urlBuilder.toString();
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static String httpPost(String url, Map formParams) {
        FormBody formBody = formBodyBuild(formParams);
        Request request = requestBuild(url, formBody);
        logger.debug("Http Post Url: {}", request.url().toString());
        return responseResult(request);
    }

    private static FormBody formBodyBuild(Map formParams) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        if (formParams != null) {
            for (Object o : formParams.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                formBodyBuilder.add(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return formBodyBuilder.build();
    }

    public static String httpPostJson(String url, String json) {
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request request = requestBuild(url, requestBody);
        logger.debug("Http httpPostJson Url: {}", request.url().toString());
        return responseResult(request);
    }

    public static String httpPostJson(String url, Map<String, String> requestHeaders, String json) {
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request request = requestBuild(url, requestHeaders, requestBody);
        logger.debug("Http httpPostJson Url: {}", request.url().toString());
        return responseResult(request);
    }

    public static String httpPostXml(String url, String xml) {
        RequestBody requestBody = RequestBody.create(XML, xml);
        Request request = requestBuild(url, requestBody);
        logger.debug("Http httpPostXml Url: {}", request.url().toString());
        return responseResult(request);
    }

    public static String httpUpload(String url, Map<String, Object> params) {
        MultipartBody multipartBody = multipartBodyBuild(params);
        Request request = requestBuild(url, multipartBody);
        logger.debug("Http Upload Url: {}", request.url().toString());
        return responseResult(request);
    }

    private static MultipartBody multipartBodyBuild(Map<String, Object> params) {
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (Map.Entry entry : params.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            if (value instanceof File) {
                File file = (File) value;
                addFile(multipartBodyBuilder, key, file.getName(), file);
            } else {
                multipartBodyBuilder.addFormDataPart(key, value.toString());
            }
        }
        return multipartBodyBuilder.build();
    }

    private static void addFile(MultipartBody.Builder multipartBodyBuilder, String key, String fileName, File file) {
        try {
            String suffix = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
            switch (suffix) {
                case "jpeg":
                    multipartBodyBuilder.addFormDataPart(key, fileName, RequestBody.create(JPEG, file));
                    break;
                case "jpg":
                    multipartBodyBuilder.addFormDataPart(key, fileName, RequestBody.create(JPEG, file));
                    break;
                case "gif":
                    multipartBodyBuilder.addFormDataPart(key, fileName, RequestBody.create(GIF, file));
                    break;
                case "png":
                    multipartBodyBuilder.addFormDataPart(key, fileName, RequestBody.create(PNG, file));
                    break;
                case "xls":
                    multipartBodyBuilder.addFormDataPart(key, fileName, RequestBody.create(XLS, file));
                    break;
                case "txt":
                    multipartBodyBuilder.addFormDataPart(key, fileName, RequestBody.create(TXT, file));
                    break;
                default:
                    multipartBodyBuilder.addFormDataPart(key, fileName, RequestBody.create(BIN, file));
                    break;
            }
        } catch (Exception e) {
            multipartBodyBuilder.addFormDataPart(key, fileName, RequestBody.create(BIN, file));
        }
    }

    private static Request requestBuild(String url) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        return requestBuilder.build();
    }

    private static Request requestBuild(String url, RequestBody requestBody) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        requestBuilder.post(requestBody);
        return requestBuilder.build();
    }

    private static Request requestBuild(String url, Map<String, String> requestHeaders, RequestBody requestBody) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        for (Map.Entry entry : requestHeaders.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            requestBuilder.addHeader(key, value);
        }
        requestBuilder.post(requestBody);
        return requestBuilder.build();
    }

    private static String responseResult(Request request) {
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    return responseBody.string();
                }
            }
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
        }
        return "";
    }

    public static OkHttpClient twoWaySslClient(String caPath, String password) {
        InputStream inputStream = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            inputStream = new FileInputStream(caPath);
            keyStore.load(inputStream, password.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, password.toCharArray());
            KeyManager[] kms = kmf.getKeyManagers();

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] tms = trustManagerFactory.getTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kms, tms, new SecureRandom());
            return new OkHttpClient
                    .Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) tms[0])
                    .build();
        } catch (Exception e) {
            logger.error("创建wxRefundClient失败: {}", e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static class OkHttpResponseFuture implements Callback {
        public final CompletableFuture<Response> future = new CompletableFuture<>();

        @Override
        public void onFailure(Call call, IOException e) {
            future.completeExceptionally(e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            future.complete(response);
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        LinkedHashMap params = new LinkedHashMap();
        params.put("wd", "baidu");
        System.out.println(httpGet("http://www.baidu.com/s", params));
    }
}
