package com.example.testcdc;

import java.io.Serializable;

public class JsCallResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private String callback;

    private String method;

    private T data;

    public JsCallResult(String callback) {
        this.callback = callback;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
