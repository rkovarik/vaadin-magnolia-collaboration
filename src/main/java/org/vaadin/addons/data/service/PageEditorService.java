package org.vaadin.addons.data.service;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vaadin.addons.data.entity.Properties;
import org.vaadin.addons.views.login.SecurityConfiguration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.VaadinRequest;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PageEditorService {

    public static final String PROPERTIES = "properties";
    public static final String PROPERTY_NAME = "name";
    public static final String COMPONENT_PATH = "componentPath";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String REST_NODES_V_1_WEBSITE = "/.rest/nodes/v1/website";
    public static final String VALUES = "values";
    private static final String HREF = "href";
    public static final String EDIT = "/edit";
    public static final String UI_ID = "uiId";
    public static final String DIALOG = "dialog";

    @Value("${magnolia.author.url}")
    private String magnoliaAuthorUrl;

    @Value("${magnolia.public.url}")
    private String magnoliaPublicUrl;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final SecurityConfiguration securityConfiguration;

    public PageEditorService(SecurityConfiguration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
        OBJECT_MAPPER.enable(JsonParser.Feature.IGNORE_UNDEFINED);
    }

    public Stream<JsonNode> getChildPages(String path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(magnoliaAuthorUrl + REST_NODES_V_1_WEBSITE + path + "?depth=10&excludeNodeTypes=mgnl:area,rep:system,rep:AccessControl,mgnl:component"))
                .header(AUTHORIZATION, securityConfiguration.getMagnoliaSuperuserAuthorization())
                .build();
        try {
            String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            JsonNode jsonNode = OBJECT_MAPPER.reader().readValue(body, ObjectNode.class).path("nodes");
            return StreamSupport.stream(jsonNode.spliterator(), false);
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
            return Stream.empty();
        }
    }

    @SneakyThrows
    public Properties convert(JsonNode node) {
        ArrayNode jsonNode = (ArrayNode) OBJECT_MAPPER.reader().readValue(node.toString(), JsonNode.class).path(PROPERTIES);
        ObjectNode transformed = JsonNodeFactory.instance.objectNode();
        jsonNode.forEach(child -> transformed.put(child.path("name").asText(), child.path(VALUES).get(0).asText()));
        var properties = OBJECT_MAPPER.reader().readValue(transformed.toString(), Properties.class);
        properties.setJson(transformed.toPrettyString());
        return properties;
    }

    @SneakyThrows
    public JsonNode getComponent(String path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(magnoliaAuthorUrl + REST_NODES_V_1_WEBSITE + path))
                .header(AUTHORIZATION, securityConfiguration.getMagnoliaSuperuserAuthorization())
                .build();
        String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        return OBJECT_MAPPER.reader().readValue(body, JsonNode.class);
    }

    public Properties update(Properties entity, String path) throws IOException, InterruptedException {
        var jsonNode = OBJECT_MAPPER.valueToTree(entity);
        ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
        for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext(); ) {
            String fieldName = it.next();
            ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
            objectNode.put("name", fieldName);
            objectNode.set(VALUES, JsonNodeFactory.instance.arrayNode().add(jsonNode.path(fieldName).asText()));
            objectNode.put("type", "String");
            arrayNode.add(objectNode);
        }
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.set(PROPERTIES, arrayNode);
        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(magnoliaAuthorUrl + REST_NODES_V_1_WEBSITE + path))
                        .timeout(Duration.ofMinutes(2))
                        .header("Content-Type", "application/json")
                        .header(AUTHORIZATION, securityConfiguration.getMagnoliaSuperuserAuthorization())
                        .POST(HttpRequest.BodyPublishers.ofString(objectNode.toString()))
                        .build();
        Notification.show(String.valueOf(httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode()));
        return entity;
    }

    public String fetchRenderedPage(String path) {
        var uri = magnoliaAuthorUrl + path + "?mgnlPreview=false";
        try {
            var doc = Jsoup.connect(uri)
                    .header(AUTHORIZATION, securityConfiguration.getMagnoliaSuperuserAuthorization())
                    .get();

            toAbsoluteLinks(doc, "link", HREF);
            toAbsoluteLinks(doc, "img", "src");
            toAbsoluteLinks(doc, "script", "src");
            toAbsoluteLinks(doc, "a", HREF);
            //            magnoliaLinksToVaadinLinks(doc);
            relativeSetsToAbsoluteSets(doc);

            doc.head().append("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + magnoliaAuthorUrl + "/VAADIN/themes/pages-app/page-editor.css\">");
            doc.head().append("<script>\n" +
                    "function edit(url) {\n" +
                    "  var xhr = new XMLHttpRequest();\n" +
                    "  xhr.open(\"GET\", url, true);" +
                    "  xhr.send();" +
                    "}\n" +
                    "</script>");
            addPageEditorBars(doc.body());
            return doc.toString();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return "<h1>Can't fetch the Magnolia page " + path + "<h1>";
        }
    }

    private Node addPageEditorBars(Node doc) {
        doc.childNodes().stream()
                .map(this::addPageEditorBars)
                .filter(n -> "#comment".equals(n.nodeName()))
                .forEach(node -> {
                            String comment = node.attr(node.nodeName());
                            if (comment.contains(" cms:component")) {
                                var nodePath = StringUtils.substringBetween(comment, "content=\"website:", "\"");
                                var title = StringUtils.substringBetween(comment, "label=\"", "\"");
                                var dialog = StringUtils.substringBetween(comment, "dialog=\"", "\"");
                                var mgnlLevel = StringUtils.countMatches(nodePath, "/");
                                //TODO
                                var focus = nodePath.equals(VaadinRequest.getCurrent().getParameter(COMPONENT_PATH)) ? "focus" : StringUtils.EMPTY;
                                var editUrl = EDIT + "?" + UI_ID + "=" + UI.getCurrent().getUIId()
                                        + "&" + COMPONENT_PATH + "=" + nodePath
                                        + "&" + DIALOG + "=" + dialog;
                                node.after("<div class=\"mgnlEditorBar mgnlEditor component " + focus + "\" " +
                                        "<div " +
                                        "class=\"mgnlEditorBarLabelSection\"><div></div><div " +
                                        "class=\"mgnlEditorBarLabel " + "mgnlLevel-" + mgnlLevel + "\" " +
                                        "title=\"Jumbotron - Header for a page\">" + title +
                                        "</div></div><div class=\"mgnlEditorBarButtons\">" +
                                        "<div class=\"editorIcon icon-edit\" onclick=\"edit('" + editUrl + "');\">" +
                                        "</div>");
                            }
                        }
                );
        return doc;
    }

    private void relativeSetsToAbsoluteSets(Document doc) {
        doc.select("div").forEach(e -> {
            var attr = e.attr("style");
            if (!attr.isEmpty()) {
                attr = attr.replace("background-image: url(", "background-image: url(" + magnoliaPublicUrl);
                e.attr("style", attr);
            }
        });

        doc.select("img").forEach(e -> {
            var attr = e.attr("data-srcset");
            if (!attr.isEmpty()) {
                attr = attr.replace("/.imaging", magnoliaPublicUrl + "/.imaging");
                // path
                e.attr("data-srcset", attr);
            }
        });
    }

    private void toAbsoluteLinks(Document doc, String tag, String attribute) {
        doc.select(tag).forEach(e -> {
            var attributeValue = e.attr(attribute);
            if (!attributeValue.startsWith("http")) {
                attributeValue = "/" + StringUtils.stripStart(attributeValue, "/travel");
                e.attr(attribute, "https://demopublic.magnolia-cms.com" + attributeValue);
            }
        });
    }

}
