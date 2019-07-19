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

import com.savoirfairelinux.liferay.module.o365.core.api.AuthenticatedService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Component(service = AuthenticatedServiceTracker.class)
public class AuthenticatedServiceTracker {
	private final List<AuthenticatedService> authenticatedServices = new CopyOnWriteArrayList<>();
	private String scopeCache;
	
	@Reference(
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC)
	void addService(AuthenticatedService authenticatedService) {
		authenticatedServices.add(authenticatedService);
		scopeCache = null;
	}
	
	void removeService(AuthenticatedService authenticatedService) {
		authenticatedServices.remove(authenticatedService);
		scopeCache = null;
	}
	
	public String getScope(){
		String currentScope = scopeCache;
		if(currentScope==null){
			
			synchronized(this){
				if(scopeCache==null) {
					Set<String> tempScope = new HashSet<>();
					for (AuthenticatedService authenticatedService : authenticatedServices) {
						tempScope.addAll(Arrays.asList(authenticatedService.getRequiredScope().split(" ")));
					}
					currentScope = scopeCache = String.join(" ", tempScope);
				} else {
					currentScope = scopeCache;
				}
			}
			
		}
		return currentScope;
	}
}

