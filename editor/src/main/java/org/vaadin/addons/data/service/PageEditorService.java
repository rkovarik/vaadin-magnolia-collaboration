package org.vaadin.addons.data.service;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vaadin.addons.data.entity.Properties;
import org.vaadin.addons.data.entity.Templates;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.HttpStatusCode;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PageEditorService {

    public static final String PROPERTIES = "properties";
    public static final String PROPERTY_NAME = "name";
    public static final String COMPONENT_PATH = "componentPath";
    public static final String PATH = "path";
    public static final String IDENTIFIER = "identifier";
    public static final String NODES = "nodes";
    public static final String TYPE = "type";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String REST_NODES_V_1 = "/.rest/nodes/v1/";
    public static final String VALUES = "values";
    private static final String HREF = "href";
    public static final String DIALOG = "dialog";
    public static final String TITLE = "title";
    public static final String WEBSITE = "website";

    @Value("${magnolia.author.url}")
    private String magnoliaAuthorUrl;
    @Value("${magnolia.public.url}")
    private String magnoliaPublicUrl;
    @Value("${magnolia.admin.name}")
    private String superuserName;
    @Value("${magnolia.admin.password}")
    private String superuserPassword;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public PageEditorService() {
        OBJECT_MAPPER.enable(JsonParser.Feature.IGNORE_UNDEFINED);
    }

    public String getMagnoliaSuperuserAuthorization() {
        String valueToEncode = superuserName + ":" + superuserPassword;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    public Stream<JsonNode> getChildPages(String path, String workspace) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(magnoliaAuthorUrl + REST_NODES_V_1 + workspace + path + "?depth=10&excludeNodeTypes=mgnl:area,rep:system,rep:AccessControl,mgnl:component"))
                .header(AUTHORIZATION, getMagnoliaSuperuserAuthorization())
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
        jsonNode.forEach(child -> transformed.put(child.path(PROPERTY_NAME).asText(), child.path(VALUES).get(0).asText()));
        var properties = OBJECT_MAPPER.reader().readValue(transformed.toString(), Properties.class);
        properties.setJson(transformed.toPrettyString());
        return properties;
    }

    @SneakyThrows
    public JsonNode getComponent(String path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(magnoliaAuthorUrl + REST_NODES_V_1 + WEBSITE + path))
                .header(AUTHORIZATION, getMagnoliaSuperuserAuthorization())
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != HttpStatusCode.OK.getCode()) {
            return null;
        }
        String body = response.body();
        return OBJECT_MAPPER.reader().readValue(body, JsonNode.class);
    }

    public void update(Properties entity, String path) throws IOException, InterruptedException {
        var jsonNode = OBJECT_MAPPER.valueToTree(entity);
        ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
        for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext(); ) {
            String fieldName = it.next();
            ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
            objectNode.put(PROPERTY_NAME, fieldName);
            var node = jsonNode.path(fieldName);
            if (node instanceof NullNode) {
                continue;
            }
            objectNode.set(VALUES, JsonNodeFactory.instance.arrayNode().add(node.asText()));
            var type = node.getNodeType().name();
            type = "NUMBER".equals(type) ? "Double" : StringUtils.capitalize(type.toLowerCase());
            objectNode.put(TYPE, type);
            arrayNode.add(objectNode);
        }
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.set(PROPERTIES, arrayNode);
        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(magnoliaAuthorUrl + REST_NODES_V_1 + WEBSITE + path))
                        .timeout(Duration.ofMinutes(2))
                        .header("Content-Type", "application/json")
                        .header(AUTHORIZATION, getMagnoliaSuperuserAuthorization())
                        .POST(HttpRequest.BodyPublishers.ofString(objectNode.toString()))
                        .build();
        log.info(String.valueOf(httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode()));
    }

    public void delete(String componentPath) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(magnoliaAuthorUrl + REST_NODES_V_1 + WEBSITE + componentPath))
                        .header(AUTHORIZATION, getMagnoliaSuperuserAuthorization())
                        .DELETE()
                        .build();
        log.info(String.valueOf(httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode()));
    }

    public String fetchRenderedPage(String path) {
        var uri = magnoliaAuthorUrl + path + "?mgnlPreview=false";
        try {
            var doc = Jsoup.connect(uri)
                    .header(AUTHORIZATION, getMagnoliaSuperuserAuthorization())
                    .get();

            toAbsoluteLinks(doc, "link", HREF);
            toAbsoluteLinks(doc, "img", "src");
            toAbsoluteLinks(doc, "script", "src");
            toAbsoluteLinks(doc, "a", HREF);
            relativeSetsToAbsoluteSets(doc);

            doc.head().append("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + magnoliaAuthorUrl + "/VAADIN/themes/pages-app/page-editor.css\">");
            doc.body().attr("onload", "document.getElementById('focused')?.scrollIntoView();");
            addPageEditorBars(doc.body());
            return doc.toString();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return "<h1>Can't fetch the Magnolia page " + path + "<h1>";
        }
    }

    private Node addPageEditorBars(Node body) {
        body.childNodes().stream()
                .map(this::addPageEditorBars)
                .filter(n -> "#comment".equals(n.nodeName()))
                .forEach(node -> {
                            String comment = node.attr(node.nodeName());
                            if (comment.contains(" cms:component")) {
                                var dialog = StringUtils.substringBetween(comment, "dialog=\"", "\"");
                                dialog = StringUtils.contains(dialog, "/") ? StringUtils.substringAfterLast(dialog, "/") : StringUtils.substringAfter(dialog, ":");
                                var inherited = StringUtils.substringBetween(comment, "inherited=\"", "\"");
                                var editable = new Templates(magnoliaPublicUrl, this).get(dialog) != null && (inherited == null || Boolean.FALSE.toString().equals(inherited));
                                if (editable) {
                                    var nodePath = StringUtils.substringBetween(comment, "content=\"website:", "\"");
                                    var title = StringUtils.substringBetween(comment, "label=\"", "\"");
                                    var mgnlLevel = StringUtils.countMatches(nodePath, "/");
                                    var parameter = UI.getCurrent().getInternals().getActiveViewLocation()
                                            .getQueryParameters()
                                            .getParameters()
                                            .getOrDefault(COMPONENT_PATH, List.of())
                                            .stream()
                                            .findFirst()
                                            .orElse(null);
                                    var focus = nodePath.equals(parameter) ? "focus" : StringUtils.EMPTY;
                                    var getElementScript = "parent.document.getElementsByClassName('master-detail-view')[0].$server.";
                                    var focusScript = "this.parentElement.parentElement.classList.add('focus');";
                                    var editScript = getElementScript + "edit(" +
                                            "'" + nodePath + "'" + ", " +
                                            "'" + dialog + "'" +
                                            ", '" + title + "'" +
                                            "); " + focusScript;
                                    var deleteScript = getElementScript + "delete('" + nodePath + "'); " + focusScript;
                                    var editIcon = "<div class=\"editorIcon icon-edit\" onclick=\"" + editScript + "\"></div>";
                                    var deleteIcon = "<div class=\"editorIcon icon-delete\" onclick=\"" + deleteScript + "\"></div>";
                                    node.after("<div class=\"mgnlEditorBar mgnlEditor component " + focus + "\" " +
                                            (focus.isEmpty() ? StringUtils.EMPTY : " id='focused'") +
                                            "<div " + //delete-search, creative, schedule
                                            "class=\"mgnlEditorBarLabelSection\"><div></div><div " +
                                            "class=\"mgnlEditorBarLabel " + "mgnlLevel-" + mgnlLevel + "\" " +
                                            "title=\"" + title + "\">" + title +
                                            "</div></div><div class=\"mgnlEditorBarButtons\">" +
                                            editIcon + deleteIcon
                                    );
                                }
                            }
                        }
                );
        return body;
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
                e.attr(attribute, magnoliaPublicUrl + attributeValue);
            }
        });
    }
}
