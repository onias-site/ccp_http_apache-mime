package com.ccp.implementations.http.apache.mime;

import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.http.CcpHttpRequester;

/**
 * DI provider that exposes {@code ApacheMimeHttpRequester} as the {@code CcpHttpRequester} implementation.
 */
public class CcpApacheMimeHttp implements CcpInstanceProvider<CcpHttpRequester> {

	public CcpHttpRequester getInstance() {
		ApacheMimeHttpRequester apacheMimeHttpRequester = new ApacheMimeHttpRequester();
		return apacheMimeHttpRequester;
	}
}
