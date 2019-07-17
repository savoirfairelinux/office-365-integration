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
