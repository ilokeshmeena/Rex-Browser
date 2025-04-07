package com.rexvit.browser.network;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;


public class NullOnEmptyConverterFactory extends Converter.Factory {
    @Override 
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotationArr, Retrofit retrofit) {
        final Converter nextResponseBodyConverter = retrofit.nextResponseBodyConverter(this, type, annotationArr);
        return new Converter() { 
            @Override 
            public final Object convert(Object obj) {
                try {
                    return NullOnEmptyConverterFactory.nResponseBodyConverter(nextResponseBodyConverter, (ResponseBody) obj);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    
    public static  Object nResponseBodyConverter(Converter converter, ResponseBody responseBody) throws IOException {
        if (responseBody.contentLength() == 0) {
            return null;
        }
        return converter.convert(responseBody);
    }
}
