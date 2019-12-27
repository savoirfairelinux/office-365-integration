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

package com.savoirfairelinux.liferay.module.o365.service;

import com.microsoft.graph.models.extensions.DateTimeTimeZone;
import com.microsoft.graph.models.extensions.Event;
import com.microsoft.graph.models.extensions.ItemBody;
import com.microsoft.graph.models.extensions.Location;
import com.microsoft.graph.models.generated.BodyType;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.IEventCollectionPage;
import com.savoirfairelinux.liferay.module.o365.api.CalendarService;
import com.savoirfairelinux.liferay.module.o365.core.api.AuthenticatedService;
import com.savoirfairelinux.liferay.module.o365.core.model.O365Authentication;
import com.savoirfairelinux.liferay.module.o365.core.service.BaseAuthenticatedServiceImpl;
import org.osgi.service.component.annotations.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

@Component(
        service = {CalendarService.class, AuthenticatedService.class},
        immediate = true
)
public class CalendarServiceImpl extends BaseAuthenticatedServiceImpl implements CalendarService
{
	
	@Override
	public String getRequiredScope() {
		return "User.Read Calendars.ReadWrite";
	}
	
	@Override
	public ZonedDateTime getNextEvent(O365Authentication authentication, ZoneId userTimeZoneId) {
		List<Option> options = new LinkedList<>();
		options.add(new QueryOption("startDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
		
		ZonedDateTime startOfNextDay = ZonedDateTime.now(userTimeZoneId).plusDays(1).toLocalDate().atStartOfDay(userTimeZoneId);
		String startOfNextDayGMT = startOfNextDay.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.of("GMT")));
		options.add(new QueryOption("endDateTime", startOfNextDayGMT));
		IEventCollectionPage events = getGraphClient(authentication).me()
				                   .calendar().calendarView()
				                   .buildRequest(options)
				                   .get();
		
		List<Event> currentEvents = events.getCurrentPage();
		for (Event nextEvent : currentEvents) {
			ZonedDateTime eventTime = msDateToZonedDate(nextEvent.start);
			if(ZonedDateTime.now().isAfter(eventTime)){
				continue;
			}
			return eventTime;
		}
		
		return null;
	}
	
	@Override
	public void createEvent(O365Authentication authentication, String name, String description, String location, ZonedDateTime startTime, ZonedDateTime endTime) {
		Event event = new Event();
		event.subject = name;
		event.body = createItemBody(description);
		event.location = createLocation(location);
		event.start = zonedDateToMsDate(startTime);
		event.end = zonedDateToMsDate(endTime);
		
		getGraphClient(authentication).me().events().buildRequest().post(event);
	}
	
	private Location createLocation(String location) {
		Location msLocation = new Location();
		msLocation.displayName = location;
		return msLocation;
	}
	
	private ItemBody createItemBody(String content) {
		ItemBody itemBody = new ItemBody();
		itemBody.content = content;
		itemBody.contentType = BodyType.HTML;
		return itemBody;
	}
	
	private ZonedDateTime msDateToZonedDate(DateTimeTimeZone dateTime) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of(dateTime.timeZone));
		return ZonedDateTime.parse(dateTime.dateTime, dateTimeFormatter);
	}
	
	private DateTimeTimeZone zonedDateToMsDate(ZonedDateTime dateTime) {
		DateTimeTimeZone dateTimeTimeZone = new DateTimeTimeZone();
		dateTimeTimeZone.dateTime = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		dateTimeTimeZone.timeZone = dateTime.getZone().getId();
		
		return dateTimeTimeZone;
	}
}
