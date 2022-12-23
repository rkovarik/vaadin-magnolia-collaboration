package org.vaadin.addons.views.login;

import java.util.Collections;
import java.util.Iterator;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.fasterxml.jackson.databind.JsonNode;
import com.vaadin.collaborationengine.UserInfo;

public class MagnoliaUser extends User {

    private final JsonNode properties;
    private final String magnoliaPublicUrl;

    public MagnoliaUser(String username, JsonNode properties, String magnoliaPublicUrl) {
        super(username, "{noop}" + username, Collections.emptyList());
        this.properties = properties;
        this.magnoliaPublicUrl = magnoliaPublicUrl;
    }

    public static MagnoliaUser getInstance() {
        return (MagnoliaUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public UserInfo getUserInfo() {
        return new UserInfo(getUsername(),
                getString("firstName") + " " + getString("lastName"),
                magnoliaPublicUrl + "/contacts/" + getUsername() + "/photo"
        );
    }

    private String getString(String fieldName) {
        for (Iterator<JsonNode> it = properties.elements(); it.hasNext(); ) {
            JsonNode element = it.next();
            if (element.path("name").asText().equals(fieldName)) {
                return element.path("values").path(0).asText();
            }
        }
        return null;
    }
}
