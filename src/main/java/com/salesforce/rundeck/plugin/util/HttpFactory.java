package com.salesforce.rundeck.plugin.util;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.stereotype.Component;

/**
 * A factory class for http-components for testing.
 */
@Component
public class HttpFactory {
    
    public HttpClient createHttpClient() {
        return new DefaultHttpClient();
    }

    public HttpPost createHttpPost(String uri) {
        return new HttpPost(uri);
    }

    public HttpGet createHttpGet(String uri) {
        return new HttpGet(uri);
    }
}