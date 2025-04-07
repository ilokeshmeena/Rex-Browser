package com.rexvit.browser.network;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;


public interface RequestService {
    @GET("{video_id}/?__a=1")
    Call<JsonObject> getVideoUrl(@Path("video_id") String str);
}
