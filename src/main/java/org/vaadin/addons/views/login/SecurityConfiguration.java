package org.vaadin.addons.views.login;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.spring.security.VaadinWebSecurity;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    private static final String REST_NODES_V_1_CONTACTS = "/.rest/nodes/v1/contacts/";

    @Value("${magnolia.public.url}")
    private String magnoliaPublicUrl;
    @Value("${magnolia.admin.name}")
    private String superuserName;
    @Value("${magnolia.admin.password}")
    private String superuserPassword;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        setLoginView(http, LoginView.class);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(magnoliaPublicUrl + REST_NODES_V_1_CONTACTS + username))
                    .header(AUTHORIZATION, getMagnoliaSuperuserAuthorization())
                    .build();

            try {
                String body = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build()
                        .send(request, HttpResponse.BodyHandlers.ofString())
                        .body();

                JsonNode jsonNode = new ObjectMapper().reader().readValue(body, JsonNode.class);
                return new MagnoliaUser(jsonNode.path("name").asText(), jsonNode.path("properties"));
            } catch (IOException e) {
                return null;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public String getMagnoliaSuperuserAuthorization() {
        String valueToEncode = superuserName + ":" + superuserPassword;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    public class MagnoliaUser extends User {

        private final JsonNode properties;

        public MagnoliaUser(String username, JsonNode properties) {
            super(username, "{noop}" + username, Collections.emptyList());
            this.properties = properties;
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
}