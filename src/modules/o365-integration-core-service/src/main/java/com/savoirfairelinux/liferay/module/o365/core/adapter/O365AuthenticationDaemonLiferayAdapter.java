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

package com.savoirfairelinux.liferay.module.o365.core.adapter;

import com.savoirfairelinux.liferay.module.o365.core.model.O365Authentication;

import java.io.Serializable;
import java.time.Instant;

/**
 * This adapter provides a way to persist the users authentication data to use the in a transparent way by the client application.
 *
 * The accessToken is stored in the httpSession of the current user
 * The refreshToken is stored in the logged-in user’s profile
 * The accessToken expiration time is stored in both httpSession and in the logged-in user’s profile.
 */
public class O365AuthenticationDaemonLiferayAdapter implements O365Authentication{
	
	private Serializable accessToken;
	
	public O365AuthenticationDaemonLiferayAdapter() {
	}

	
	@Override
	public Serializable getAccessToken() {
		return accessToken;
	}
	
	
	@Override
	public String getRefreshToken() {
		return "";
	}
	
	@Override
	public Instant getAccessTokenExpireAt() {
		return Instant.now().plusSeconds(10);
	}
	
	@Override
	public void setAccessToken(Serializable accessToken) {
		this.accessToken = accessToken;
	}

	
	@Override
	public String getCallBackURL(){
		return "";
	}
}
