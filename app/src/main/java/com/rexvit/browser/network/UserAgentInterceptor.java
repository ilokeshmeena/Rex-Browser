package com.rexvit.browser.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;


public class UserAgentInterceptor implements Interceptor {
    private final String userAgent;

    public UserAgentInterceptor(String str) {
        this.userAgent = str;
    }

    @Override 
    public Response intercept(Interceptor.Chain chain) throws IOException {
        return chain.proceed(chain.request().newBuilder().header("User-Agent", this.userAgent).build());
    }
}
