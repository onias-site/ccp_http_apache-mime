package com.ccp.implementations.http.apache.mime;

import java.util.Set;

import org.apache.http.client.methods.HttpDelete;
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
		
		public HttpRequestBase getMethod(String url, String body) {
			HttpPost method = new HttpPost(url);
			method.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
			return method;
		}
	},
	GET {
		
		public HttpRequestBase getMethod(String url, String body) {
			return new HttpGet(url);
		}
	},
	PUT {
		
		public HttpRequestBase getMethod(String url, String body) {
			HttpPut method = new HttpPut(url);
			method.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
			return method;
		}
	},
	PATCH {
		
		public HttpRequestBase getMethod(String url, String body) {
			HttpPatch method = new HttpPatch(url);
			method.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
			return method;
		}
	},
	DELETE {
		
		public HttpRequestBase getMethod(String url, String body) {
			HttpDelete method = new HttpDelete(url);
			return method;
		}
	},
	HEAD {
		
		public HttpRequestBase getMethod(String url, String body) {
			HttpHead httpHead = new HttpHead(url);
			return httpHead;
		}
	},
	;
	
	public HttpRequestBase getMethod(String url, CcpJsonRepresentation headers, String body) {
		HttpRequestBase method = this.getMethod(url, body);
		Set<String> keySet = headers.fieldSet();
		for (String headerName : keySet) {
			String headerValue = headers.getDynamicVersion().getAsString(headerName);
			method.addHeader(headerName, headerValue);
		}
		return method;
	}
	
	public abstract HttpRequestBase getMethod(String url, String body);
	
}
