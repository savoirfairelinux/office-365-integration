package com.savoirfairelinux.liferay.module.o365.core.service;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;
import com.savoirfairelinux.liferay.module.o365.core.model.O365Authentication;

/**
 * Authentication providers required to use the Microsoft Graph Client API.
 * This adds user authentication to the requests sent by the API
 *
 * @see <a href="https://github.com/microsoftgraph/msgraph-sdk-java/blob/dev/README.md">Microsoft Graph SDK for Java</a>
 */
final class ScribejavaAuthenticationProvider implements IAuthenticationProvider {
	private final O365Authentication authentication;
	private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
	private static final String OAUTH_BEARER_PREFIX = "bearer ";

	ScribejavaAuthenticationProvider(O365Authentication authentication) {
		if(authentication == null || authentication.getAccessToken()==null){
			throw new RuntimeException("User need to be logged in Office 365 before calling the API." );
		}
		this.authentication = authentication;
	}

	@Override
	public void authenticateRequest(IHttpRequest request) {
		OAuth2AccessToken accessToken = (OAuth2AccessToken) authentication.getAccessToken();
		request.addHeader(AUTHORIZATION_HEADER_NAME, OAUTH_BEARER_PREFIX + accessToken.getAccessToken());
	}
}