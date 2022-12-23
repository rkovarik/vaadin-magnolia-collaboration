package org.vaadin.addons.views;


import static org.vaadin.addons.data.service.PageEditorService.COMPONENT_PATH;

import java.util.ArrayList;

import javax.annotation.security.PermitAll;

import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.vaadin.addons.components.appnav.NavigationGrid;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * The main view is a top-level placeholder for other views.
 */
@PermitAll
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private H2 viewTitle;

    private final NavigationGrid grid;

    public MainLayout(NavigationGrid navigationGrid) {
        this.grid = navigationGrid;
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String path = getQueryParam(event, COMPONENT_PATH);
        if (path != null) {
            grid.select(path);
        }
    }

    private static String getQueryParam(BeforeEnterEvent event, String key) {
        return event.getLocation().getQueryParameters().getParameters().getOrDefault(key, new ArrayList<>()).stream()
                .findFirst()
                .orElse(null);
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        Button logout = new Button("Log out", e -> logout());

        HorizontalLayout header = new HorizontalLayout(toggle, viewTitle, logout);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(viewTitle);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(true, header);
    }

    private void logout() {
        UI.getCurrent().getPage().setLocation("/");
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
    }

    private void addDrawerContent() {
        H1 appName = new H1("Website structure");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);
        addToDrawer(header, grid, createFooter());
    }

    private Footer createFooter() {
        return new Footer();
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
