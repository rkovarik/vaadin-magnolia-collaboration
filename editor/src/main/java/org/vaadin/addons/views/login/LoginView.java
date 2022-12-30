package org.vaadin.addons.views.login;

import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@AnonymousAllowed
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    public LoginView() {
        addClassName("login-view");
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setAdditionalInformation("Example demo users/passwords: mmonroe/mmonroe, jbach/jbach, ldavinci/ldavinci...");
        setTitle("Magnolia");
        setDescription("Basic Magnolia Editor");
        setI18n(i18n);
        setAction("login");
        setForgotPasswordButtonVisible(false);
        setOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (beforeEnterEvent.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            setError(true);
        }
    }
}