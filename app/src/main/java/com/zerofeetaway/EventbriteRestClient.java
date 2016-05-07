package com.zerofeetaway;

import com.loopj.android.http.*;

public class EventbriteRestClient {
    private static final String BASE_URL = "https://www.eventbriteapi.com/v3/";
    /** Anonymous access OAuth token */
    private static final String AUTH_TOKEN_SUFFIX = "/?token=7CAZDDALALXS7RWPCBJX";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    /** Unused */
    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl + AUTH_TOKEN_SUFFIX;
    }
}
