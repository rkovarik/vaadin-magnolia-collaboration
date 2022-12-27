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
        put("html", new Html());
        put("tourTypeTeaserRow", new TeaserRow());
        put("tourList", new TourList());
        put("linkList", new LinkList());
        put("link", new Link());
        put("form", new Form());
        put("formEdit", new Input());
        put("formSubmit", new Button());
    }

    @Data
    public abstract static class Template {
        final ComboBox<String> headlineLevel = new ComboBox<>("Headline level", "small", "medium", "big");
        final TextField title = new TextField("Title");

        public abstract Collection<Component> getFields();
    }

    public static class TextImage extends Templates.Template {
        private final TextField headLine = new TextField("Headline");
        private final TextArea text = new TextArea("Text");
        private final ComboBox<String> imagePosition = new ComboBox<>("Image position", "below", "above");

        @Override
        public Collection<Component> getFields() {
            return Arrays.asList(headLine, headlineLevel, text, imagePosition);
        }
    }

    public static class Html extends Templates.Template {
        private final TextArea editHTML = new TextArea("HTML");

        @Override
        public Collection<Component> getFields() {
            return List.of(editHTML);
        }
    }

    public static class LinkList extends Template {
        private final TextField subtitle = new TextField("Subtitle");

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

    public static class Link extends TeaserRow {
        final TextField linkTypeexternal = new TextField("URL", "https://");

        @Override
        public Collection<Component> getFields() {
            return Arrays.asList(title, headlineLevel, linkTypeexternal);
        }
    }

    public static class Form extends Template {
        private final TextField formTitle = new TextField("Form");
        @Override
        public Collection<Component> getFields() {
            return List.of(formTitle);
        }
    }

    /**
     *  "buttonText_de" : "Abschicken",
     *   "buttonText" : "Send"
     */
    public static class Button extends Template {
        private final TextField buttonText = new TextField("Button text");
        @Override
        public Collection<Component> getFields() {
            return List.of(buttonText);
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
