package org.vaadin.addons.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Properties {

    private Boolean hideInNav;
    private String title;
    private String text;
    private String description;
    private String imagePosition;
    private String headline;
    private String json;

}
