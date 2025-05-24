package com.ccp.implementations.http.apache.mime;

import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.http.CcpHttpRequester;

public class CcpApacheMimeHttp implements CcpInstanceProvider<CcpHttpRequester> {

	public CcpHttpRequester getInstance() {
		return new ApacheMimeHttpRequester();
	}
}
