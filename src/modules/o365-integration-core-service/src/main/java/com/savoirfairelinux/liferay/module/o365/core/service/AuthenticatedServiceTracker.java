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

