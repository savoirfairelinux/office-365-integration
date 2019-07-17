package com.savoirfairelinux.liferay.module.o365.core.model;

import java.io.Serializable;
import java.time.Instant;


public interface O365Authentication {

	Serializable getAccessToken();
	
	String getRefreshToken();
	
	Instant getAccessTokenExpireAt();
	
	void setAccessToken(Serializable accessToken);
	
	String getCallBackURL();
}
