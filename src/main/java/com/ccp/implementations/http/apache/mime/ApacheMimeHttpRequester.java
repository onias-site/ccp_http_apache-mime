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
 * Implementação de {@code CcpHttpRequester} usando o Apache HttpClient com suporte a multipart/mime.
 * Constrói e executa requisições HTTP simples e multipart, aplica o retry handler configurado
 * em {@code CcpHttpRequestRetryHandler}, e converte a resposta em {@code CcpHttpResponse}.
 */
class ApacheMimeHttpRequester implements CcpHttpRequester {

	
	public CcpHttpResponse executeHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, String body) {
	
		HttpRequestBase metodo = this.buildHttpRequestWithBody(url, method, headers, body);
	
		try {
			CcpHttpResponse executeHttpRequest = this.executeHttpRequest(metodo);
			
			return executeHttpRequest;
			
		} catch (Exception e) {
			CcpErrorApacheMimeHttp ccpErrorApacheMimeHttp = new CcpErrorApacheMimeHttp(e);
			throw ccpErrorApacheMimeHttp;
		}
	}

	private CcpHttpResponse executeHttpRequest(HttpRequestBase metodo) throws Exception{
		CloseableHttpClient client = CcpHttpRequestRetryHandler.getClient();
		CloseableHttpResponse response = client.execute(metodo);

		HttpEntity entity = response.getEntity();
		String string = "";
		boolean entityDiferente = entity != null;
		if(entityDiferente) {
			string = EntityUtils.toString(entity); 
		}
		
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		String curl = this.toCurl(metodo);
		CcpHttpResponse ccpHttpResponse = new CcpHttpResponse(string, statusCode, curl);
		return ccpHttpResponse;
	}

	private HttpRequestBase buildHttpRequestWithBody(String url, CcpHttpMethods method, CcpJsonRepresentation headers, String body) {
		String methodName = method.name();
		HttpMethod verb = HttpMethod.valueOf(methodName);
		HttpRequestBase metodo = verb.getMethodWithBody(url, body);
		
		Set<String> keySet = headers.fieldSet();
		for (String headerName : keySet) { 
			CcpFieldName ccpFieldName = new CcpFieldName(headerName);
			String header = headers.getAsString(ccpFieldName);
			metodo.addHeader(headerName, header);
		}
		return metodo;
	}

	private HttpEntityEnclosingRequestBase buildHttpRequestWithoutBody(String url, CcpHttpMethods method, CcpJsonRepresentation headers) {
		String methodName2 = method.name();
		HttpMethod verb = HttpMethod.valueOf(methodName2);
		HttpEntityEnclosingRequestBase metodo = verb.getMethodWithoutBody(url);
		
		Set<String> keySet = headers.fieldSet();
		for (String headerName : keySet) { 
			CcpFieldName ccpFieldName2 = new CcpFieldName(headerName);
			String header = headers.getAsString(ccpFieldName2);
			metodo.addHeader(headerName, header);
		}
		return metodo;
	}
	
	public CcpHttpResponse executeMultiPartHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, List<CcpHttpBodyText> bodyTexts, List<CcpHttpBodyBinary> bodyBinaries) {
		
		HttpEntityEnclosingRequestBase metodo = this.buildHttpRequestWithoutBody(url, method, headers);
		
		MultipartEntityBuilder multipart = MultipartEntityBuilder.create();
		
		for (var body : bodyBinaries) {

			byte[] bytes = body.getBytes();
			String contentTypeName = body.contentType.name();
			CustomContentType valueOf = CustomContentType.valueOf(contentTypeName);

			multipart = multipart.addBinaryBody(
	               body.name,
	                bytes,
	                valueOf.contentType,
	                body.fileName
	            );
		}
		
		for (var body : bodyTexts) {
			String contentTypeName2 = body.contentType.name();
			CustomContentType valueOf2 = CustomContentType.valueOf(contentTypeName2);
			multipart = multipart.addTextBody(
	               body.name,
	               body.text,
	                valueOf2.contentType
	            );
		}
		HttpEntity build = multipart.build();
		
		metodo.setEntity(build);
		try {
			CcpHttpResponse executeHttpRequest = this.executeHttpRequest(metodo);
			
			return executeHttpRequest;
		} catch (Exception e) {
			CcpErrorApacheMimeHttp ccpErrorApacheMimeHttp2 = new CcpErrorApacheMimeHttp(e);
			throw ccpErrorApacheMimeHttp2;
		}

	} 
	
	private String toCurl(HttpUriRequest request) {
        StringBuilder curl = new StringBuilder("curl");
        StringBuilder append = curl.append(" -X ");
        String method2 = request.getMethod();

        // Método
        append.append(method2);
        StringBuilder append2 = curl.append(" \"");
        URI requestURI = request.getURI();
        StringBuilder append3 = append2.append(requestURI);

        // URL
        append3.append("\"");
        Header[] allHeaders = request.getAllHeaders();

        // Headers
        for (Header header : allHeaders) {
            StringBuilder append4 = curl.append(" -H \"");
            String headerName2 = header.getName();
            StringBuilder append5 = append4
                .append(headerName2);
                StringBuilder append6 = append5.append(": ");
                String headerValue = header.getValue();
                StringBuilder append7 = append6
                .append(headerValue);
                append7
                .append("\"");
        }
        boolean isHttpEntityEnclosingRequestBase = request instanceof HttpEntityEnclosingRequestBase;

        // Body (POST, PUT, PATCH...)
        if (isHttpEntityEnclosingRequestBase) {
            HttpEntityEnclosingRequestBase entityRequest =
                (HttpEntityEnclosingRequestBase) request;

            HttpEntity entity = entityRequest.getEntity();
            boolean entityDiferente2 = entity != null;
            if (entityDiferente2) {
                String body;
				try {
					body = EntityUtils.toString(entity);
				} catch (Exception e) {
					CcpErrorApacheMimeHttp ccpErrorApacheMimeHttp3 = new CcpErrorApacheMimeHttp(e);
					throw ccpErrorApacheMimeHttp3;
				}
    StringBuilder append8 = curl.append(" --data '");
    String bodyReplace = body.replace("'", "'\"'\"'");
    StringBuilder append9 = append8
                    .append(bodyReplace);
                    append9
                    .append("'");
            }
        }
        String toString = curl.toString();
        return toString;
    }
	

	@SuppressWarnings("serial")
	private static class CcpErrorApacheMimeHttp extends RuntimeException {
		private CcpErrorApacheMimeHttp(Throwable cause) {
			super(cause);
		}
	}
}
