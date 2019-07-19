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

import com.microsoft.graph.models.extensions.MailFolder;
import com.savoirfairelinux.liferay.module.o365.api.EmailService;
import com.savoirfairelinux.liferay.module.o365.core.api.AuthenticatedService;
import com.savoirfairelinux.liferay.module.o365.core.model.O365Authentication;
import com.savoirfairelinux.liferay.module.o365.core.service.BaseAuthenticatedServiceImpl;
import org.osgi.service.component.annotations.Component;

@Component(
        service = {EmailService.class, AuthenticatedService.class},
        immediate = true
)
public class EmailServiceImpl extends BaseAuthenticatedServiceImpl implements EmailService
{
	
	@Override
	public String getRequiredScope() {
		return "User.Read Mail.Read";
	}
	
	@Override
	public int getNumberOfInboxUnreadMail(O365Authentication authentication) {
		
		MailFolder mails = getGraphClient(authentication).me()
				                               .mailFolders("inbox")
				                               .buildRequest()
				                               .get();
		
		return mails.unreadItemCount;
	}
}
