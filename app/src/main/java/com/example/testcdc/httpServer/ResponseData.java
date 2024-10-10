package com.example.testcdc.httpServer;

import com.example.testcdc.Utils.ResultsCode;

public class ResponseData {

    public static <T> Result<T> success(T data) {
        return new Result<T>().setCode(ResultsCode.SUCCESS.code).setMsg(ResultsCode.SUCCESS.message).setData(data);
    }

    public static Result<Object> success() {
        return new Result<Object>().setCode(ResultsCode.SUCCESS.code).setMsg(ResultsCode.SUCCESS.message);
    }
    public static Result<Object> fail() {
        return new Result<Object>().setCode(ResultsCode.FAIL.code).setMsg(ResultsCode.FAIL.message);
    }

    public static <T> Result<T> fail(T data) {
        return new Result<T>().setCode(ResultsCode.FAIL.code).setMsg(ResultsCode.FAIL.message).setData(data);
    }

}
