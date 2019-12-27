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

package com.savoirfairelinux.liferay.module.o365.core.service.config;

import aQute.bnd.annotation.metatype.Meta;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

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
	
	@Meta.AD(description = "The api key of the application registered in the Microsoft Azure AD", required = false)
	String apiKey();
	
	@Meta.AD(description = "The api secret of the application registered in the Microsoft Azure AD", required = false)
	String apiSecret();
	
	@Meta.AD(description = "The organisation tenant. Leave empty if using multi-tenant or common", deflt = "", required = false)
	String tenant();
}
