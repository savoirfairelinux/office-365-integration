package com.savoirfairelinux.liferay.module.o365.api;

import com.savoirfairelinux.liferay.module.o365.core.api.AuthenticatedService;
import com.savoirfairelinux.liferay.module.o365.core.model.O365Authentication;

import java.time.ZonedDateTime;

/**
 * Wrapper service to retrieve calendar data from Microsoft API
 */
public interface CalendarService extends AuthenticatedService
{
	/**
	 * Get the time of the next event until the end of the day. In progress events won’t be returned by this method.
	 */
	ZonedDateTime getNextEvent(O365Authentication authentication);
	
	void createEvent(O365Authentication authentication, String name, String description, String location, ZonedDateTime startTime, ZonedDateTime endTime);
}
