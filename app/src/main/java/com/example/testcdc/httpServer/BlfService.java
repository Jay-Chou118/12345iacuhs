package com.example.testcdc.httpServer;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface BlfService {
    @POST("/blft/getBLFdata")
    Call<Map<String, Object>> getBlfData(@Body Map<String, String> body);
}
