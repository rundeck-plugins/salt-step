package com.salesforce.rundeck.plugin.util;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.springframework.stereotype.Component;

/**
 * A factory class for http-components for testing. This factory allows for clients that require
 * a http client/methods to delegate the creation of dependencies which makes the clients testable.
 */
@Component
public class HttpFactory {
    
    public HttpClient createHttpClient() {
        return new SystemDefaultHttpClient();
    }

    public HttpPost createHttpPost(String uri) {
        return new HttpPost(uri);
    }

    public HttpGet createHttpGet(String uri) {
        return new HttpGet(uri);
    }
}