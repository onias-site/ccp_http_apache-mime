package com.ccp.implementations.http.apache.mime;

import org.apache.http.entity.ContentType;

/**
 * Mapping of the content types supported for multipart ({@code TEXT_PLAIN}, {@code TEXT_HTML}),
 * exposing the matching Apache HttpClient {@code ContentType}.
 */
public enum CustomContentType {
	/** {@code text/plain}. */
	TEXT_PLAIN(ContentType.TEXT_PLAIN),
	/** {@code text/html}. */
	TEXT_HTML(ContentType.TEXT_HTML),
	;
	
	
	/**
	 * Associates the constant with the Apache content type.
	 * @param contentType the Apache content type
	 */
	private CustomContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	/** The Apache content type. */
	final ContentType contentType;
}
