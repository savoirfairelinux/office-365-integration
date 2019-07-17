package com.savoirfairelinux.liferay.module.o365.core.service.config;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

@ExtendedObjectClassDefinition(
        scope = ExtendedObjectClassDefinition.Scope.SYSTEM,
        category = "officeauth"
)
@Meta.OCD(
		id = O365Configuration.CONFIGURATION_PID,
		name = "Office 365 configuration")
public interface O365Configuration
{
    String CONFIGURATION_PID = "com.savoirfairelinux.liferay.module.o365.core.service.config.O365Configuration";
	
	@Meta.AD(description = "The api key of the application registered in the Microsoft Azur AD", required = false)
	String apiKey();
	
	@Meta.AD(description = "The api secret of the application registered in the Microsoft Azur AD", required = false)
	String apiSecret();
	
	@Meta.AD(description = "The organisation tenant. Leave empty if using multi-tenant or common", deflt = "", required = false)
	String tenant();
}
