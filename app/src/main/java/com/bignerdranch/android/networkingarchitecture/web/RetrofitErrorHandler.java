package com.bignerdranch.android.networkingarchitecture.web;

import com.bignerdranch.android.networkingarchitecture.exception.UnauthorizedException;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Interceptor;
import okhttp3.Response;

public class RetrofitErrorHandler implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }
        return response;
    }
}
