package com.savoirfairelinux.liferay.module.o365.core.api;

import com.savoirfairelinux.liferay.module.o365.core.model.O365Authentication;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Base service that give access to the authentication to implementing service. This interface should extend any service
 * interface that require an authentication
 */
public interface AuthenticatedService
{
	
	/**
	 * @param request used to initialise the Authentication
	 * @return an o365Authentication for the current user
	 */
	O365Authentication getAuthentication(HttpServletRequest request);
	
	/**
	 * @param portletRequest used to initialise the Authentication
	 * @return an o365Authentication for the current user
	 */
	O365Authentication getAuthentication(PortletRequest portletRequest);
	
	String getRequiredScope();
}
