package com.example.testcdc.httpServer;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HttpServer extends NanoHTTPD {

    public HttpServer(int port) {
        super(port);
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (session.getMethod() == Method.POST) {
            // Handle POST request
            Map<String, String> postParams = session.getParms();
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> entry : postParams.entrySet()) {
                postData.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
            System.out.println("Received POST data: " + postData.toString());
            return newFixedLengthResponse("{\"status\":\"success\", \"message\":\"POST data received\"}");
        } else if (session.getMethod() == Method.GET) {
            // Handle GET request
            Map<String, List<String>> getParams = session.getParameters();
            StringBuilder getData = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : getParams.entrySet()) {
                getData.append(entry.getKey()).append("=").append(String.join(",", entry.getValue())).append("\n");
            }
            System.out.println("Received GET parameters: " + getData.toString());
            return newFixedLengthResponse("{\"status\":\"success\", \"message\":\"GET parameters received\", \"parameters\":\"" + getData.toString() + "\"}");
        }
        return newFixedLengthResponse("{\"status\":\"error\", \"message\":\"Invalid request method\"}");
    }

    public static void main(String[] args) {
        new HttpServer(8080);
        System.out.println("Server started at http://localhost:8080/");
    }
}