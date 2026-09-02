package com.ccp.implementations.http.apache.mime;

import java.util.Set;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Enum que mapeia os verbos HTTP (POST, GET, PUT, PATCH, DELETE, HEAD) para os objetos
 * {@code HttpRequestBase} correspondentes do Apache HttpClient, com ou sem corpo de requisição.
 */
enum HttpMethod {

	POST { 
		
		public HttpRequestBase getMethodWithBody(String url, String body) {
			HttpPost method = new HttpPost(url);
			StringEntity stringEntity = new StringEntity(body, ContentType.APPLICATION_JSON);
			method.setEntity(stringEntity);
			return method;
		}

		public HttpEntityEnclosingRequestBase getMethodWithoutBody(String url) {
			var method = new HttpPost(url);
			return method;
		}
	},
	GET {
		
		public HttpRequestBase getMethodWithBody(String url, String body) {
			HttpGet httpGet = new HttpGet(url);
			return httpGet;
		}
		public HttpEntityEnclosingRequestBase getMethodWithoutBody(String url) {
			UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException();
			throw unsupportedOperationException;
		}
	},
	PUT {
		
		public HttpRequestBase getMethodWithBody(String url, String body) {
			HttpPut method = new HttpPut(url);
			StringEntity stringEntity2 = new StringEntity(body, ContentType.APPLICATION_JSON);
			method.setEntity(stringEntity2);
			return method;
		}
		public HttpEntityEnclosingRequestBase getMethodWithoutBody(String url) {
			var method = new HttpPut(url);
			return method;
		}
	},
	PATCH {
		
		public HttpRequestBase getMethodWithBody(String url, String body) {
			HttpPatch method = new HttpPatch(url);
			StringEntity stringEntity3 = new StringEntity(body, ContentType.APPLICATION_JSON);
			method.setEntity(stringEntity3);
			return method;
		}
		public HttpEntityEnclosingRequestBase getMethodWithoutBody(String url) {
			var method = new HttpPatch(url);
			return method;
		}
	},
	DELETE {
		
		public HttpRequestBase getMethodWithBody(String url, String body) {
			HttpDelete method = new HttpDelete(url);
			return method;
		}
		public HttpEntityEnclosingRequestBase getMethodWithoutBody(String url) {
			UnsupportedOperationException unsupportedOperationException2 = new UnsupportedOperationException();
			throw unsupportedOperationException2;
		}
	},
	HEAD {
		
		public HttpRequestBase getMethodWithBody(String url, String body) {
			HttpHead httpHead = new HttpHead(url);
			return httpHead;
		}
		public HttpEntityEnclosingRequestBase getMethodWithoutBody(String url) {
			UnsupportedOperationException unsupportedOperationException3 = new UnsupportedOperationException();
			throw unsupportedOperationException3;
		}
	},
	;
	
	public HttpRequestBase getMethod(String url, CcpJsonRepresentation headers, String body) {
		HttpRequestBase method = this.getMethodWithBody(url, body);
		Set<String> keySet = headers.fieldSet();
		for (String headerName : keySet) {
			CcpFieldName ccpFieldName = new CcpFieldName(headerName);
			String headerValue = headers.getAsString(ccpFieldName);
			method.addHeader(headerName, headerValue);
		}
		return method;
	}
	
	public abstract HttpRequestBase getMethodWithBody(String url, String body);
	
	public abstract HttpEntityEnclosingRequestBase getMethodWithoutBody(String url);
	
}
