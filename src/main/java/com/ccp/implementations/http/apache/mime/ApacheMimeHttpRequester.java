package com.ccp.implementations.http.apache.mime;

import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.http.CcpHttpMethods;

class ApacheMimeHttpRequester implements CcpHttpRequester {

	
	public CcpHttpResponse executeHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, String body) {
		HttpMethod verb = HttpMethod.valueOf(method.name());
		HttpRequestBase metodo = verb.getMethod(url, body);
		
		Set<String> keySet = headers.fieldSet();
		for (String headerName : keySet) { 
			String header = headers.getDynamicVersion().getAsString(headerName);
			metodo.addHeader(headerName, header);
		}
		
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
			CcpHttpResponse ccpHttpResponse = new CcpHttpResponse(string, statusCode);
			return ccpHttpResponse;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
