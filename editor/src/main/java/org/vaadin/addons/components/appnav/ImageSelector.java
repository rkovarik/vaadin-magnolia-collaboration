package org.vaadin.addons.components.appnav;

import static org.vaadin.addons.data.service.PageEditorService.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.vaadin.addons.data.service.PageEditorService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;

public class ImageSelector extends ComboBox<JsonNode> {

    private final PageEditorService pageEditorService;

    public ImageSelector(String magnoliaPublicUrl, PageEditorService pageEditorService) {
        super("Image");
        this.pageEditorService = pageEditorService;
        setPlaceholder("Select an image");
        setClearButtonVisible(true);
        setRenderer(new ComponentRenderer<>(item -> {
            var title = getItemLabelGenerator().apply(item);
            var image = new Image(magnoliaPublicUrl + "/.imaging/portrait/dam/" + getDataProvider().getId(item), title);
            image.setHeight(20, Unit.PIXELS);
            image.setTitle(title);
            var layout = new HorizontalLayout(new Span(title));
            if (!title.endsWith(".mp4")) {
                layout.addComponentAsFirst(image);
            }
            return layout;
        }));
        setItemLabelGenerator(item -> item.path(PROPERTIES).path(3).path(VALUES).path(0).asText());
        setItems(new DataProvider());
    }

    public class DataProvider extends AbstractBackEndDataProvider<JsonNode, String> {

        private final Map<String, JsonNode> assets = new LinkedHashMap<>();

        public JsonNode getItem(String id) {
            return assets.get(id);
        }

        @Override
        public String getId(JsonNode item) {
            return item == null ? null : item.path(IDENTIFIER).asText();
        }

        @Override
        protected Stream<JsonNode> fetchFromBackEnd(Query<JsonNode, String> query) {
            return getAssets().stream()
                    .filter(jsonNode -> query.getFilter().map(filter ->  getItemLabelGenerator().apply(jsonNode).contains(filter)).orElse(true))
                    .skip(query.getOffset())
                    .limit(query.getLimit());
        }

        private Collection<JsonNode> getAssets() {
            if (assets.isEmpty()) {
                pageEditorService.getChildPages("/", "dam").forEach(child -> collectChildren(child, assets));
            }
            return assets.values();
        }

        private void collectChildren(JsonNode jsonNode, Map<String, JsonNode> assets) {
            var children = jsonNode.path(NODES);
            if (children instanceof ArrayNode) {
                children.elements().forEachRemaining(child -> collectChildren(child, assets));
            } else if ("mgnl:resource".equals(jsonNode.path(TYPE).asText())) {
                assets.put(getId(jsonNode), jsonNode);
            }
        }

        @Override
        protected int sizeInBackEnd(Query<JsonNode, String> query) {
            return (int) fetchFromBackEnd(query).count();
        }
    }

}
