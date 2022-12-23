package org.vaadin.addons.components.iframe;

import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@SpringComponent
@UIScope
public class PageEditorIFrame extends IFrame {

    public PageEditorIFrame() {
        setSizeFull();
        this.setSandbox((SandboxType[]) null);
        getElement().setAttribute("frameBorder", "0");
        getElement().setAttribute("id", "mgnl-pageeditor");
    }
}
