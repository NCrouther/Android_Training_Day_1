package com.bignerdranch.android.networkingarchitecture.web;

import com.bignerdranch.android.networkingarchitecture.exception.UnauthorizedException;

import java.net.HttpURLConnection;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RetrofitErrorHandler implements ErrorHandler {
    @Override
    public Throwable handleError(RetrofitError cause) {
        Response response = cause.getResponse();
        if (response != null &&
                response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            return new UnauthorizedException(cause);
        }
        return cause;
    }
}
