package org.vaadin.addons;

import static org.vaadin.addons.data.service.PageEditorService.*;

import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.vaadin.addons.data.service.PageEditorService;
import org.vaadin.addons.views.masterdetail.MasterDetailView;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

@Component
public class ApplicationServiceInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addRequestHandler((session, request, response) -> {
            if (request.getPathInfo().equals(PageEditorService.EDIT)) {
                session.lock();
                try {
                    var pagePath = request.getParameter(COMPONENT_PATH);
                    var dialog = request.getParameter(DIALOG);
                    var uiId = request.getParameter(PageEditorService.UI_ID);
                    var ui = session.getUIById(Integer.parseInt(uiId));
                    var queryParameters = new QueryParameters(Map.of(
                            COMPONENT_PATH, Collections.singletonList(pagePath),
                            DIALOG, Collections.singletonList(dialog))
                    );
                    ui.access(() -> ui.navigate(MasterDetailView.class, queryParameters));
                    return true;
                } finally {
                    session.unlock();
                }
            }
            return false;
        });
    }

}
