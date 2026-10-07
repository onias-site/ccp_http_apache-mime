package com.ccp.implementations.http.apache.mime;

import java.util.List;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.http.CcpHttpBodyBinary;
import com.ccp.especifications.http.CcpHttpBodyText;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;
import java.net.URI;

/**
 * {@code CcpHttpRequester} implementation using Apache HttpClient with multipart/mime support.
 * Builds and runs simple and multipart HTTP requests, applies the retry handler configured
 * in {@code CcpHttpRequestRetryHandler}, and converts the response into a {@code CcpHttpResponse}.
 */
class ApacheMimeHttpRequester implements CcpHttpRequester {

	
	/**
	 * Runs the request with a JSON body and the given headers.
	 * @param url the target URL
	 * @param method the HTTP method
	 * @param headers the request headers
	 * @param body the request body (ignored by GET, DELETE and HEAD)
	 * @return the response
	 * @throws CcpErrorApacheMimeHttp when the request cannot be executed
	 */
	public CcpHttpResponse executeHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, String body) {
	
		HttpRequestBase httpRequest = this.buildHttpRequestWithBody(url, method, headers, body);
	
		try {
			CcpHttpResponse httpResponse = this.executeHttpRequest(httpRequest);
			
			return httpResponse;
			
		} catch (Exception e) {
			CcpErrorApacheMimeHttp ccpErrorApacheMimeHttp = new CcpErrorApacheMimeHttp(e);
			throw ccpErrorApacheMimeHttp;
		}
	}

	/**
	 * Executes the request with the shared client and reads the status, the body and the equivalent curl command (with
	 * the secrets masked, see {@link #toCurl}). The response is closed, giving its connection back to the pool.
	 * @param httpRequest the request
	 * @return the response
	 * @throws Exception when the request fails
	 */
	private CcpHttpResponse executeHttpRequest(HttpRequestBase httpRequest) throws Exception{
		CloseableHttpClient client = CcpHttpRequestRetryHandler.getClient();

		try (CloseableHttpResponse response = client.execute(httpRequest)) {
			HttpEntity entity = response.getEntity();
			String responseBody = "";
			boolean hasEntity = entity != null;
			if(hasEntity) {
				responseBody = EntityUtils.toString(entity);
			}

			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			String curl = this.toCurl(httpRequest);
			CcpHttpResponse ccpHttpResponse = new CcpHttpResponse(responseBody, statusCode, curl);
			return ccpHttpResponse;
		}
	}

	/**
	 * Builds the request with the body and the headers.
	 * @param url the target URL
	 * @param method the HTTP method
	 * @param headers the request headers
	 * @param body the request body
	 * @return the request
	 */
	private HttpRequestBase buildHttpRequestWithBody(String url, CcpHttpMethods method, CcpJsonRepresentation headers, String body) {
		String methodName = method.name();
		HttpMethod verb = HttpMethod.valueOf(methodName);
		HttpRequestBase httpRequest = verb.getMethodWithBody(url, body);
		
		Set<String> keySet = headers.fieldSet();
		for (String headerName : keySet) { 
			CcpFieldName headerFieldName = new CcpFieldName(headerName);
			String headerValue = headers.getAsString(headerFieldName);
			httpRequest.addHeader(headerName, headerValue);
		}
		return httpRequest;
	}

	/**
	 * Builds a body-capable request (POST, PUT, PATCH) with the headers but no body yet.
	 * @param url the target URL
	 * @param method the HTTP method
	 * @param headers the request headers
	 * @return the request
	 */
	private HttpEntityEnclosingRequestBase buildHttpRequestWithoutBody(String url, CcpHttpMethods method, CcpJsonRepresentation headers) {
		String methodName = method.name();
		HttpMethod verb = HttpMethod.valueOf(methodName);
		HttpEntityEnclosingRequestBase httpRequest = verb.getMethodWithoutBody(url);
		
		Set<String> keySet = headers.fieldSet();
		for (String headerName : keySet) { 
			CcpFieldName headerFieldName = new CcpFieldName(headerName);
			String headerValue = headers.getAsString(headerFieldName);
			httpRequest.addHeader(headerName, headerValue);
		}
		return httpRequest;
	}
	
	/**
	 * Runs a multipart request with the binary parts followed by the text parts.
	 * @param url the target URL
	 * @param method the HTTP method (POST, PUT or PATCH)
	 * @param headers the request headers
	 * @param bodyTexts the text parts
	 * @param bodyBinaries the binary parts
	 * @return the response
	 * @throws CcpErrorApacheMimeHttp when the request cannot be executed
	 */
	public CcpHttpResponse executeMultiPartHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, List<CcpHttpBodyText> bodyTexts, List<CcpHttpBodyBinary> bodyBinaries) {
		
		HttpEntityEnclosingRequestBase httpRequest = this.buildHttpRequestWithoutBody(url, method, headers);
		
		MultipartEntityBuilder multipart = MultipartEntityBuilder.create();
		
		for (var body : bodyBinaries) {

			byte[] bytes = body.getBytes();
			String contentTypeName = body.contentType.name();
			CustomContentType binaryContentType = CustomContentType.valueOf(contentTypeName);

			multipart = multipart.addBinaryBody(
	               body.name,
	                bytes,
	                binaryContentType.contentType,
	                body.fileName
	            );
		}
		
		for (var body : bodyTexts) {
			String contentTypeName = body.contentType.name();
			CustomContentType textContentType = CustomContentType.valueOf(contentTypeName);
			multipart = multipart.addTextBody(
	               body.name,
	               body.text,
	                textContentType.contentType
	            );
		}
		HttpEntity multipartEntity = multipart.build();
		
		httpRequest.setEntity(multipartEntity);
		try {
			CcpHttpResponse httpResponse = this.executeHttpRequest(httpRequest);
			
			return httpResponse;
		} catch (Exception e) {
			CcpErrorApacheMimeHttp ccpErrorApacheMimeHttp = new CcpErrorApacheMimeHttp(e);
			throw ccpErrorApacheMimeHttp;
		}

	} 
	
	/** Headers whose values are credentials, masked in the curl command (compared ignoring case). */
	private static final Set<String> SECRET_HEADERS = Set.of("authorization", "proxy-authorization", "cookie", "sessiontoken", "x-api-key");

	/** Text that replaces a secret in the curl command. */
	static final String MASK = "***";

	/**
	 * Builds the curl command equivalent to the request, for debugging: method, URL, every header and the body. The curl
	 * goes into errors, logs and the {@code jn_http_api_*} records, so the secrets are masked: the values of the
	 * {@link #SECRET_HEADERS} and the Telegram bot token that travels in the URL path ({@code /bot<token>/}). Until
	 * 2026-10-06 they went in clear.
	 * @param request the request
	 * @return the curl command
	 */
	String toCurl(HttpUriRequest request) {
        StringBuilder curl = new StringBuilder("curl");
        StringBuilder curlWithMethodFlag = curl.append(" -X ");
        String methodName = request.getMethod();

        // Method
        curlWithMethodFlag.append(methodName);
        StringBuilder curlWithUrlQuote = curl.append(" \"");
        URI requestURI = request.getURI();
        String requestUrl = requestURI.toString();
        String maskedUrl = requestUrl.replaceAll("/bot[^/]+/", "/bot" + MASK + "/");
        StringBuilder curlWithUrl = curlWithUrlQuote.append(maskedUrl);

        // URL
        curlWithUrl.append("\"");
        Header[] allHeaders = request.getAllHeaders();

        // Headers
        for (Header header : allHeaders) {
            StringBuilder curlWithHeaderFlag = curl.append(" -H \"");
            String headerName = header.getName();
            StringBuilder curlWithHeaderName = curlWithHeaderFlag
                .append(headerName);
                StringBuilder curlWithHeaderSeparator = curlWithHeaderName.append(": ");
                String lowerCaseHeaderName = headerName.toLowerCase();
                boolean isSecret = SECRET_HEADERS.contains(lowerCaseHeaderName);
                String headerValue = isSecret ? MASK : header.getValue();
                StringBuilder curlWithHeader = curlWithHeaderSeparator
                .append(headerValue);
                curlWithHeader
                .append("\"");
        }
        boolean isHttpEntityEnclosingRequestBase = request instanceof HttpEntityEnclosingRequestBase;

        // Body (POST, PUT, PATCH...)
        if (isHttpEntityEnclosingRequestBase) {
            HttpEntityEnclosingRequestBase entityRequest =
                (HttpEntityEnclosingRequestBase) request;

            HttpEntity entity = entityRequest.getEntity();
            boolean hasEntity = entity != null;
            if (hasEntity) {
                String body;
				try {
					body = EntityUtils.toString(entity);
				} catch (Exception e) {
					CcpErrorApacheMimeHttp ccpErrorApacheMimeHttp = new CcpErrorApacheMimeHttp(e);
					throw ccpErrorApacheMimeHttp;
				}
    StringBuilder curlWithDataFlag = curl.append(" --data '");
    String escapedBody = body.replace("'", "'\"'\"'");
    StringBuilder curlWithBody = curlWithDataFlag
                    .append(escapedBody);
                    curlWithBody
                    .append("'");
            }
        }
        String curlCommand = curl.toString();
        return curlCommand;
    }
	

	/** Raised when an HTTP request cannot be executed. */
	@SuppressWarnings("serial")
	private static class CcpErrorApacheMimeHttp extends RuntimeException {
		/**
		 * Wraps the cause.
		 * @param cause the original failure
		 */
		private CcpErrorApacheMimeHttp(Throwable cause) {
			super(cause);
		}
	}
}
