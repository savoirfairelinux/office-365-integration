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

package com.savoirfairelinux.liferay.module.o365.portlet;


import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.savoirfairelinux.liferay.module.o365.api.CalendarService;
import com.savoirfairelinux.liferay.module.o365.api.EmailService;
import com.savoirfairelinux.liferay.module.o365.api.UsersService;
import com.savoirfairelinux.liferay.module.o365.core.api.AuthenticationService;
import com.savoirfairelinux.liferay.module.o365.core.model.O365Authentication;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This is a test portlet that should only be used to test the user’s authentication. This should be used as the main
 * way to login users with office 365.
 */
@Component(
		service = Portlet.class,
		immediate = true,
		property = { 
				"com.liferay.portlet.display-category=category.test",
				"com.liferay.portlet.instanceable=false", 
				"javax.portlet.name=com_savoirfairelinux_liferay_module_o365_portlet_o365TestPortlet",
				"javax.portlet.display-name=o365 test portlet",
				"javax.portlet.init-param.view-template=/WEB-INF/jsp/view.jsp", 
	})
public class O365TestController extends MVCPortlet {
	private static final Logger LOG = LoggerFactory.getLogger(O365TestController.class);
	
	@Reference
	private AuthenticationService authenticationService;
	@Reference
	private EmailService emailService;
	@Reference
	private CalendarService calendarService;
	@Reference
	private UsersService usersService;
	
	@Reference
	Portal portalUtil;
	
	@Override
	public void doView(RenderRequest renderRequest, RenderResponse renderResponse)
			throws IOException, PortletException {
		O365Authentication authentication = authenticationService.getAuthentication(renderRequest);
		
		boolean isConnected = authenticationService.isConnected(authentication, "Mail.Read Calendars.ReadWrite");
		LOG.debug("isconnected: {}", isConnected);
		renderRequest.setAttribute("isConnected", isConnected);
		ThemeDisplay themeDisplay  = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
		renderRequest.setAttribute("backURL", HttpUtil.encodePath(themeDisplay.getURLCurrent()));
		
		if(isConnected) {
			int unreadMail = 0;
			try {
				unreadMail = emailService.getNumberOfInboxUnreadMail(authentication);
			} catch (Exception e) {
				LOG.error("cannot retrieve emails", e);
			}
			renderRequest.setAttribute("unreadMail", unreadMail);
			
			ZonedDateTime nextEvent = null;
			try {
				nextEvent = calendarService.getNextEvent(authentication, ZoneId.of(portalUtil.getUser(renderRequest).getTimeZoneId()));
			} catch (Exception e) {
				LOG.error("cannot retrieve events", e);
			}
			if(nextEvent!=null){
				String nextEventTime = null;
				try {
					nextEventTime = nextEvent.format(DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of(portalUtil.getUser(renderRequest).getTimeZoneId())));
				} catch (PortalException e) {
					LOG.error("cannot format events", e);
				}
				renderRequest.setAttribute("nextEventTime", nextEventTime);
			} else {
				renderRequest.setAttribute("nextEventTime", "free time today");
			}
		}
		
		renderRequest.setAttribute("users", usersService.getUsersList());
		
		super.doView(renderRequest, renderResponse);
	}
	
	public void removeAuth(ActionRequest actionRequest, ActionResponse actionResponse) {
		HttpServletRequest httpServletRequest = PortalUtil.getHttpServletRequest(actionRequest);
		HttpServletRequest originalServletRequest = PortalUtil.getOriginalServletRequest(httpServletRequest);
		HttpSession httpSession = originalServletRequest.getSession();
		httpSession.setAttribute("O365_ACCESS_TOKEN",null);
	}
	
	public void removeSession(ActionRequest actionRequest, ActionResponse actionResponse) {
		HttpServletRequest httpServletRequest = PortalUtil.getHttpServletRequest(actionRequest);
		HttpServletRequest originalServletRequest = PortalUtil.getOriginalServletRequest(httpServletRequest);
		HttpSession httpSession = originalServletRequest.getSession();
		httpSession.setAttribute("O365_ACCESS_TOKEN",null);
		httpSession.setAttribute("O365_ACCESS_TOKENexpires_at",null);
	}
	
	public void expireSession(ActionRequest actionRequest, ActionResponse actionResponse) {
		PortalPreferences userPreference;
		HttpServletRequest httpServletRequest = PortalUtil.getHttpServletRequest(actionRequest);
		HttpServletRequest originalServletRequest = PortalUtil.getOriginalServletRequest(httpServletRequest);
		HttpSession httpSession = originalServletRequest.getSession();
		httpSession.setAttribute("O365_ACCESS_TOKENexpires_at",Instant.now().minusSeconds(10).getEpochSecond());
		long userId = GetterUtil.getLong(httpSession.getAttribute("USER_ID"));
		try {
			User user = UserLocalServiceUtil.getUser(userId);
			if(!user.isDefaultUser()){
				userPreference = PortletPreferencesFactoryUtil.getPortalPreferences(userId, true);
				userPreference.setValue("com.savoirfairelinux.liferay.module.o365", "expires_at", String.valueOf(Instant.now().minusSeconds(10).getEpochSecond()));
			}
		} catch (PortalException e) {
			LOG.error("cannot retrieve current user", e);
		}
	}
	
	public void removeProperties(ActionRequest actionRequest, ActionResponse actionResponse) {
		PortalPreferences userPreference;
		HttpServletRequest httpServletRequest = PortalUtil.getHttpServletRequest(actionRequest);
		HttpServletRequest originalServletRequest = PortalUtil.getOriginalServletRequest(httpServletRequest);
		HttpSession httpSession = originalServletRequest.getSession();
		long userId = GetterUtil.getLong(httpSession.getAttribute("USER_ID"));
		try {
			if(userId>0){
				User user = UserLocalServiceUtil.getUser(userId);
				if(!user.isDefaultUser()){
					userPreference = PortletPreferencesFactoryUtil.getPortalPreferences(userId, true);
					userPreference.setValue("com.savoirfairelinux.liferay.module.o365", "refreshToken", null);
					userPreference.setValue("com.savoirfairelinux.liferay.module.o365", "expires_at", null);
				}
			}
		} catch (PortalException e) {
			LOG.error("cannot retrieve current user", e);
		}
	}
	
	public void createEvent(ActionRequest actionRequest, ActionResponse actionResponse) {
		O365Authentication authentication = authenticationService.getAuthentication(actionRequest);
		ZonedDateTime beginTime = ZonedDateTime.now();
		ZonedDateTime endTime = ZonedDateTime.now().plusHours(2);
		calendarService.createEvent(authentication, "name", "description", "la-bas", beginTime, endTime);
	}
}