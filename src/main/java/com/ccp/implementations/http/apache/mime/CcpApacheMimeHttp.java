package com.ccp.implementations.http.apache.mime;

import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.http.CcpHttpRequester;

/**
 * Provedor de DI que expõe {@code ApacheMimeHttpRequester} como implementação de {@code CcpHttpRequester}.
 */
public class CcpApacheMimeHttp implements CcpInstanceProvider<CcpHttpRequester> {

	public CcpHttpRequester getInstance() {
		return new ApacheMimeHttpRequester();
	}
}
