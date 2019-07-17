package com.savoirfairelinux.liferay.module.o365.core.service;

import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.util.Validator;
import com.savoirfairelinux.liferay.module.o365.core.api.AuthenticatedService;
import com.savoirfairelinux.liferay.module.o365.core.api.AuthenticationService;
import com.savoirfairelinux.liferay.module.o365.core.model.O365Authentication;
import com.savoirfairelinux.liferay.module.o365.core.service.config.O365Configuration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Component(
		configurationPid = O365Configuration.CONFIGURATION_PID,
        service = {AuthenticationService.class, AuthenticatedService.class},
        immediate = true
)
public class AuthenticationServiceImpl extends BaseAuthenticatedServiceImpl implements AuthenticationService
{
	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
	
	/**
	 * The osgi configuration can be accessed to any sub service classes.
	 */
	private volatile O365Configuration configuration;
	
	@Reference
	private AuthenticatedServiceTracker authenticatedServiceTracker;
	
	@Activate
	@Modified
	public synchronized void activate(Map<String, String> properties) {
		this.configuration = ConfigurableUtil.createConfigurable(O365Configuration.class, properties);
	}
	
	@Override
	public String getRequiredScope() {
		return "openid offline_access";
	}
	
	@Override
	public String getAuthenticationURL(O365Authentication authentication, String backURL, String prompt)
	{
		Map<String, String> params = new HashMap<>();
		params.put("response_mode","form_post");
		params.put("state",backURL);
		params.put("prompt",prompt);
		
		try (OAuth20Service authService = getAuthService(authentication)) {
			return authService.getAuthorizationUrl(params);
		} catch (IOException e) {
			throw new RuntimeException("Cannot invoke authService", e);
		}
	}
	
	@Override
	public boolean validateIdToken(O365Authentication authentication, String id_token) {
		OAuth2AccessToken accessToken;
		try (OAuth20Service authService = getAuthService(authentication)){
			accessToken = authService.getAccessToken(id_token);
		} catch (IOException | InterruptedException | ExecutionException e) {
			LOG.error("Cannot validate token", e);
			return false;
		}
		
		authentication.setAccessToken(accessToken);
		return true;
	}
	
	private void refreshAccessToken(O365Authentication authentication, String refreshToken) {
		
		OAuth2AccessToken accessToken = null;
		try (OAuth20Service authService = getAuthService(authentication)){
			accessToken = authService.refreshAccessToken(refreshToken);
			LOG.debug("refresh token");
		} catch (IOException | InterruptedException | ExecutionException e) {
			LOG.error("Cannot refresh token", e);
		}
		
		authentication.setAccessToken(accessToken);
	}
	
	@Override
	public boolean isConnected(O365Authentication authentication) {
		return isConnected(authentication, getRequiredScope());
	}
	
	@Override
	public boolean isConnected(O365Authentication authentication, String accessScope) {
		OAuth2AccessToken accessToken = (OAuth2AccessToken) authentication.getAccessToken();
		boolean accessValid = true;
		if(accessToken == null){
			accessValid = false;
		}
		
		if(accessValid && !isScopeValid(accessToken, accessScope)){
			return false;
		}
	    if(authentication.getAccessTokenExpireAt().isBefore(Instant.now())){
			accessValid = false;
		}
		
		if(!accessValid){
			String refreshToken = authentication.getRefreshToken();
			if(Validator.isNotNull(refreshToken)){
				refreshAccessToken(authentication, refreshToken);
				accessValid = authentication.getAccessToken()!=null;
			}
		}
		
		return accessValid;
	}
	
	private boolean isScopeValid(OAuth2AccessToken accessToken, String requestScopes) {
		List<String> tokenScope = Arrays.asList(accessToken.getScope().split(" "));
		String[] requestScopesList = requestScopes.split(" ");
		
		for (String scope : requestScopesList) {
			if(!tokenScope.contains(scope))
				return false;
		}
		return true;
	}
	
	private OAuth20Service getAuthService(O365Authentication authentication) {
		try {
			LOG.debug("create connection with apiKey: {} apiSecret: {}", configuration.apiKey(), configuration.apiSecret());
			return new ServiceBuilder(configuration.apiKey())
					       .withScope(authenticatedServiceTracker.getScope())
					       .apiKey(configuration.apiKey())
					       .apiSecret(configuration.apiSecret())
					       .callback(authentication.getCallBackURL())
					       .build(MicrosoftAzureActiveDirectory20Api.custom(configuration.tenant()));
		} catch (Exception e) {
			LOG.error("Office 365 authentication is misconfigured, original error was : {}", e.getMessage());
			LOG.debug("Office 365 authentication detail misconfiguration", e);
			throw new RuntimeException("Office 365 authentication is misconfigured", e);
		}
	}
}
