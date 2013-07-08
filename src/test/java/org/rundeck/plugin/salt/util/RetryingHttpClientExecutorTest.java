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
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.rundeck.plugin.salt.util.ExponentialBackoffTimer;
import org.rundeck.plugin.salt.util.LogWrapper;
import org.rundeck.plugin.salt.util.RetryingHttpClientExecutor;
import org.rundeck.plugin.salt.util.ExponentialBackoffTimer.Factory;

import com.google.common.base.Predicate;

public class RetryingHttpClientExecutorTest {

    protected HttpClient client;
    protected HttpGet get;
    protected HttpResponse response;
    protected RetryingHttpClientExecutor executor;
    protected ExponentialBackoffTimer timer;
    protected Factory timerFactory;
    protected LogWrapper logger;

    @Before
    public void setup() {
        executor = new RetryingHttpClientExecutor();
        
        logger = Mockito.mock(LogWrapper.class);
        client = Mockito.mock(HttpClient.class);
        get = Mockito.mock(HttpGet.class);
        response = Mockito.mock(HttpResponse.class);
        
        timerFactory = Mockito.mock(Factory.class);
        timer = Mockito.mock(ExponentialBackoffTimer.class);
        Mockito.when(timerFactory.newTimer(Mockito.anyLong(), Mockito.anyLong())).thenReturn(timer);
        executor.timerFactory = timerFactory;
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testRetryWithZeroRetryCountThrowsException() throws Exception {
        executor.execute(logger, client, get, 0);
    }

    @Test
    public void testSuccessfulGet() throws Exception {
        setupResponseCode(get, HttpStatus.SC_ACCEPTED);
        Assert.assertSame("Expected mocked response to be returned.", response, executor.execute(logger, client, get, 10));
        Mockito.verifyZeroInteractions(timer);
    }

    @Test
    public void testUnsuccessfulGetThatThrowsClientProtocolException() throws Exception {
        ClientProtocolException someException = new ClientProtocolException();
        testThatThrowsUnrecoverableExceptionAbortsExecute(someException);
    }

    @Test
    public void testUnsuccessfulGetThatThrowsUnknownHostException() throws Exception {
        UnknownHostException someException = new UnknownHostException();
        testThatThrowsUnrecoverableExceptionAbortsExecute(someException);
    }

    @Test
    public void testUnsuccessfulGetThatThrowsSSLException() throws Exception {
        SSLException someException = new SSLException((String) null);
        testThatThrowsUnrecoverableExceptionAbortsExecute(someException);
    }

    @Test
    public void testUnsuccessfulGetThatThrowsSocketException() throws Exception {
        SocketException someException = new SocketException();
        testThatThrowsUnrecoverableExceptionAbortsExecute(someException, 5, 5);
    }

    @Test
    public void testUnsuccessfulGetThatThrowsConnectTimeoutException() throws Exception {
        ConnectTimeoutException someException = new ConnectTimeoutException();
        testThatThrowsUnrecoverableExceptionAbortsExecute(someException, 5, 5);
    }

    @Test
    public void testUnsuccessfulGetThatThrowsIOException() throws Exception {
        IOException someException = new IOException();
        testThatThrowsUnrecoverableExceptionAbortsExecute(someException, 5, 5);
    }

    @Test
    public void testUnsuccessfulGetThatReturnsErrorStatusCode() throws Exception {
        setupResponseCode(get, HttpStatus.SC_BAD_REQUEST);
        Assert.assertSame("Expected mocked response to be returned.", response, executor.execute(logger, client, get, 5));

        Mockito.verify(client, Mockito.times(5)).execute(Mockito.same(get));
        Mockito.verify(timer, Mockito.times(4)).waitForNext();
    }

    @Test
    public void testUnsuccessfulGetThatReturnsBlacklistedStatusCode() throws Exception {
        setupResponseCode(get, HttpStatus.SC_UNAUTHORIZED);
        Assert.assertSame("Expected mocked response to be returned.", response, executor.execute(logger, client, get, 5, new Predicate<Integer>() {
            @Override
            public boolean apply(Integer input) {
                return input != HttpStatus.SC_UNAUTHORIZED;
            }
        }));

        Mockito.verify(client, Mockito.times(1)).execute(Mockito.same(get));
        Mockito.verifyZeroInteractions(timer);
    }

    @Test
    public void testGetThatRetriesCodeAndThenGetsThrows() throws Exception {
        IOException someException = new IOException();
        setupResponseCode(get, HttpStatus.SC_UNAUTHORIZED).thenThrow(someException);

        try {
            executor.execute(logger, client, get, 2);
            Assert.fail("Expected exception.");
        } catch (Exception e) {
            Assert.assertSame("Expected mocked exception to be thrown.", someException, e);
        }

        Mockito.verify(client, Mockito.times(2)).execute(Mockito.same(get));
        Mockito.verify(timer, Mockito.times(1)).waitForNext();
    }

    @Test
    public void testGetThatRetriesThrowsThenGetsCode() throws Exception {
        IOException someException = new IOException();
        setupResponseWithStatusLine(HttpStatus.SC_BAD_REQUEST);
        Mockito.when(client.execute(Mockito.same(get))).thenThrow(someException).thenReturn(response);

        Assert.assertSame("Expected mocked response to be returned.", response, executor.execute(logger, client, get, 2));

        Mockito.verify(client, Mockito.times(2)).execute(Mockito.same(get));
        Mockito.verify(timer, Mockito.times(1)).waitForNext();
    }

    @Test
    public void testGetThatRetriesCodeAndThenGetsThrowsThenSuccess() throws Exception {
        IOException someException = new IOException();
        setupResponseWithStatusLine(HttpStatus.SC_UNAUTHORIZED, HttpStatus.SC_OK);
        Mockito.when(client.execute(Mockito.same(get))).thenReturn(response).thenThrow(someException)
                .thenReturn(response);

        Assert.assertSame("Expected mocked response to be returned.", response, executor.execute(logger, client, get, 4));

        Mockito.verify(client, Mockito.times(3)).execute(Mockito.same(get));
        Mockito.verify(timer, Mockito.times(2)).waitForNext();
    }

    @Test
    public void testGetThatRetriesThrowsThenGetsCodeThenSuccess() throws Exception {
        IOException someException = new IOException();
        setupResponseWithStatusLine(HttpStatus.SC_UNAUTHORIZED, HttpStatus.SC_OK);
        Mockito.when(client.execute(Mockito.same(get))).thenThrow(someException).thenReturn(response);

        Assert.assertSame("Expected mocked response to be returned.", response, executor.execute(logger, client, get, 4));

        Mockito.verify(client, Mockito.times(3)).execute(Mockito.same(get));
        Mockito.verify(timer, Mockito.times(2)).waitForNext();
    }

    @Test
    public void testGetThatRetriesAndIsSuccessful() throws Exception {
        setupResponseCodeAfter(get, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_OK);
        Assert.assertSame("Expected mocked response to be returned.", response, executor.execute(logger, client, get, 3));

        Mockito.verify(client, Mockito.times(2)).execute(Mockito.same(get));
        Mockito.verify(timer, Mockito.times(1)).waitForNext();
    }

    protected void setupResponseWithStatusLine(int... codes) {
        StatusLine line = Mockito.mock(StatusLine.class);
        OngoingStubbing<Integer> stub = Mockito.when(line.getStatusCode());
        for (int code : codes) {
            stub = stub.thenReturn(code);
        }
        Mockito.when(response.getStatusLine()).thenReturn(line);
    }

    protected OngoingStubbing<HttpResponse> setupResponseCode(HttpUriRequest request, int... codes) {
        return setupResponseCodeAfter(request, codes);
    }

    protected OngoingStubbing<HttpResponse> setupResponseCodeAfter(HttpUriRequest request, int... codes) {
        setupResponseWithStatusLine(codes);
        try {
            return Mockito.when(client.execute(request)).thenReturn(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void setupDoThrowOnExecute(Throwable t) {
        try {
            Mockito.when(client.execute(Mockito.any(HttpUriRequest.class))).thenThrow(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void testThatThrowsUnrecoverableExceptionAbortsExecute(Exception e) throws Exception {
        testThatThrowsUnrecoverableExceptionAbortsExecute(e, 5, 1);
    }
    
    protected void testThatThrowsUnrecoverableExceptionAbortsExecute(Exception e, int numRetries, int attempts) throws Exception {
        setupDoThrowOnExecute(e);
        try {
            executor.execute(logger, client, get, numRetries);
            Assert.fail("Expected exception.");
        } catch (Exception other) {
            Assert.assertSame("Expected mocked exception to be thrown.", e, other);
        }

        Mockito.verify(client, Mockito.times(attempts)).execute(Mockito.same(get));
        Mockito.verify(timer, Mockito.times(attempts - 1)).waitForNext();
    }
}
