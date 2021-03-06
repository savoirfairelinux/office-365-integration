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

package com.savoirfairelinux.liferay.module.o365.api;

import com.savoirfairelinux.liferay.module.o365.core.api.AuthenticatedService;
import com.savoirfairelinux.liferay.module.o365.core.model.O365Authentication;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Wrapper service to retrieve calendar data from Microsoft API
 */
public interface CalendarService extends AuthenticatedService
{
	/**
	 * Get the time of the next event until the end of the day. In progress events won’t be returned by this method.
	 *
	 * authentication the user authentication
	 * userTimeZoneId the user time zone to determine the end of the day
	 */
	ZonedDateTime getNextEvent(O365Authentication authentication, ZoneId userTimeZoneId);
	
	/**
	 * Create a new event int the user calendar
	 *
 	 * @param authentication the user authentication
	 * @param name the event name/title
	 * @param description a description of the event
	 * @param location the location/place of the event
	 * @param startTime the start time of the event
	 * @param endTime the end time of the event
	 */
	void createEvent(O365Authentication authentication, String name, String description, String location, ZonedDateTime startTime, ZonedDateTime endTime);
}
