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

package com.savoirfairelinux.liferay.module.o365.core.service;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.savoirfairelinux.liferay.module.o365.core.adapter.O365AuthenticationDaemonLiferayAdapter;
import com.savoirfairelinux.liferay.module.o365.core.api.DaemonAuthenticatedService;
import com.savoirfairelinux.liferay.module.o365.core.api.DaemonAuthenticationService;
import com.savoirfairelinux.liferay.module.o365.core.model.O365Authentication;

public class DaemonAuthenticatedServiceImpl implements DaemonAuthenticatedService {
	
	
	@Override
	public O365Authentication getAuthentication(DaemonAuthenticationService authenticationService) {
		O365Authentication o365Auth = new O365AuthenticationDaemonLiferayAdapter();
		authenticationService.validateIdToken(o365Auth);
		
		return o365Auth;
	}
	
	/**
	 * Give access to the graphClient api configured with the provided authentication
	 *
	 * @return The graphClient api
	 */
	protected IGraphServiceClient getGraphClient(DaemonAuthenticationService authenticationService) {
		IAuthenticationProvider authenticationProvider = new ScribejavaAuthenticationProvider(getAuthentication(authenticationService));
		
		return GraphServiceClient
				       .builder()
				       .authenticationProvider(authenticationProvider)
				       .buildClient();
	}
}
