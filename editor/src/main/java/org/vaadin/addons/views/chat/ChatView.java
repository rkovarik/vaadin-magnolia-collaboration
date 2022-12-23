package org.vaadin.addons.views.chat;

import org.vaadin.addons.views.login.MagnoliaUser;

import com.vaadin.collaborationengine.CollaborationMessageInput;
import com.vaadin.collaborationengine.CollaborationMessageList;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;

@PageTitle("Chat")
public class ChatView extends VerticalLayout {

    public ChatView() {
        addClassName("chat-view");
        setSpacing(false);

        MagnoliaUser user = MagnoliaUser.getInstance();
        UserInfo userInfo = user.getUserInfo();

        CollaborationMessageList list = new CollaborationMessageList(userInfo, "");
        list.setWidthFull();
        list.addClassNames("chat-view-message-list");

        CollaborationMessageInput input = new CollaborationMessageInput(list);
        input.addClassNames("chat-view-message-input");
        input.setWidthFull();

        // Layouting
        add(list, input);
        setSizeFull();
        expand(list);
    }

}