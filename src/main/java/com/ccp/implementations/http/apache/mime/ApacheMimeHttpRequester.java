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

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.http.CcpHttpBodyBinary;
import com.ccp.especifications.http.CcpHttpBodyText;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;

class ApacheMimeHttpRequester implements CcpHttpRequester {

	
	public CcpHttpResponse executeHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, String body) {
	
		HttpRequestBase metodo = this.buildHttpRequestWithBody(url, method, headers, body);
	
		CcpHttpResponse executeHttpRequest = this.executeHttpRequest(metodo);

		return executeHttpRequest;
	}

	private CcpHttpResponse executeHttpRequest(HttpRequestBase metodo) {
		try {
			CloseableHttpClient client = CcpHttpRequestRetryHandler.getClient();
			CloseableHttpResponse response = client.execute(metodo);

			HttpEntity entity = response.getEntity();
			String string = "";
			if(entity != null) {
				string = EntityUtils.toString(entity);
			}
			
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			String curl = this.toCurl(metodo);
			CcpHttpResponse ccpHttpResponse = new CcpHttpResponse(string, statusCode, curl);
			return ccpHttpResponse;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private HttpRequestBase buildHttpRequestWithBody(String url, CcpHttpMethods method, CcpJsonRepresentation headers, String body) {
		HttpMethod verb = HttpMethod.valueOf(method.name());
		HttpRequestBase metodo = verb.getMethodWithBody(url, body);
		
		Set<String> keySet = headers.fieldSet();
		for (String headerName : keySet) { 
			String header = headers.getDynamicVersion().getAsString(headerName);
			metodo.addHeader(headerName, header);
		}
		return metodo;
	}

	private HttpEntityEnclosingRequestBase buildHttpRequestWithoutBody(String url, CcpHttpMethods method, CcpJsonRepresentation headers) {
		HttpMethod verb = HttpMethod.valueOf(method.name());
		HttpEntityEnclosingRequestBase metodo = verb.getMethodWithoutBody(url);
		
		Set<String> keySet = headers.fieldSet();
		for (String headerName : keySet) { 
			String header = headers.getDynamicVersion().getAsString(headerName);
			metodo.addHeader(headerName, header);
		}
		return metodo;
	}
	
	public CcpHttpResponse executeMultiPartHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, List<CcpHttpBodyText> bodyTexts, List<CcpHttpBodyBinary> bodyBinaries) {
		
		HttpEntityEnclosingRequestBase metodo = this.buildHttpRequestWithoutBody(url, method, headers);
		
		MultipartEntityBuilder multipart = MultipartEntityBuilder.create();
		
		for (var body : bodyBinaries) {

			byte[] bytes = body.getBytes();
			
			multipart = multipart.addBinaryBody(
	               body.name,
	                bytes,
	                CustomContentType.valueOf(body.contentType.name()).contentType,
	                body.fileName
	            );
		}
		
		for (var body : bodyTexts) {
			multipart = multipart.addTextBody(
	               body.name,
	               body.text,
	                CustomContentType.valueOf(body.contentType.name()).contentType
	            );
		}
		HttpEntity build = multipart.build();
		
		metodo.setEntity(build);

		CcpHttpResponse executeHttpRequest = this.executeHttpRequest(metodo);

		return executeHttpRequest;
	} 
	
	private String toCurl(HttpUriRequest request) throws Exception {
        StringBuilder curl = new StringBuilder("curl");

        // Método
        curl.append(" -X ").append(request.getMethod());

        // URL
        curl.append(" \"").append(request.getURI()).append("\"");

        // Headers
        for (Header header : request.getAllHeaders()) {
            curl.append(" -H \"")
                .append(header.getName()).append(": ")
                .append(header.getValue())
                .append("\"");
        }

        // Body (POST, PUT, PATCH...)
        if (request instanceof HttpEntityEnclosingRequestBase) {
            HttpEntityEnclosingRequestBase entityRequest =
                (HttpEntityEnclosingRequestBase) request;

            HttpEntity entity = entityRequest.getEntity();
            if (entity != null) {
                String body = EntityUtils.toString(entity);
                curl.append(" --data '")
                    .append(body.replace("'", "'\"'\"'")) // escape aspas simples
                    .append("'");
            }
        }
        return curl.toString();
    }
	
}
