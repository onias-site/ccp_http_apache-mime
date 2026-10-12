package com.ccp.implementations.http.apache.mime;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;

/**
 * Apache HttpClient {@code HttpRequestRetryHandler} implementation. Makes up to 3 attempts
 * for idempotent requests (without body), giving up immediately on timeout, unknown host,
 * connection failure or SSL error.
 */
class CcpHttpRequestRetryHandler implements HttpRequestRetryHandler {

	
	/**
	 * Retries up to 3 executions only requests without body, and never on timeout, unknown host, connection timeout or
	 * SSL error.
	 * @param exception the I/O failure
	 * @param executionCount how many times the request was executed
	 * @param context the execution context
	 * @return {@code true} when the request must be retried
	 */
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

	/** Connections kept in the pool, in all. */
	private static final int MAX_CONNECTIONS = 100;

	/** Connections kept in the pool per destination host. */
	private static final int MAX_CONNECTIONS_PER_HOST = 20;

	/** The single client of the process, with its pool of connections; see {@link #getClient()}. */
	private static final CloseableHttpClient CLIENT = buildClient();

	/**
	 * Returns the HTTP client shared by every request. Until 2026-10-06 each request built a new client and closed neither
	 * the client nor the response (one leaked connection per call), and the client trusted any self-signed certificate
	 * and any host name; now the certificates and host names are checked by the default JVM rules.
	 * @return the client
	 */
	static CloseableHttpClient getClient() {
		return CLIENT;
	}

	/**
	 * How long, in seconds, a connection may stay idle in the pool and still be reused. A keep-alive connection left idle
	 * for minutes may be dropped by the other side (or by a NAT/firewall in between) without any notice, and the pool
	 * only finds that out when it reuses it: the request is written and the answer is a {@code Connection reset}. That is
	 * what happened on 2026-10-11 when sending a message to the Telegram after a few minutes without traffic.
	 */
	private static final int MAX_IDLE_SECONDS = 20;

	/**
	 * Builds the client: default TLS validation, a pool of connections and this retry handler. A connection idle for
	 * longer than {@link #MAX_IDLE_SECONDS} is never reused: its keep-alive expires at that time (even when the server
	 * promises more), the pool discards an expired connection when lending it, and a background thread closes the idle
	 * and expired ones. Retrying a failed POST was not an option: the server may have received it already, and a
	 * message to the user would go twice.
	 * @return the client
	 */
	private static CloseableHttpClient buildClient() {
		HttpClientBuilder clientBuilder = HttpClients.custom();
		CcpHttpRequestRetryHandler ccpHttpRequestRetryHandler = new CcpHttpRequestRetryHandler();
		HttpClientBuilder clientBuilderWithRetryHandler = clientBuilder.setRetryHandler(ccpHttpRequestRetryHandler);
		HttpClientBuilder clientBuilderWithPool = clientBuilderWithRetryHandler
				.setMaxConnTotal(MAX_CONNECTIONS)
				.setMaxConnPerRoute(MAX_CONNECTIONS_PER_HOST);
		HttpClientBuilder clientBuilderWithKeepAlive = clientBuilderWithPool
				.setKeepAliveStrategy(CcpHttpRequestRetryHandler::getKeepAliveDuration)
				.evictExpiredConnections()
				.evictIdleConnections(MAX_IDLE_SECONDS, TimeUnit.SECONDS);
		CloseableHttpClient client = clientBuilderWithKeepAlive.build();
		return client;
	}

	/**
	 * How long the connection of this response may be reused: what the server announces in {@code Keep-Alive}, limited
	 * to {@link #MAX_IDLE_SECONDS}.
	 * @param response the response
	 * @param context the execution context
	 * @return the keep-alive, in milliseconds
	 */
	static long getKeepAliveDuration(HttpResponse response, HttpContext context) {
		long maxIdleMillis = TimeUnit.SECONDS.toMillis(MAX_IDLE_SECONDS);
		long keepAliveAnnouncedByTheServer = DefaultConnectionKeepAliveStrategy.INSTANCE.getKeepAliveDuration(response, context);
		boolean serverDidNotLimit = keepAliveAnnouncedByTheServer <= 0;
		if (serverDidNotLimit) {
			return maxIdleMillis;
		}
		long keepAlive = Math.min(keepAliveAnnouncedByTheServer, maxIdleMillis);
		return keepAlive;
	}

}
