package com.example.testcdc.httpServer;

import static com.example.testcdc.Utils.Utils.blfGetAnalysisByParams;
import static com.example.testcdc.Utils.Utils.blfthaveDataSignal;
import static com.example.testcdc.Utils.Utils.parseBlfByPython;
import static com.example.testcdc.Utils.Utils.parseDBCforBlf;
import static com.example.testcdc.Utils.Utils.parseDBCforBlf1;
import static com.example.testcdc.Utils.Utils.reAdjust;

import android.util.Log;

import com.google.common.base.MoreObjects;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {


    private final static String LOG_PREFIX = "HttpServer";
    private String carType,sdb;


    public HttpServer(String hostname, int port) {
        super(hostname, port);
        Log.i(LOG_PREFIX, "hi, i am init");
    }

    @Override
    public void start(int timeout, boolean daemon) throws IOException {
        super.start(timeout, daemon);
        Log.i(LOG_PREFIX, "hi, i am running");
    }


    /**
     * 判断是否为CORS 预检请求请求(Preflight)
     *
     * @param session
     * @return
     */
    private static boolean isPreflightRequest(IHTTPSession session) {
        Map<String, String> headers = session.getHeaders();
        return Method.OPTIONS.equals(session.getMethod())
                && headers.containsKey("origin")
                && headers.containsKey("access-control-request-method")
                && headers.containsKey("access-control-request-headers");
    }


    /**
     * 向响应包中添加CORS包头数据
     *
     * @param session
     * @return
     */
    private Response responseCORS(IHTTPSession session) {
        Response resp = wrapResponse(session, newFixedLengthResponse(""));
        Map<String, String> headers = session.getHeaders();
        resp.addHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS");

        String requestHeaders = headers.get("access-control-request-headers");
        //resp.addHeader("Access-Control-Max-Age", "86400");
        resp.addHeader("Access-Control-Max-Age", "0");
        return resp;
    }

    /**
     * Standard HTTP header names.
     */
    public static final class HeaderNames {
        /**
         * {@code "Accept"}
         */
        public static final String ACCEPT = "Accept";
        /**
         * {@code "Accept-Charset"}
         */
        public static final String ACCEPT_CHARSET = "Accept-Charset";
        /**
         * {@code "Accept-Encoding"}
         */
        public static final String ACCEPT_ENCODING = "Accept-Encoding";
        /**
         * {@code "Accept-Language"}
         */
        public static final String ACCEPT_LANGUAGE = "Accept-Language";
        /**
         * {@code "Accept-Ranges"}
         */
        public static final String ACCEPT_RANGES = "Accept-Ranges";
        /**
         * {@code "Accept-Patch"}
         */
        public static final String ACCEPT_PATCH = "Accept-Patch";
        /**
         * {@code "Access-Control-Allow-Credentials"}
         */
        public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
        /**
         * {@code "Access-Control-Allow-Headers"}
         */
        public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
        /**
         * {@code "Access-Control-Allow-Methods"}
         */
        public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
        /**
         * {@code "Access-Control-Allow-Origin"}
         */
        public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
        /**
         * {@code "Access-Control-Expose-Headers"}
         */
        public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
        /**
         * {@code "Access-Control-Max-Age"}
         */
        public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
        /**
         * {@code "Access-Control-Request-Headers"}
         */
        public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
        /**
         * {@code "Access-Control-Request-Method"}
         */
        public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
        /**
         * {@code "Age"}
         */
        public static final String AGE = "Age";
        /**
         * {@code "Allow"}
         */
        public static final String ALLOW = "Allow";
        /**
         * {@code "Authorization"}
         */
        public static final String AUTHORIZATION = "Authorization";
        /**
         * {@code "Cache-Control"}
         */
        public static final String CACHE_CONTROL = "Cache-Control";
        /**
         * {@code "Connection"}
         */
        public static final String CONNECTION = "Connection";
        /**
         * {@code "Content-Base"}
         */
        public static final String CONTENT_BASE = "Content-Base";
        /**
         * {@code "Content-Encoding"}
         */
        public static final String CONTENT_ENCODING = "Content-Encoding";
        /**
         * {@code "Content-Language"}
         */
        public static final String CONTENT_LANGUAGE = "Content-Language";
        /**
         * {@code "Content-Length"}
         */
        public static final String CONTENT_LENGTH = "Content-Length";
        /**
         * {@code "Content-Location"}
         */
        public static final String CONTENT_LOCATION = "Content-Location";
        /**
         * {@code "Content-Transfer-Encoding"}
         */
        public static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
        /**
         * {@code "Content-MD5"}
         */
        public static final String CONTENT_MD5 = "Content-MD5";
        /**
         * {@code "Content-Range"}
         */
        public static final String CONTENT_RANGE = "Content-Range";
        /**
         * {@code "Content-Type"}
         */
        public static final String CONTENT_TYPE = "Content-Type";
        /**
         * {@code "Cookie"}
         */
        public static final String COOKIE = "Cookie";
        /**
         * {@code "Date"}
         */
        public static final String DATE = "Date";
        /**
         * {@code "ETag"}
         */
        public static final String ETAG = "ETag";
        /**
         * {@code "Expect"}
         */
        public static final String EXPECT = "Expect";
        /**
         * {@code "Expires"}
         */
        public static final String EXPIRES = "Expires";
        /**
         * {@code "From"}
         */
        public static final String FROM = "From";
        /**
         * {@code "Host"}
         */
        public static final String HOST = "Host";
        /**
         * {@code "If-Match"}
         */
        public static final String IF_MATCH = "If-Match";
        /**
         * {@code "If-Modified-Since"}
         */
        public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
        /**
         * {@code "If-None-Match"}
         */
        public static final String IF_NONE_MATCH = "If-None-Match";
        /**
         * {@code "If-Range"}
         */
        public static final String IF_RANGE = "If-Range";
        /**
         * {@code "If-Unmodified-Since"}
         */
        public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
        /**
         * {@code "Last-Modified"}
         */
        public static final String LAST_MODIFIED = "Last-Modified";
        /**
         * {@code "Location"}
         */
        public static final String LOCATION = "Location";
        /**
         * {@code "Max-Forwards"}
         */
        public static final String MAX_FORWARDS = "Max-Forwards";
        /**
         * {@code "Origin"}
         */
        public static final String ORIGIN = "Origin";
        /**
         * {@code "Pragma"}
         */
        public static final String PRAGMA = "Pragma";
        /**
         * {@code "Proxy-Authenticate"}
         */
        public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
        /**
         * {@code "Proxy-Authorization"}
         */
        public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
        /**
         * {@code "Range"}
         */
        public static final String RANGE = "Range";
        /**
         * {@code "Referer"}
         */
        public static final String REFERER = "Referer";
        /**
         * {@code "Retry-After"}
         */
        public static final String RETRY_AFTER = "Retry-After";
        /**
         * {@code "Sec-WebSocket-Key1"}
         */
        public static final String SEC_WEBSOCKET_KEY1 = "Sec-WebSocket-Key1";
        /**
         * {@code "Sec-WebSocket-Key2"}
         */
        public static final String SEC_WEBSOCKET_KEY2 = "Sec-WebSocket-Key2";
        /**
         * {@code "Sec-WebSocket-Location"}
         */
        public static final String SEC_WEBSOCKET_LOCATION = "Sec-WebSocket-Location";
        /**
         * {@code "Sec-WebSocket-Origin"}
         */
        public static final String SEC_WEBSOCKET_ORIGIN = "Sec-WebSocket-Origin";
        /**
         * {@code "Sec-WebSocket-Protocol"}
         */
        public static final String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
        /**
         * {@code "Sec-WebSocket-Version"}
         */
        public static final String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
        /**
         * {@code "Sec-WebSocket-Key"}
         */
        public static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
        /**
         * {@code "Sec-WebSocket-Accept"}
         */
        public static final String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
        /**
         * {@code "Server"}
         */
        public static final String SERVER = "Server";
        /**
         * {@code "Set-Cookie"}
         */
        public static final String SET_COOKIE = "Set-Cookie";
        /**
         * {@code "Set-Cookie2"}
         */
        public static final String SET_COOKIE2 = "Set-Cookie2";
        /**
         * {@code "TE"}
         */
        public static final String TE = "TE";
        /**
         * {@code "Trailer"}
         */
        public static final String TRAILER = "Trailer";
        /**
         * {@code "Transfer-Encoding"}
         */
        public static final String TRANSFER_ENCODING = "Transfer-Encoding";
        /**
         * {@code "Upgrade"}
         */
        public static final String UPGRADE = "Upgrade";
        /**
         * {@code "User-Agent"}
         */
        public static final String USER_AGENT = "User-Agent";
        /**
         * {@code "Vary"}
         */
        public static final String VARY = "Vary";
        /**
         * {@code "Via"}
         */
        public static final String VIA = "Via";
        /**
         * {@code "Warning"}
         */
        public static final String WARNING = "Warning";
        /**
         * {@code "WebSocket-Location"}
         */
        public static final String WEBSOCKET_LOCATION = "WebSocket-Location";
        /**
         * {@code "WebSocket-Origin"}
         */
        public static final String WEBSOCKET_ORIGIN = "WebSocket-Origin";
        /**
         * {@code "WebSocket-Protocol"}
         */
        public static final String WEBSOCKET_PROTOCOL = "WebSocket-Protocol";
        /**
         * {@code "WWW-Authenticate"}
         */
        public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

        private HeaderNames() {
        }
    }


    /**
     * 封装响应包
     *
     * @param session http请求
     * @param resp    响应包
     * @return resp
     */
    private Response wrapResponse(IHTTPSession session, Response resp) {
        if (null != resp) {
            Map<String, String> headers = session.getHeaders();
            resp.addHeader(HeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            // 如果请求头中包含'Origin',则响应头中'Access-Control-Allow-Origin'使用此值否则为'*'
            // nanohttd将所有请求头的名称强制转为了小写
            String origin = MoreObjects.firstNonNull(headers.get(HeaderNames.ORIGIN.toLowerCase()), "*");
            resp.addHeader(HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, origin);

            String requestHeaders = headers.get(HeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toLowerCase());
            if (requestHeaders != null) {
                resp.addHeader(HeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders);
            }
        }
        return resp;
    }

    private static final String getDBC = "/getDBC";

    private static final String getBLFdata = "/getBLFdata";

    private static final String getAnalysisByParams = "/getAnalysisByParams";

    private static final String reAdjust = "/reAdjust";

    private static final String blfthaveDataSignal = "/blfthaveDataSignal";

    private static final String getDBC1 = "/getDBC1";

    @Override
    public Response serve(IHTTPSession session) {


        // 判断是否为跨域预请求
        if (isPreflightRequest(session)) {
            // 如果是则发送CORS响应告诉浏览HTTP服务支持的METHOD及HEADERS和请求源
            return responseCORS(session);
        }

        String uri = session.getUri();
        //是否接收到http请求
        Log.d("network", session.toString() + "http win!!!!!!!");

        Method method = session.getMethod();
        if (method.equals(Method.POST)) {
            String ret;
            Map<String, String> params = new HashMap<String, String>();
            switch (uri) {
                case getDBC1:
                    Log.e("HTTP", "getDBC run ");
                    try {
                        session.parseBody(params);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ResponseException e) {
                        throw new RuntimeException(e);
                    }
                    String postDataStr = params.get("postData");

                    try {
                        // 将 postDataStr 转换为 JSONObject
                        JSONObject jsonObject = new JSONObject(postDataStr);

                        // 提取 carType和sdb 字段
                        String carType = jsonObject.getString("carType");
                        String sdb = jsonObject.getString("sdb");

                        Log.e("HTTP","getDBC running" );
                        ret = parseDBCforBlf1(carType, sdb);

                        Log.e("HTTP", "getDBC finish");

                        return wrapResponse(session, newFixedLengthResponse(Response.Status.OK, "application/json", ret));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                case getDBC:
                    Log.e("HTTP", "getDBC run ");
                    try {
                        session.parseBody(params);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ResponseException e) {
                        throw new RuntimeException(e);
                    }
                    String postDataStr1 = params.get("postData");

                    try {
                        // 将 postDataStr 转换为 JSONObject
                        JSONObject jsonObject = new JSONObject(postDataStr1);

                        // 提取 carType和sdb 字段
                        carType = jsonObject.getString("carType");
                        sdb = jsonObject.getString("sdb");

                        Log.e("HTTP","getDBC running" );
                        ret = parseDBCforBlf(carType, sdb);

                        Log.e("HTTP", "getDBC finish");

                        return wrapResponse(session, newFixedLengthResponse(Response.Status.OK, "application/json", ret));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                case getBLFdata:
                    Log.e("HTTP", "getBLFdata run ");
                    try {
                        session.parseBody(params);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ResponseException e) {
                        throw new RuntimeException(e);
                    }
                    postDataStr = params.get("postData");

                    try {
                        // 将 postDataStr 转换为 JSONObject
                        JSONObject jsonObject = new JSONObject(postDataStr);

                        String blfFile = jsonObject.getString("blfFile");

                        parseDBCforBlf1(carType, sdb);
                        ret = parseBlfByPython(blfFile);
                        Log.e("HTTP", "getBLFdata finish");

                        return wrapResponse(session, newFixedLengthResponse(Response.Status.OK, "application/json", ret));
                    } catch (JSONException e) {
                        Log.e("getBLFdata error", "error");
                        throw new RuntimeException(e);
                    }

                case getAnalysisByParams:
                    Log.e("HTTP", "getAnalysisByParams run");
                    try {
                        session.parseBody(params);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ResponseException e) {
                        throw new RuntimeException(e);
                    }
                    postDataStr = params.get("postData");

                    Log.d("postDataStr", postDataStr);

                    ret = blfGetAnalysisByParams(postDataStr);
                    Log.e("HTTP", "getAnalysisByParams finish");
                    return wrapResponse(session, newFixedLengthResponse(Response.Status.OK, "application/json", ret));

                case reAdjust:
                    ret = reAdjust();
                    return wrapResponse(session, newFixedLengthResponse(Response.Status.OK, "application/json", ret));

                case blfthaveDataSignal:
                    Log.e("HTTP", "blfthaveDataSignal run ");
                    try {
                        session.parseBody(params);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ResponseException e) {
                        throw new RuntimeException(e);
                    }
                    postDataStr = params.get("postData");

                    blfthaveDataSignal(postDataStr);
                    return wrapResponse(session, newFixedLengthResponse(Response.Status.OK, "application/json", ""));
            }

        }

        return newFixedLengthResponse(Response.Status.OK, "application/json", "");

    }
}
