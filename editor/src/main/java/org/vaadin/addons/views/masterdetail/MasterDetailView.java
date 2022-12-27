package org.vaadin.addons.views.masterdetail;

import static org.vaadin.addons.data.service.PageEditorService.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.annotation.security.PermitAll;

import org.vaadin.addons.components.appnav.NavigationGrid;
import org.vaadin.addons.components.iframe.PageEditorIFrame;
import org.vaadin.addons.data.entity.Properties;
import org.vaadin.addons.data.entity.Templates;
import org.vaadin.addons.data.service.PageEditorService;
import org.vaadin.addons.views.MainLayout;
import org.vaadin.addons.views.chat.ChatView;
import org.vaadin.addons.views.login.MagnoliaUser;

import com.fasterxml.jackson.databind.JsonNode;
import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationBinder;
import com.vaadin.collaborationengine.MessageManager;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@PageTitle("Page Editor")
@Route(value = "/", layout = MainLayout.class)
@PermitAll
@Uses(NavigationGrid.class)
@SpringComponent
@UIScope
public class MasterDetailView extends Div implements BeforeEnterObserver {

    private static final String VISIBILITY = "visibility";
    private static final String VISIBLE = "visible";
    private static final String HIDDEN = "hidden";
    private final MessageManager messageManager;

    private final CollaborationAvatarGroup avatarGroup;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");
    private final UserInfo userInfo;

    private CollaborationBinder<Properties> binder;
    private final NavigationGrid grid;
    private final FormLayout formLayout = new FormLayout();
    private final Templates templates = new Templates();

    private Properties properties;

    private final PageEditorService pageEditorService;
    private final PageEditorIFrame iFrame;
    private final Div editorLayoutDiv = new Div();
    private String componentPath;

    public MasterDetailView(PageEditorService pageEditorService, NavigationGrid navigationGrid, PageEditorIFrame iFrame) {
        this.grid = navigationGrid;
        this.pageEditorService = pageEditorService;
        this.iFrame = iFrame;
        addClassNames("master-detail-view");

        var user = MagnoliaUser.getInstance();
        userInfo = user.getUserInfo();
        messageManager = new MessageManager(this, userInfo, "");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        avatarGroup = new CollaborationAvatarGroup(userInfo, null);
        avatarGroup.getStyle().set(VISIBILITY, HIDDEN);

        createPageEditorLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        cancel.addClickListener(e -> {
            clearForm();
            refreshPreview("");
        });

        save.addClickShortcut(Key.ENTER);
        save.addClickListener(e -> {
            try {
                binder.writeBean(this.properties);
                pageEditorService.update(this.properties, componentPath);
                messageManager.submit("I've updated component " + componentPath);
                refreshPreview(getPagePath());
            } catch (ValidationException | IOException | InterruptedException validationException) {
                Notification.show("An exception happened while trying to store the properties details." + validationException.getMessage());
            }
        });
        cancel.addClickListener(clickEvent -> editorLayoutDiv.setVisible(false));

        grid.asSingleSelect().addValueChangeListener(changeEvent -> {
            QueryParameters queryParameters = null;
            if (changeEvent.getValue() != null) {
                queryParameters = new QueryParameters(Map.of(PageEditorService.COMPONENT_PATH, Collections.singletonList(getPagePath())));
            } else {
                clearForm();
            }
            UI.getCurrent().navigate(MasterDetailView.class, queryParameters);
        });
    }

    private String getPagePath() {
        return this.grid.asSingleSelect().getValue().path(PageEditorService.PATH).asText();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.componentPath = getParameter(event, COMPONENT_PATH).orElse(getPagePath());
        var dialog = getParameter(event, DIALOG).orElse(getPagePath());
        edit(componentPath, dialog);
    }

    @ClientCallable
    public void populateForm(String componentPath, String dialog) {
        QueryParameters queryParameters = new QueryParameters(Map.of(
                COMPONENT_PATH, Collections.singletonList(componentPath),
                DIALOG, Collections.singletonList(dialog)
        ));
        UI.getCurrent().navigate(MasterDetailView.class, queryParameters);
    }

    private void edit(String componentPath, String dialog) {
        this.componentPath = componentPath;
        populateForm(pageEditorService.getComponent(componentPath), dialog);
    }

    private static Optional<String> getParameter(BeforeEnterEvent event, String paramName) {
        return event.getLocation().getQueryParameters().getParameters().getOrDefault(paramName, Collections.emptyList()).stream()
                .findFirst();
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        editorLayoutDiv.setClassName("editor-layout");
        editorLayoutDiv.setVisible(false);

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        editorDiv.add(avatarGroup, formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createPageEditorLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setSizeFull();
        iFrame.setSizeFull();
        var chatView = new ChatView();
        chatView.setHeight(20, Unit.PERCENTAGE);
        var splitEditorLayout = new SplitLayout(iFrame, chatView, SplitLayout.Orientation.VERTICAL);
        add(splitEditorLayout);
        splitLayout.addToPrimary(splitEditorLayout);
        wrapper.add(splitEditorLayout);
    }

    private void refreshPreview(String path) {
        grid.select(path);
    }

    private void clearForm() {
        populateForm((JsonNode) null, null);
    }

    private void populateForm(JsonNode jsonNode, String dialog) {
        var template = templates.getOrDefault(dialog, new Templates.Unknown());
        if (jsonNode == null || template == null) {
            editorLayoutDiv.setVisible(false);
            return;
        } else {
            editorLayoutDiv.setVisible(true);
        }
        formLayout.removeAll();
        formLayout.add(template.getFields());
        binder = new CollaborationBinder<>(Properties.class, userInfo);
        binder.bindInstanceFields(template);

        this.properties = pageEditorService.convert(jsonNode);

        String topic = jsonNode.path(PageEditorService.PATH).asText();
        binder.setTopic(topic, () -> this.properties);

        if (this.properties != null && topic != null) {
            avatarGroup.getStyle().set(VISIBILITY, VISIBLE);
        } else {
            avatarGroup.getStyle().set(VISIBILITY, HIDDEN);
        }
        avatarGroup.setTopic(topic);
    }
}
