package com.savoirfairelinux.liferay.module.o365.core.adapter;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.savoirfairelinux.liferay.module.o365.core.model.O365Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.time.Instant;

/**
 * This adapter provides a way to persist the users authentication data to use the in a transparent way by the client application.
 *
 * The accessToken is stored in the httpSession of the current user
 * The refreshToken is stored in the logged-in user’s profile
 * The accessToken expiration time is stored in both httpSession and in the logged-in user’s profile.
 */
public class O365AuthenticationLiferayAdapter implements O365Authentication{
	
	private static final Logger LOG = LoggerFactory.getLogger(O365AuthenticationLiferayAdapter.class);
	private static final String NAMESPACE = "com.savoirfairelinux.liferay.module.o365";
	private static final String O365_ACCESS_TOKEN = "O365_ACCESS_TOKEN";
	private static final String REFRESH_TOKEN = "refreshToken";
	private static final String EXPIRES_AT = "expires_at";
	
	private final HttpSession httpSession;
	private final String callBackUrl;
	
	public O365AuthenticationLiferayAdapter(HttpServletRequest request) {
		HttpServletRequest originalServletRequest = PortalUtil.getOriginalServletRequest(request);
		this.httpSession = originalServletRequest.getSession();
		
		StringBuffer requestURL = request.getRequestURL();
		requestURL.delete(requestURL.indexOf("/", 8), Integer.MAX_VALUE);
		requestURL.append("/o/o365/login");
		
		String tempCallBackUrl = requestURL.toString();
		if(!tempCallBackUrl.contains("localhost")){
			tempCallBackUrl = tempCallBackUrl.replace("http:", "https:");
		}
		callBackUrl = tempCallBackUrl;
	}

	public O365AuthenticationLiferayAdapter(PortletRequest request) {
		this(PortalUtil.getHttpServletRequest(request));
	}
	
	@Override
	public Serializable getAccessToken() {
		return (Serializable) httpSession.getAttribute(O365_ACCESS_TOKEN);
	}
	
	private OAuth2AccessToken getOAuthAccessToken() {
		return (OAuth2AccessToken) httpSession.getAttribute(O365_ACCESS_TOKEN);
	}
	
	@Override
	public String getRefreshToken() {
		OAuth2AccessToken accessToken = getOAuthAccessToken();
		if(accessToken!=null){
			return accessToken.getRefreshToken();
		}
		PortalPreferences userPreference = getUserPreference();
		if(userPreference != null) {
			return userPreference.getValue(NAMESPACE, REFRESH_TOKEN);
		}
		return "";
	}
	
	@Override
	public Instant getAccessTokenExpireAt() {
		Long expireAt = (Long) httpSession.getAttribute(O365_ACCESS_TOKEN+EXPIRES_AT);
		if(Validator.isNull(expireAt)){
			PortalPreferences userPreference = getUserPreference();
			if(userPreference != null) {
				expireAt = Long.parseLong(userPreference.getValue(NAMESPACE, EXPIRES_AT, "0"));
			}
		}
		if(Validator.isNull(expireAt)){
			return Instant.now().minusSeconds(1);
		}
		return Instant.ofEpochSecond(expireAt);
	}
	
	@Override
	public void setAccessToken(Serializable accessToken) {
		httpSession.setAttribute(O365_ACCESS_TOKEN, accessToken);
		
		PortalPreferences userPreference = getUserPreference();
		if(userPreference != null){
			userPreference.setValue(NAMESPACE, REFRESH_TOKEN, getRefreshToken());
		
			Integer expiresIn = getOAuthAccessToken().getExpiresIn();
			Instant expiresAt = Instant.now().plusSeconds(expiresIn-30);
			userPreference.setValue(NAMESPACE, EXPIRES_AT, String.valueOf(expiresAt.getEpochSecond()));
			httpSession.setAttribute(O365_ACCESS_TOKEN+EXPIRES_AT, expiresAt.getEpochSecond());
		}
	}

	private PortalPreferences getUserPreference(){
		long userId = GetterUtil.getLong(httpSession.getAttribute("USER_ID"));
		if(userId>0) {
			try {
				User user = UserLocalServiceUtil.getUser(userId);
				if (!user.isDefaultUser()) {
					return PortletPreferencesFactoryUtil.getPortalPreferences(userId, true);
				}
			} catch (PortalException e) {
				LOG.error("cannot retrieve current user", e);
			}
		}
		return null;
	}
	
	@Override
	public String getCallBackURL(){
		return callBackUrl;
	}
}
