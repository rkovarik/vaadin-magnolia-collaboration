package org.vaadin.addons.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Properties {

    private Boolean hideInNav;
    private Boolean mandatory;

    private String floating;
    private String vertical;
    private String title;
    private String subtitle;
    private String formTitle;
    private String text;
    private String teaserAbstract;
    private String buttonText;
    private String backButtonText;
    private String labels;
    private String type;
    private String description;
    private String imagePosition;
    private String headline;
    private String headlineLevel;
    private String linkTypeexternal;
    private String editHTML;
    private String json;

    private Double size;

}
