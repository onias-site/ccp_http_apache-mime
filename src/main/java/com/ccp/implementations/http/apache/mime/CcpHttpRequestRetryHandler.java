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
 * Implementação de {@code HttpRequestRetryHandler} do Apache HttpClient. Realiza até 3 tentativas
 * para requisições idempotentes (sem corpo), abortando imediatamente em casos de timeout,
 * host desconhecido, falha de conexão ou erro SSL.
 */
class CcpHttpRequestRetryHandler implements HttpRequestRetryHandler {

	
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		boolean executionCountMaiorOuIgual = executionCount >= 3;
       if (executionCountMaiorOuIgual) {
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
        boolean b = false == (isHttpEntityEnclosingRequest);
		return b;
	}

	@SuppressWarnings("deprecation")
	static CloseableHttpClient getClient() throws Exception{
		SSLContextBuilder builder = new SSLContextBuilder();
		TrustSelfSignedStrategy trustSelfSignedStrategy = new TrustSelfSignedStrategy();
		builder.loadTrustMaterial(null, trustSelfSignedStrategy);
		SSLContext build = builder.build();

		LayeredConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                build, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);;
                HttpClientBuilder custom2 = HttpClients.custom();
                HttpClientBuilder custom = custom2.setSSLSocketFactory(sslsf);
                CcpHttpRequestRetryHandler ccpHttpRequestRetryHandler = new CcpHttpRequestRetryHandler();

                HttpClientBuilder setRetryHandler = custom.setRetryHandler(ccpHttpRequestRetryHandler);
		CloseableHttpClient client = setRetryHandler.build();
		return client;
	}

}