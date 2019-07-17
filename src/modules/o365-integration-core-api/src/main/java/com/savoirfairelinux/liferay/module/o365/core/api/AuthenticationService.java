package com.savoirfairelinux.liferay.module.o365.core.api;

import com.savoirfairelinux.liferay.module.o365.core.model.O365Authentication;

/**
 * Wrapper service to manage the authentication process server side.
 */
public interface AuthenticationService extends AuthenticatedService
{
	/**
	 * `backURL` URL parameter that is to be used when sending the user to the /o/o365/login end point.
	 *
	 * see Office365LoginFilter
	 **/
	String BACK_URL_PARAM = "backURL";
	
	/**
	 * Value `login` for the prompt parameter
	 * Will force the user to enter their credentials on that request, negating single sign on.
	 **/
	String PROMPT_LOGIN = "login";
	
	/**
	 * Value `none` for the prompt parameter
	 * Is the opposite - it will ensure that the user isn't presented with any interactive prompt whatsoever. If the
	 * request can't be completed silently via a single sign on, the Microsoft identity platform endpoint will return an
	 * interaction_required error.
	 **/
	String PROMPT_NONE = "none";
	
	/**
	 * Value `consent` for the prompt parameter
	 * will trigger the OAuth consent dialog after the user signs in, asking the user to grant permission to the app.
	 **/
	String PROMPT_CONSENT = "consent";
	
	/**
	 * Retrieve the Office365 API authentication url.
	 *
	 * @param authentication adapter
	 * @param backURL relative or absolute url to send the user after completing authentication process
	 * @param prompt One of the PROMPT_* constant to indicates the type of user interaction that is required.
	 *
	 * @return the url to forward the user to.
	 */
	String getAuthenticationURL(O365Authentication authentication, String backURL, String prompt);
	
	/**
	 * Validate the user authentication and persist it with the provided authentication
	 *
	 * @param authentication to persist the authentication
	 * @param id_token the code forwarded by office 365 authentication
	 *
	 * @return false in case of error
	 */
	boolean validateIdToken(O365Authentication authentication, String id_token);
	
	/**
	 * Check if the user authentication is still valid. This might trigger authentication refresh server side.
	 *
	 * @param authentication of the current user
	 *
	 * @return true if the user authentication is still valid.
	 */
	boolean isConnected(O365Authentication authentication);
	
	
	/**
	 * Check if the user authentication is still valid for the specified access scope. This might trigger authentication
	 * refresh server side.
	 *
	 * @param authentication of the current user
	 * @param accessScope required for future api call
	 *
	 * @return true if the user authentication is still valid and contains the required accessScope.
	 */
	boolean isConnected(O365Authentication authentication, String accessScope);
}
