package org.vaadin.addons.views.login;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.vaadin.addons.data.service.PageEditorService.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.vaadin.addons.data.service.PageEditorService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.spring.security.VaadinWebSecurity;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    private static final String REST_NODES_V_1_CONTACTS = "/.rest/nodes/v1/contacts/";

    @Value("${magnolia.public.url}")
    private String magnoliaPublicUrl;
    private final PageEditorService pageEditorService;

    public SecurityConfiguration(PageEditorService pageEditorService) {
        this.pageEditorService = pageEditorService;
    }

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
                    .header(AUTHORIZATION, pageEditorService.getMagnoliaSuperuserAuthorization())
                    .build();

            try {
                String body = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build()
                        .send(request, HttpResponse.BodyHandlers.ofString())
                        .body();

                JsonNode jsonNode = new ObjectMapper().reader().readValue(body, JsonNode.class);
                return new MagnoliaUser(jsonNode.path(PROPERTY_NAME).asText(), jsonNode.path(PROPERTIES), magnoliaPublicUrl);
            } catch (IOException e) {
                return null;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

}