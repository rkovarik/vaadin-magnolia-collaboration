package org.vaadin.addons.components.appnav;

import static org.vaadin.addons.data.service.PageEditorService.*;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.addons.components.iframe.PageEditorIFrame;
import org.vaadin.addons.data.service.PageEditorService;

import com.fasterxml.jackson.databind.JsonNode;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@SpringComponent
@UIScope
public class NavigationGrid extends TreeGrid<JsonNode> {

    private static final String MGNL_PAGE = "mgnl:page";

    private final PageEditorIFrame iFrame;
    private final PageEditorService pageEditorService;

    @Autowired
    public NavigationGrid(PageEditorService pageEditorService, PageEditorIFrame iFrame) {
        this.iFrame = iFrame;
        this.pageEditorService = pageEditorService;

        addItemClickListener(event -> select(event.getItem()));
        setSizeFull();
        addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);

        addComponentHierarchyColumn(jsonNode -> {
            var name = jsonNode.path(PROPERTY_NAME).asText();
            for (Iterator<JsonNode> it = jsonNode.path(PROPERTIES).elements(); it.hasNext(); ) {
                JsonNode property = it.next();
                if (property.path(PROPERTY_NAME).asText().equals(TITLE)) {
                    name = property.path(VALUES).elements().next().asText();
                }
            }
            var div = new Span();
            div.setText(name);
            div.addClickListener(event -> select(jsonNode));
            div.setTitle(name);
            return div;
        });
        setDataProvider(new NavigationGridDataProvider(unused -> pageEditorService.getChildPages("/", "website")));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        select("/travel");
    }

    private boolean isPage(JsonNode objectNode) {
        return objectNode != null && MGNL_PAGE.equals(objectNode.path(TYPE).asText());
    }

    public void select(String pagePath) {
        var dataProvider = getDataProvider();
        JsonNode parentPage = null;
        JsonNode objectNode = null;
        for (String nodeName : StringUtils.split(pagePath, "/")) {
            objectNode = dataProvider.fetch(new HierarchicalQuery<>(null, objectNode))
                    .filter(jsonNode -> jsonNode.path(PROPERTY_NAME).asText().equals(nodeName))
                    .findFirst()
                    .orElse(parentPage);

            expand(objectNode);
            if (isPage(objectNode)) {
                parentPage = objectNode;
            }
        }
        select(objectNode);
        expand(objectNode);
        if (parentPage != null) {
            var path = parentPage.path(PATH).asText();
            var renderedPage = pageEditorService.fetchRenderedPage(path);
            iFrame.setSrcdoc(renderedPage);
        }
    }
}
