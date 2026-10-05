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
 * Enum that maps the HTTP verbs (POST, GET, PUT, PATCH, DELETE, HEAD) to the matching Apache
 * HttpClient {@code HttpRequestBase} objects, with or without a request body.
 */
enum HttpMethod {

	/** HTTP POST. */
	POST { 
		
		/**
		 * Builds a POST with the JSON body.
		 * @param url the target URL
		 * @param body the JSON body
		 * @return the request
		 */
		public HttpRequestBase getMethodWithBody(String url, String body) {
			HttpPost method = new HttpPost(url);
			StringEntity stringEntity = new StringEntity(body, ContentType.APPLICATION_JSON);
			method.setEntity(stringEntity);
			return method;
		}

		/**
		 * Builds a POST without body.
		 * @param url the target URL
		 * @return the request
		 */
		public HttpEntityEnclosingRequestBase getMethodWithoutBody(String url) {
			var method = new HttpPost(url);
			return method;
		}
	},
	/** HTTP GET. */
	GET {
		
		/**
		 * Builds a GET; the body is ignored.
		 * @param url the target URL
		 * @param body ignored
		 * @return the request
		 */
		public HttpRequestBase getMethodWithBody(String url, String body) {
			HttpGet httpGet = new HttpGet(url);
			return httpGet;
		}
		/**
		 * A GET cannot carry a multipart body.
		 * @param url the target URL
		 * @return never returns
		 * @throws UnsupportedOperationException always
		 */
		public HttpEntityEnclosingRequestBase getMethodWithoutBody(String url) {
			UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException();
			throw unsupportedOperationException;
		}
	},
	/** HTTP PUT. */
	PUT {
		
		/**
		 * Builds a PUT with the JSON body.
		 * @param url the target URL
		 * @param body the JSON body
		 * @return the request
		 */
		public HttpRequestBase getMethodWithBody(String url, String body) {
			HttpPut method = new HttpPut(url);
			StringEntity stringEntity = new StringEntity(body, ContentType.APPLICATION_JSON);
			method.setEntity(stringEntity);
			return method;
		}
		/**
		 * Builds a PUT without body.
		 * @param url the target URL
		 * @return the request
		 */
		public HttpEntityEnclosingRequestBase getMethodWithoutBody(String url) {
			var method = new HttpPut(url);
			return method;
		}
	},
	/** HTTP PATCH. */
	PATCH {
		
		/**
		 * Builds a PATCH with the JSON body.
		 * @param url the target URL
		 * @param body the JSON body
		 * @return the request
		 */
		public HttpRequestBase getMethodWithBody(String url, String body) {
			HttpPatch method = new HttpPatch(url);
			StringEntity stringEntity = new StringEntity(body, ContentType.APPLICATION_JSON);
			method.setEntity(stringEntity);
			return method;
		}
		/**
		 * Builds a PATCH without body.
		 * @param url the target URL
		 * @return the request
		 */
		public HttpEntityEnclosingRequestBase getMethodWithoutBody(String url) {
			var method = new HttpPatch(url);
			return method;
		}
	},
	/** HTTP DELETE. */
	DELETE {
		
		/**
		 * Builds a DELETE; the body is ignored.
		 * @param url the target URL
		 * @param body ignored
		 * @return the request
		 */
		public HttpRequestBase getMethodWithBody(String url, String body) {
			HttpDelete method = new HttpDelete(url);
			return method;
		}
		/**
		 * A DELETE cannot carry a multipart body.
		 * @param url the target URL
		 * @return never returns
		 * @throws UnsupportedOperationException always
		 */
		public HttpEntityEnclosingRequestBase getMethodWithoutBody(String url) {
			UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException();
			throw unsupportedOperationException;
		}
	},
	/** HTTP HEAD. */
	HEAD {
		
		/**
		 * Builds a HEAD; the body is ignored.
		 * @param url the target URL
		 * @param body ignored
		 * @return the request
		 */
		public HttpRequestBase getMethodWithBody(String url, String body) {
			HttpHead httpHead = new HttpHead(url);
			return httpHead;
		}
		/**
		 * A HEAD cannot carry a multipart body.
		 * @param url the target URL
		 * @return never returns
		 * @throws UnsupportedOperationException always
		 */
		public HttpEntityEnclosingRequestBase getMethodWithoutBody(String url) {
			UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException();
			throw unsupportedOperationException;
		}
	},
	;
	
	/**
	 * Builds the request with the body and the headers.
	 * @param url the target URL
	 * @param headers the request headers
	 * @param body the request body
	 * @return the request
	 */
	public HttpRequestBase getMethod(String url, CcpJsonRepresentation headers, String body) {
		HttpRequestBase method = this.getMethodWithBody(url, body);
		Set<String> keySet = headers.fieldSet();
		for (String headerName : keySet) {
			CcpFieldName headerFieldName = new CcpFieldName(headerName);
			String headerValue = headers.getAsString(headerFieldName);
			method.addHeader(headerName, headerValue);
		}
		return method;
	}
	
	/**
	 * Builds the request with the JSON body (ignored by the verbs without body).
	 * @param url the target URL
	 * @param body the JSON body
	 * @return the request
	 */
	public abstract HttpRequestBase getMethodWithBody(String url, String body);
	
	/**
	 * Builds a body-capable request without body, to receive a multipart entity.
	 * @param url the target URL
	 * @return the request
	 * @throws UnsupportedOperationException for the verbs without body
	 */
	public abstract HttpEntityEnclosingRequestBase getMethodWithoutBody(String url);
	
}
