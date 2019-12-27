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

import com.microsoft.graph.models.extensions.User;
import com.microsoft.graph.requests.extensions.IUserCollectionPage;
import com.savoirfairelinux.liferay.module.o365.api.UsersService;
import com.savoirfairelinux.liferay.module.o365.core.api.DaemonAuthenticatedService;
import com.savoirfairelinux.liferay.module.o365.core.api.DaemonAuthenticationService;
import com.savoirfairelinux.liferay.module.o365.core.service.DaemonAuthenticatedServiceImpl;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;

@Component(
        service = {UsersService.class, DaemonAuthenticatedService.class},
        immediate = true
)
public class UsersServiceImpl extends DaemonAuthenticatedServiceImpl implements UsersService
{
	
	@Reference
	DaemonAuthenticationService authenticationService;
	
	@Override
	public List<String> getUsersList() {
		
		IUserCollectionPage users = getGraphClient(authenticationService).users()
				                   .buildRequest()
				                   .get();
		
		List<String> usersEmail = new ArrayList<>();
		
		for (User user : users.getCurrentPage()) {
			usersEmail.add(user.userPrincipalName);
		}
		
		return usersEmail;
	}
}
