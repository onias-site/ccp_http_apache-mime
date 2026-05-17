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

import com.ccp.decorators.CcpJsonRepresentation;

enum HttpMethod {

	POST { 
		
		public HttpRequestBase getMethodWithBody(String url, String body) {
			HttpPost method = new HttpPost(url);
			method.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
			return method;
		}

		public HttpEntityEnclosingRequestBase getMethodWithoutBody(String url) {
			var method = new HttpPost(url);
			return method;
		}
	},
	GET {
		
		public HttpRequestBase getMethodWithBody(String url, String body) {
			return new HttpGet(url);
		}
		public HttpEntityEnclosingRequestBase getMethodWithoutBody(String url) {
			throw new UnsupportedOperationException();
		}
	},
	PUT {
		
		public HttpRequestBase getMethodWithBody(String url, String body) {
			HttpPut method = new HttpPut(url);
			method.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
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
			method.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
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
			throw new UnsupportedOperationException();
		}
	},
	HEAD {
		
		public HttpRequestBase getMethodWithBody(String url, String body) {
			HttpHead httpHead = new HttpHead(url);
			return httpHead;
		}
		public HttpEntityEnclosingRequestBase getMethodWithoutBody(String url) {
			throw new UnsupportedOperationException();
		}
	},
	;
	
	public HttpRequestBase getMethod(String url, CcpJsonRepresentation headers, String body) {
		HttpRequestBase method = this.getMethodWithBody(url, body);
		Set<String> keySet = headers.fieldSet();
		for (String headerName : keySet) {
			String headerValue = headers.getDynamicVersion().getAsString(headerName);
			method.addHeader(headerName, headerValue);
		}
		return method;
	}
	
	public abstract HttpRequestBase getMethodWithBody(String url, String body);
	
	public abstract HttpEntityEnclosingRequestBase getMethodWithoutBody(String url);
	
}
