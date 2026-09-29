package com.ccp.implementations.http.apache.mime;

import org.apache.http.entity.ContentType;

/**
 * Mapping of the content types supported for multipart ({@code TEXT_PLAIN}, {@code TEXT_HTML}),
 * exposing the matching Apache HttpClient {@code ContentType}.
 */
public enum CustomContentType {
	TEXT_PLAIN(ContentType.TEXT_PLAIN),
	TEXT_HTML(ContentType.TEXT_HTML),
	;
	
	
	private CustomContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	final ContentType contentType;
}
