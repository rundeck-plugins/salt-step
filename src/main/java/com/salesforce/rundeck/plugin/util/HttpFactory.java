package com.salesforce.rundeck.plugin.util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.stereotype.Component;

/**
 * A factory class for commons-httpclient for testing.
 */
@Component
public class HttpFactory {
    public HttpClient createHttpClient() {
        return new HttpClient();
    }

    public PostMethod createPostMethod(String uri) {
        return new PostMethod(uri);
    }

    public GetMethod createGetMethod(String uri) {
        return new GetMethod(uri);
    }
}