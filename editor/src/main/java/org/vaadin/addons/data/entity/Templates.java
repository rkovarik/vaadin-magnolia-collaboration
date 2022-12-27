package org.vaadin.addons.data.entity;

import java.util.ArrayList;
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
        put("page", new Page());

        put("textImage", new TextImage());
        put("html", new Html());

        put("tourTypeTeaserRow", new Title());
        put("tourList", new Title());

        put("linkList", new LinkList());
        put("link", new Link());

        put("form", new Form());
        put("formEdit", new FormInput());
        put("formSubmit", new Button());
        put("formSelection", new FormSelection());
        put("formGroupFields", new Title());

        put("searchResults", new SearchResults());
    }

    @Data
    public abstract static class Template {
        final TextField headLine = new TextField("Headline");
        final ComboBox<String> headlineLevel = new ComboBox<>("Headline level", "small", "medium", "big");
        final TextField title = new TextField("Title");

        public abstract Collection<Component> getFields();
    }

    public static class TextImage extends Templates.Template {
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

    public static class SearchResults extends Template {
        @Override
        public Collection<Component> getFields() {
            return List.of(headLine);
        }
    }

    public static class Title extends Template {
        @Override
        public Collection<Component> getFields() {
            return List.of(title);
        }
    }

    public static class Link extends Title {
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
    public static class FormInput extends Title {
        private final Checkbox mandatory = new Checkbox("Mandatory");
        @Override
        public Collection<Component> getFields() {
            return Arrays.asList(title, mandatory);
        }
    }

    /**
     *   "controlName" : "mealOptions",
     *   "type" : "radio",
     *   "title" : "Meal Options",
     *   "mandatory" : "false",
     *   "labels" : "Gluten Free:gluten-
     */
    public static class FormSelection extends FormInput {
        private final TextArea labels = new TextArea("Labels");
        final ComboBox<String> type = new ComboBox<>("Selection type", "radio", "checkbox");

        @Override
        public Collection<Component> getFields() {
            var fields = new ArrayList<>(super.getFields());
            fields.add(labels);
            fields.add(type);
            return fields;
        }
    }

    /**
     *   "hideInNav" : "true",
     *   "title" : "Search Results",
     */
    public static class Page extends Template {
        private final TextField title = new TextField("Page title");

        private final Checkbox hideInNav = new Checkbox("Hide in navigation");

        @Override
        public Collection<Component> getFields() {
            return List.of(title, hideInNav);
        }
    }


    public static class Unknown extends Template {
        private final TextField description = new TextField("Description");
        private final TextArea json = new TextArea("JSON");

        @Override
        public Collection<Component> getFields() {
            json.setReadOnly(true);
            return Collections.singleton(json);
        }
    }

}
