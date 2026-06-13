package com.ccp.implementations.http.apache.mime;

import org.apache.http.entity.ContentType;

/**
 * Mapeamento dos content-types suportados para multipart ({@code TEXT_PLAIN}, {@code TEXT_HTML})
 * expondo o {@code ContentType} correspondente do Apache HttpClient.
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
