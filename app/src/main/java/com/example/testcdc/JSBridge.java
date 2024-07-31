package com.example.testcdc;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JSBridge {
    private static final String BRIDGE_NAME = "JSBridge";
    private static final String CALLBACK_JS_FORMAT = "javascript:JSBridge.handleNativeResponse('%s')";

    private final Map<String, BridgeHandler> messageHandlers = new HashMap<String, BridgeHandler>();
    private final WebView webView;

    public JSBridge(WebView webView) {
        this.webView = webView;
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new JsInterface(), BRIDGE_NAME);
    }

    public void send(String method, JSONObject data, final BridgeHandler responseHandler) {
        try {
            JSONObject message = new JSONObject();
            message.put("method", method);
            message.put("data", data);

            if (responseHandler != null) {
                String callbackId = "cb_" + System.currentTimeMillis();
                message.put("callbackId", callbackId);
                messageHandlers.put(callbackId, responseHandler);
            }

            String js = String.format("javascript:JSBridge.send('%s')", message.toString());
            webView.loadUrl(js);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void registerHandler(String handlerName, BridgeHandler handler) {
        messageHandlers.put(handlerName, handler);
    }

    private void handleNativeResponse(String responseData) {
        try {
            JSONObject response = new JSONObject(responseData);
            String callbackId = response.optString("callbackId");
            if (callbackId.length() > 0) {
                BridgeHandler responseHandler = messageHandlers.get(callbackId);
                if (responseHandler != null) {
                    responseHandler.handle(response.optJSONObject("data"));
                    messageHandlers.remove(callbackId);
                }
            } else {
                String handlerName = response.optString("handlerName");
                if (handlerName.length() > 0) {
                    BridgeHandler handler = messageHandlers.get(handlerName);
                    if (handler != null) {
                        final String callbackJs = String.format(CALLBACK_JS_FORMAT, response.toString());
                        handler.handle(response.optJSONObject("data"), new BridgeHandler() {
                            @Override
                            public void handle(JSONObject data) {
                                webView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        webView.loadUrl(callbackJs);
                                    }
                                });
                            }

                            @Override
                            public void handle(JSONObject data, BridgeHandler responseCallback) {

                            }
                        });
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class JsInterface {
        @JavascriptInterface
        public void send(String message) {
            handleNativeResponse(message);
        }
    }

    public interface BridgeHandler {
        void handle(JSONObject data);
        void handle(JSONObject data, BridgeHandler responseCallback);
    }
}