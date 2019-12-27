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

import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.savoirfairelinux.liferay.module.o365.core.api.DaemonAuthenticatedService;
import com.savoirfairelinux.liferay.module.o365.core.api.DaemonAuthenticationService;
import com.savoirfairelinux.liferay.module.o365.core.model.O365Authentication;
import com.savoirfairelinux.liferay.module.o365.core.service.config.O365Configuration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component(
		configurationPid = O365Configuration.CONFIGURATION_PID,
        service = {DaemonAuthenticationService.class, DaemonAuthenticatedService.class},
        immediate = true
)
public class DaemonAuthenticationServiceImpl extends DaemonAuthenticatedServiceImpl implements DaemonAuthenticationService
{
	private static final Logger LOG = LoggerFactory.getLogger(DaemonAuthenticationServiceImpl.class);
	
	/**
	 * The osgi configuration can be accessed to any sub service classes.
	 */
	private volatile O365Configuration configuration;
	
	@Activate
	@Modified
	public synchronized void activate(Map<String, String> properties) {
		this.configuration = ConfigurableUtil.createConfigurable(O365Configuration.class, properties);
	}
	
	@Override
	public boolean validateIdToken(O365Authentication authentication) {
		OAuth2AccessToken accessToken;
		try (OAuth20Service authService = getAuthService()){
			accessToken = authService.getAccessTokenClientCredentialsGrant();
		} catch (IOException | InterruptedException | ExecutionException e) {
			LOG.error("Cannot validate token", e);
			return false;
		}
		
		authentication.setAccessToken(accessToken);
		return true;
	}
	
	
	@Override
	public boolean isConnected(O365Authentication authentication) {
		OAuth2AccessToken accessToken = (OAuth2AccessToken) authentication.getAccessToken();
		boolean accessValid = true;
		if(accessToken == null){
			accessValid = false;
		}
		
	    if(authentication.getAccessTokenExpireAt().isBefore(Instant.now())){
			accessValid = false;
		}
		
		if(!accessValid){
			validateIdToken(authentication);
			accessValid = authentication.getAccessToken()!=null;
			
		}
		
		return accessValid;
	}
	
	private OAuth20Service getAuthService() {
		try {
			LOG.debug("create daemon connection with apiKey: {} apiSecret: {}", configuration.apiKey(), configuration.apiSecret());
			return new ServiceBuilder(configuration.apiKey())
					       .withScope("https://graph.microsoft.com/.default")
					       .apiKey(configuration.apiKey())
					       .apiSecret(configuration.apiSecret())
					       .build(MicrosoftAzureActiveDirectory20Api.custom(configuration.tenant()));
		} catch (Exception e) {
			LOG.error("Office 365 authentication is misconfigured, original error was : {}", e.getMessage());
			LOG.debug("Office 365 authentication detail misconfiguration", e);
			throw new RuntimeException("Office 365 authentication is misconfigured", e);
		}
	}
}
