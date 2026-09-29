package com.ccp.implementations.http.apache.mime;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import javax.net.ssl.SSLContext;

/**
 * Apache HttpClient {@code HttpRequestRetryHandler} implementation. Makes up to 3 attempts
 * for idempotent requests (without body), giving up immediately on timeout, unknown host,
 * connection failure or SSL error.
 */
class CcpHttpRequestRetryHandler implements HttpRequestRetryHandler {

	
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		boolean maxRetriesReached = executionCount >= 3;
       if (maxRetriesReached) {
            // Do not retry if over max retry count
            return false;
        }
        boolean isInterruptedIOException = exception instanceof InterruptedIOException;
        if (isInterruptedIOException) {
            // Timeout
            return false;
        }
        boolean isUnknownHostException = exception instanceof UnknownHostException;
        if (isUnknownHostException) {
            // Unknown host
            return false;
        }
        boolean isConnectTimeoutException = exception instanceof ConnectTimeoutException;
        if (isConnectTimeoutException) {
            // Connection refused
            return false;
        }
        boolean isSSLException = exception instanceof SSLException;
        if (isSSLException) {
            // SSL handshake exception
            return false;
        }
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        HttpRequest request = clientContext.getRequest();
        boolean isHttpEntityEnclosingRequest = request instanceof HttpEntityEnclosingRequest;
        boolean isIdempotent = false == (isHttpEntityEnclosingRequest);
		return isIdempotent;
	}

	@SuppressWarnings("deprecation")
	static CloseableHttpClient getClient() throws Exception{
		SSLContextBuilder builder = new SSLContextBuilder();
		TrustSelfSignedStrategy trustSelfSignedStrategy = new TrustSelfSignedStrategy();
		builder.loadTrustMaterial(null, trustSelfSignedStrategy);
		SSLContext sslContext = builder.build();

		LayeredConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);;
                HttpClientBuilder clientBuilder = HttpClients.custom();
                HttpClientBuilder clientBuilderWithSsl = clientBuilder.setSSLSocketFactory(sslSocketFactory);
                CcpHttpRequestRetryHandler ccpHttpRequestRetryHandler = new CcpHttpRequestRetryHandler();

                HttpClientBuilder clientBuilderWithRetryHandler = clientBuilderWithSsl.setRetryHandler(ccpHttpRequestRetryHandler);
		CloseableHttpClient client = clientBuilderWithRetryHandler.build();
		return client;
	}

}