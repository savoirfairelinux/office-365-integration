/*
 * Copyright (c) 2019 Savoir-faire Linux Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU LesserGeneral Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU LesserGeneral Public License for more details.
 *
 * You should have received a copy of the GNU LesserGeneral Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
			throw new RuntimeException("User needs to be logged in Office 365 before calling the API." );
		}
		this.authentication = authentication;
	}

	@Override
	public void authenticateRequest(IHttpRequest request) {
		OAuth2AccessToken accessToken = (OAuth2AccessToken) authentication.getAccessToken();
		request.addHeader(AUTHORIZATION_HEADER_NAME, OAUTH_BEARER_PREFIX + accessToken.getAccessToken());
	}
}