package com.example.testcdc.Utils;

import java.util.HashMap;

public class ResponseData {

    public static <T> Result<T> ret(T data,boolean result) {
        if(result)
        {
            return success(data);
        }else {
            return fail(data);
        }
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>().setCode(ResultsCode.SUCCESS.code).setMsg(ResultsCode.SUCCESS.message).setData(data);
    }

    public static Result<Object> success() {
        return new Result<Object>().setCode(ResultsCode.SUCCESS.code).setMsg(ResultsCode.SUCCESS.message).setData(new HashMap<>());
    }

    public static Result fail() {
        return new Result().setCode(ResultsCode.FAIL.code).setMsg(ResultsCode.FAIL.message);
    }

    public static <T> Result<T> fail(T data) {
        return new Result<T>().setCode(ResultsCode.FAIL.code).setMsg(ResultsCode.FAIL.message).setData(data);
    }

}
