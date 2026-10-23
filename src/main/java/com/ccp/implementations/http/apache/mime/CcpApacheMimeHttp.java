package com.ccp.implementations.http.apache.mime;

import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.http.CcpHttpRequester;

/**
 * DI provider that exposes {@code ApacheMimeHttpRequester} as the {@code CcpHttpRequester} implementation.
 */
public class CcpApacheMimeHttp implements CcpInstanceProvider<CcpHttpRequester> {
	/** The single instance: the class keeps no state. */
	public final static CcpApacheMimeHttp INSTANCE = new CcpApacheMimeHttp();

	private CcpApacheMimeHttp() {}

	/**
	 * Builds the Apache HttpClient implementation of {@code CcpHttpRequester}.
	 * @return a new {@code ApacheMimeHttpRequester}
	 */
	public CcpHttpRequester getInstance() {
		ApacheMimeHttpRequester apacheMimeHttpRequester = ApacheMimeHttpRequester.INSTANCE;
		return apacheMimeHttpRequester;
	}
}
