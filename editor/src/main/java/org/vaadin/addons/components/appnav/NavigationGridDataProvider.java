package org.vaadin.addons.components.appnav;

import static org.vaadin.addons.data.service.PageEditorService.NODES;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.server.VaadinRequest;

class NavigationGridDataProvider extends AbstractBackEndHierarchicalDataProvider<JsonNode, Void> {

    private final Function<Void, Stream<JsonNode>> pageEditorService;
    private final VaadinRequest vaadinRequest;

    NavigationGridDataProvider(Function<Void, Stream<JsonNode>> pageEditorService) {
        this.pageEditorService = pageEditorService;
        this.vaadinRequest = VaadinRequest.getCurrent();
    }

    @Override
    public String getId(JsonNode item) {
        return item.path("path").asText();
    }

    @Override
    protected Stream<JsonNode> fetchChildrenFromBackEnd(HierarchicalQuery<JsonNode, Void> query) {
        return query.getParentOptional()
                    .map(jsonNode -> StreamSupport.stream(jsonNode.path(NODES).spliterator(), false)
                            .sorted(Comparator.comparing(this::hasChildren).reversed())
                    )
                    .orElseGet(() -> {
                        var cacheKey = getClass().getName();
                        Collection<JsonNode> jsonNodes = (Collection<JsonNode>) vaadinRequest.getAttribute(cacheKey);
                        if (jsonNodes == null) {
                            jsonNodes = pageEditorService.apply(null).collect(Collectors.toList());
                            vaadinRequest.setAttribute(cacheKey, jsonNodes);
                        }
                        return jsonNodes.stream();
                    });
    }

    @Override
    public int getChildCount(HierarchicalQuery<JsonNode, Void> query) {
        return (int) fetchChildren(query).count();
    }

    @Override
    public boolean hasChildren(JsonNode item) {
        return item.path("nodes") instanceof ArrayNode;
    }
}
