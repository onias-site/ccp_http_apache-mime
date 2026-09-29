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

	private CcpHttpResponse executeHttpRequest(HttpRequestBase httpRequest) throws Exception{
		CloseableHttpClient client = CcpHttpRequestRetryHandler.getClient();
		CloseableHttpResponse response = client.execute(httpRequest);

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
	
	private String toCurl(HttpUriRequest request) {
        StringBuilder curl = new StringBuilder("curl");
        StringBuilder curlWithMethodFlag = curl.append(" -X ");
        String methodName = request.getMethod();

        // Method
        curlWithMethodFlag.append(methodName);
        StringBuilder curlWithUrlQuote = curl.append(" \"");
        URI requestURI = request.getURI();
        StringBuilder curlWithUrl = curlWithUrlQuote.append(requestURI);

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
                String headerValue = header.getValue();
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
	

	@SuppressWarnings("serial")
	private static class CcpErrorApacheMimeHttp extends RuntimeException {
		private CcpErrorApacheMimeHttp(Throwable cause) {
			super(cause);
		}
	}
}
