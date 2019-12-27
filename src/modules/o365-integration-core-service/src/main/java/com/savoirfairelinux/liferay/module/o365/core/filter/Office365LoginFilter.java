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

package com.savoirfairelinux.liferay.module.o365.core.filter;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.savoirfairelinux.liferay.module.o365.core.api.AuthenticationService;
import com.savoirfairelinux.liferay.module.o365.core.model.O365Authentication;
import com.savoirfairelinux.liferay.module.o365.core.service.AuthenticatedServiceTracker;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Login filter that manages the office 365 authentication process. Any user that is directed to the /o/o365/login path
 * will follow the login process and be returned to the `backURL` URL parameter that was first provided. If none was
 * provided, the user will be returned to the root "/" page.
 *
 * @see <a href="https://docs.microsoft.com/fr-fr/azure/active-directory/develop/v1-protocols-openid-connect-code">
 *     Authorize access to web applications using OpenID Connect and Azure Active Directory</a>
 */
@Component(
	immediate = true,
	property = {
		"servlet-context-name=",
		"servlet-filter-name=office365-login-filter",
		"url-pattern=/o/o365/login",
	},
	service = Filter.class
)
public final class Office365LoginFilter extends BaseFilter {
	
	private static final Log LOG = LogFactoryUtil.getLog(Office365LoginFilter.class);
	
	@Reference
	private AuthenticationService authenticationService;
	
	@Reference
	private AuthenticatedServiceTracker authenticatedServiceTracker;
	
	@Override
	protected void processFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws Exception {
		try {
			String code = ParamUtil.get(request, "code", "");
			O365Authentication authentication = authenticationService.getAuthentication(request);
			
			if(Validator.isNull(code)) {
				String backURL = ParamUtil.getString(request, AuthenticationService.BACK_URL_PARAM, "/");
				// step 0 - test if user is already authenticated
				if(authenticationService.isConnected(authentication, authenticatedServiceTracker.getScope())){
					String contextUrl = HttpUtil.decodeURL(backURL);
					response.sendRedirect(contextUrl);
					return;
				}
				// Step 1 - redirection to o365 authentication
				String error = ParamUtil.getString(request, "error");
				String prompt = AuthenticationService.PROMPT_NONE;
				switch (error) {
					case "login_required":
					case "interaction_required":
						prompt = AuthenticationService.PROMPT_LOGIN;
						break;
					case "consent_required":
						prompt = AuthenticationService.PROMPT_CONSENT;
						break;
					case "access_denied":
						// the user refuse to give access, we return it to the portal page
						String state = ParamUtil.getString(request, "state");
						String contextUrl = HttpUtil.decodeURL(state);
						LOG.debug("Send user to " + contextUrl);
						
						response.sendRedirect(contextUrl);
						return;
				}
				String authenticationURL = authenticationService.getAuthenticationURL(authentication, backURL, prompt);
				
				LOG.debug("Send user to "+authenticationURL);
				response.sendRedirect(authenticationURL);
			} else {
				// Step 2 - token validation
				LOG.debug("Received id_token: "+code);
				authenticationService.validateIdToken(authentication, code);
				
				String state = ParamUtil.getString(request, "state");
				String contextUrl = HttpUtil.decodeURL(state);
				LOG.debug("Send user to "+contextUrl);
				
				response.sendRedirect(contextUrl);
			}
		} catch (Exception e) {
			LOG.error("Cannot authenticate user to o365", e);
			
			String backURL = ParamUtil.getString(request, "state", "/");
			backURL = ParamUtil.getString(request, AuthenticationService.BACK_URL_PARAM, backURL);
			
			String contextUrl = HttpUtil.decodeURL(backURL);
			response.sendRedirect(contextUrl);
		}
	}
	
	@Override
	protected Log getLog() {
		return LOG;
	}
	
}
