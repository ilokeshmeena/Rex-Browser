package com.rexvit.browser.callbacks;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;


public class UserAgentInterceptor implements Interceptor {
    private final String mUserAgent;

    public UserAgentInterceptor(String str) {
        this.mUserAgent = str;
    }

    @Override 
    public Response intercept(Interceptor.Chain chain) throws IOException {
        return chain.proceed(chain.request().newBuilder().header("User-Agent", this.mUserAgent).build());
    }
}
