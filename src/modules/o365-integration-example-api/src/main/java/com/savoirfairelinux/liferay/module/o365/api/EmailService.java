package com.savoirfairelinux.liferay.module.o365.api;

import com.savoirfairelinux.liferay.module.o365.core.api.AuthenticatedService;
import com.savoirfairelinux.liferay.module.o365.core.model.O365Authentication;

/**
 * Wrapper service to retrieve email data from Microsoft API
 */
public interface EmailService extends AuthenticatedService
{
	/**
	 * Get the number of unread mail in the user main inbox.
	 */
	int getNumberOfInboxUnreadMail(O365Authentication authentication);
}
