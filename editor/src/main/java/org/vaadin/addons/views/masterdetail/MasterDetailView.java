package org.vaadin.addons.views.masterdetail;

import static org.vaadin.addons.data.service.PageEditorService.COMPONENT_PATH;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.annotation.security.PermitAll;

import org.vaadin.addons.components.appnav.NavigationGrid;
import org.vaadin.addons.components.iframe.PageEditorIFrame;
import org.vaadin.addons.data.entity.Properties;
import org.vaadin.addons.data.service.PageEditorService;
import org.vaadin.addons.views.MainLayout;
import org.vaadin.addons.views.chat.ChatView;
import org.vaadin.addons.views.login.MagnoliaUser;

import com.fasterxml.jackson.databind.JsonNode;
import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationBinder;
import com.vaadin.collaborationengine.MessageManager;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
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

    private CollaborationAvatarGroup avatarGroup;

    //TODO move these to separate class:
    private final TextField title = new TextField("Title");
    private final TextField description = new TextField("Description"); //TODO remove

    private final TextArea json = new TextArea("JSON");

    private final TextField headline = new TextField("Headline");

    private final TextArea text = new TextArea("Text");
    private final Checkbox hideInNav = new Checkbox("Hide in navigation");

    private final ComboBox<String> imagePosition = new ComboBox<>("Image position", "below", "above");

    private final Collection<Component> allFields = Arrays.asList(title, headline, description, text, hideInNav, imagePosition, json);

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final CollaborationBinder<Properties> binder;
    private final NavigationGrid grid;

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
        UserInfo userInfo = user.getUserInfo();
        messageManager = new MessageManager(this, userInfo, "");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        avatarGroup = new CollaborationAvatarGroup(userInfo, null);
        avatarGroup.getStyle().set(VISIBILITY, HIDDEN);

        createPageEditorLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        binder = new CollaborationBinder<>(Properties.class, userInfo);
        binder.bindInstanceFields(this);

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
                populateForm(changeEvent.getValue());
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
        this.componentPath = getFirst(event).orElse(getPagePath());
        populateForm(pageEditorService.getComponent(componentPath));
    }

    private static Optional<String> getFirst(BeforeEnterEvent event) {
        return event.getLocation().getQueryParameters().getParameters().getOrDefault(COMPONENT_PATH, Collections.emptyList()).stream()
                .findFirst();
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        formLayout.add(allFields);

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
        populateForm(null);
    }

    private void populateForm(JsonNode jsonNode) {
        if (jsonNode == null) {
            editorLayoutDiv.setVisible(false);
            return;
        } else {
            editorLayoutDiv.setVisible(true);
        }
        //        allFields.forEach(component -> component.setVisible(false));
        //        jsonNode.path(PROPERTIES).forEach(property -> binder.getBinding(property.path(NAME).asText())
        //                .map(Binder.Binding::getField)
        //                .ifPresent(hasValue -> ((Component) hasValue).setVisible(true))
        //        );
        //        noEditableFields.setVisible(allFields.stream().noneMatch(Component::isVisible));
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
