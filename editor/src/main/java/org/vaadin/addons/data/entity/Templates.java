package org.vaadin.addons.data.entity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import lombok.Data;

public final class Templates extends HashMap<String, Templates.Template> {

    public Templates() {
        put("textImage", new TextImage());
        put("tourTypeTeaserRow", new TeaserRow());
        put("tourList", new TourList());
        put("linkList", new LinkList());
        put("link", new Link());
        put("formEdit", new Input());
        put("form", new Form());
    }

    @Data
    public abstract static class Template {
        final TextField headLine = new TextField("Headline");
        final TextArea text = new TextArea("Text");
        final ComboBox<String> headlineLevel = new ComboBox<>("Headline level", "small", "medium", "big");
        final ComboBox<String> imagePosition = new ComboBox<>("Image position", "below", "above");
        final TextField title = new TextField("Title");
        final TextField subtitle = new TextField("Subtitle");
        final TextField linkTypeexternal = new TextField("URL", "https://");

        public abstract Collection<Component> getFields();
    }

    public static class TextImage extends Templates.Template {
        @Override
        public Collection<Component> getFields() {
            return Arrays.asList(headLine, headlineLevel, text, imagePosition);
        }
    }

    public static class LinkList extends Template {
        @Override
        public Collection<Component> getFields() {
            return Arrays.asList(subtitle, headlineLevel);
        }
    }

    public static class TeaserRow extends Template {
        @Override
        public Collection<Component> getFields() {
            return List.of(title);
        }
    }

    public static class TourList extends Template {
        @Override
        public Collection<Component> getFields() {
            return List.of(title);
        }
    }

    public static class Form extends Template {
        private final TextField formTitle = new TextField();
        @Override
        public Collection<Component> getFields() {
            return List.of(formTitle);
        }
    }

    public static class Link extends TeaserRow {
        @Override
        public Collection<Component> getFields() {
            return Arrays.asList(title, headlineLevel, linkTypeexternal);
        }
    }

    /**
     *   "disabled" : "false",
     *   "validation" : "email",
     *   "controlName" : "email",
     *   "inputType" : "email",
     *   "autofocus" : "false",
     *   "title" : "Your Email",
     *   "mandatory" : "true",
     *   "rows" : "1",
     *   "title_de" : "Ihre E-Mail",
     *   "readonly" : "false",
     *   "autocomplete" : "false"
     */
    public static class Input extends TeaserRow {
        private final Checkbox mandatory = new Checkbox("Mandatory");
        @Override
        public Collection<Component> getFields() {
            return Arrays.asList(title, mandatory);
        }
    }

    public static class Unknown extends Template {
        private final TextField description = new TextField("Description");

        private final Checkbox hideInNav = new Checkbox("Hide in navigation");

        private final TextArea json = new TextArea("JSON");

        @Override
        public Collection<Component> getFields() {
            json.setReadOnly(true);
            return Collections.singleton(json);
        }
    }

}
