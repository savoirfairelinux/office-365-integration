package com.savoirfairelinux.liferay.module.o365.core.action;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.savoirfairelinux.liferay.module.o365.core.filter.Office365LoginFilter;
import org.osgi.service.component.annotations.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Post login hook that redirect users to the {@link Office365LoginFilter} after they
 * successfully login to Liferay
 */
@Component(
        immediate = true,
        property = {"key=" + PropsKeys.LOGIN_EVENTS_POST},
        service = LifecycleAction.class )
public final class PostLoginAction extends Action
{
	@Override
	public void run(HttpServletRequest request, HttpServletResponse response) {
        try{
	        String redirect = ParamUtil.getString(request, "_com_liferay_login_web_portlet_LoginPortlet_redirect","/");
	        String backUrl = HttpUtil.encodePath(redirect);
	        response.sendRedirect("/o/o365/login?backURL="+backUrl);
		}catch (Exception e){
        	throw new RuntimeException("Error when redirecting user", e);
		}
	}
}