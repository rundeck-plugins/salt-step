/**
 * Copyright (c) 2013, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.rundeck.plugin.salt.util;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Used to retry HttpRequests. This is a replacement for Httpcomponents'
 * <a href="http//hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/client/HttpRequestRetryHandler.html">HttpRetryRequestHandler</a> 
 * since it doesn't handle status code failures.
 */
@Component
public class RetryingHttpClientExecutor {

    @Autowired
    @Value("${retryingHttpClientExecutor.maximumRetryDelay}")
    protected long maximumRetryDelay;

    @Autowired
    @Value("${retryingHttpClientExecutor.delayStep}")
    protected long delayStep;

    @Autowired
    protected ExponentialBackoffTimer.Factory timerFactory;

    /**
     * Same as {@link #execute(LogWrapper, HttpClient, HttpUriRequest, int, Predicate)} but with
     * an always true predicate
     */
    public HttpResponse execute(LogWrapper log, HttpClient client, HttpUriRequest request, int retryCount)
            throws IOException, InterruptedException {
        return execute(log, client, request, retryCount, Predicates.<Integer> alwaysTrue());
    }

    /**
     * Attempts to execute the given request against the given client with the given number of retry
     * attempts.
     * 
     * This method will not try to distinguish between idempotent and non-idempotent requests. If
     * a caller is using this executor, the request will be retried.
     * 
     * @param log
     *            a {@link LogWrapper} instance to use for logging
     * @param client
     *            the HttpClient to execute with
     * @param request
     *            the HttpUriRequest to retry
     * @param retryCount
     *            the maximum number of retries
     * @param statusCodePredicate
     *            a predicate that is invoked after a non successful (i.e. non 2XX or 3XX)
     *            request to determine whether it should proceed. true means continue, false means abort.
     */
    public HttpResponse execute(LogWrapper log, HttpClient client, HttpUriRequest request, int retryCount,
            Predicate<Integer> statusCodePredicate) throws IOException, InterruptedException {
        Preconditions.checkArgument(retryCount > 0);
        ExponentialBackoffTimer timer = timerFactory.newTimer(retryCount, maximumRetryDelay);
        int count = 0;
        HttpResponse lastResponse = null;
        IOException lastException = null;
        while (count++ < retryCount) {
            lastResponse = null;
            try {
                lastResponse = client.execute(request);
                int code = lastResponse.getStatusLine().getStatusCode();
                if (isSuccessfulStatusCode(code) || !statusCodePredicate.apply(code)) {
                    return lastResponse;
                } else {
                    log.debug("Encountered recoverable status code: %s", lastResponse.getStatusLine());
                }
            } catch (ClientProtocolException e) {
                log.debug("Client protocol exception encountered, not retrying. %s", e.getMessage());
                throw e;
            } catch (UnknownHostException e) {
                log.debug("Unknown host encountered, not retrying. %s", e.getMessage());
                throw e;
            } catch (SSLException ssle) {
                log.debug("SSL exception encountered, not retrying. %s", ssle.getMessage());
                throw ssle;
            } catch (SocketException e) {
                log.debug("Socket exception encountered, retrying. %s", e.getMessage());
                lastException = e;
            } catch (ConnectTimeoutException e) {
                log.debug("Connection timeout encountered, retrying. %s", e.getMessage());
                lastException = e;
            } catch (IOException e) {
                log.debug("Generic i/o exception encountered, retrying. %s", e.getMessage());
                lastException = e;
            }
            if (count < retryCount) {
                timer.waitForNext();
            }
        }
        if (lastResponse == null) {
            throw lastException;
        } else {
            return lastResponse;
        }
    }

    protected boolean isSuccessfulStatusCode(int code) {
        int codeClass = code / 100;
        return codeClass != 4 && codeClass != 5;
    }
}
