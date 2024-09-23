package com.example.testcdc.httpServer;

import android.util.Log;

import fi.iki.elonen.NanoHTTPD;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpServer extends NanoHTTPD {
    public HttpServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (Method.POST.equals(session.getMethod())) {
            String jsonData = "";
            try {
                // 解析请求体
                jsonData = session.getParms().get("data");
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 向 Flask 发送 POST 请求
            sendPostToFlask(jsonData);
            return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"message\":\"Data sent to Flask\"}");
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
    }

    private void sendPostToFlask(String data) {
        OkHttpClient client = new OkHttpClient();

        String json = "{\"data\":\"" + data + "\"}"; // 构造 JSON 数据
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("http://0.0.0.0:8000/blft/getBLFdata")
                .post(body)
                .build();

        try (okhttp3.Response response = client.newCall(request).execute()) {
            // 处理响应
            if (response.isSuccessful()) {
                // 请求成功
                Log.d("sendPostToFlask: ", response.body().string());
                System.out.println(response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
