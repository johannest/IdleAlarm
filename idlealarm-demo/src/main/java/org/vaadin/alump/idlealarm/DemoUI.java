package org.vaadin.alump.idlealarm;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.*;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.vaadin.alump.idlealarm.client.shared.IdleAlarmFormatting;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

/**
 * Demo UI of IdleAlarm addon
 */
@Theme("demo")
@Title("IdleAlarm Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI {

    private static final String STYLED_FORMATTING = "Lennät muuten ulos <b>"
            + IdleAlarmFormatting.SECS_TO_TIMEOUT + "</b> sekunnin kuluttua, ellet tee mitään.";

    private static final int IDLE_TIMEOUT_SECONDS = 60;

    private static final String GUIDE = "IdleAlarm add-on is designed to be used with Vaadin's idle timeout feature. "
            + "Add-on adds option to show alarm to user when sessions is about to expire because of long idle period.";

    private static final String NOTICE = "Please notice that there is always some extra delay, from seconds to few "
        + "minutes before session really gets expired. As the idle time out in this application is set to very short "
        + "(60 seconds), you can easily see this extra time here.";

    // This add-on old works when closeIdleSessions init parameter is true
    @WebServlet(value = "/*", asyncSupported = true, initParams = {
            @WebInitParam(name="closeIdleSessions", value="true")
    })
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class, widgetset = "org.vaadin.alump.idlealarm.DemoWidgetSet")
    public static class Servlet extends VaadinServlet implements SessionInitListener {

        protected void servletInitialized() throws ServletException {
            super.servletInitialized();
            getService().addSessionInitListener(this);
        }

        @Override
        public void sessionInit(SessionInitEvent event) throws ServiceException {
            event.getSession().getSession().setMaxInactiveInterval(IDLE_TIMEOUT_SECONDS);
        }
    }

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth(100, Unit.PERCENTAGE);
        layout.setMargin(true);
        layout.setSpacing(true);
        setContent(layout);

        layout.addComponent(new Label(GUIDE));
        layout.addComponent(new Label(NOTICE));

        // -- Inputs to modify IdleAlarm --

        HorizontalLayout row = createRow(layout);
        row.setWidth(100, Unit.PERCENTAGE);
        row.setCaption("Setup warning message (values can be modified only in disabled state)");

        final TextField secondsBefore = new TextField("Seconds");
        secondsBefore.setValue("40");
        secondsBefore.setWidth(60, Unit.PIXELS);
        row.addComponent(secondsBefore);

        final TextField warningMessage = new TextField("Message");
        warningMessage.setValue("Your session will timeout in less than " + IdleAlarmFormatting.SECS_TO_TIMEOUT
                + " seconds. Click anywhere to continue session.");
        warningMessage.setWidth(100, Unit.PERCENTAGE);
        row.addComponent(warningMessage);
        row.setExpandRatio(warningMessage, 1f);

        final Button enableButton = new Button("Enable");
        row.addComponent(enableButton);
        row.setComponentAlignment(enableButton, Alignment.BOTTOM_RIGHT);
        final Button disableButton = new Button("Disable");
        disableButton.setEnabled(false);
        row.addComponent(disableButton);
        row.setComponentAlignment(disableButton, Alignment.BOTTOM_LEFT);

        enableButton.addClickListener(event -> {
            enableButton.setEnabled(false);
            disableButton.setEnabled(true);
            secondsBefore.setEnabled(false);
            warningMessage.setEnabled(false);

            IdleAlarm.get().setSecondsBefore(Integer.valueOf(secondsBefore.getValue()))
                    .setMessage(warningMessage.getValue());
        });

        disableButton.addClickListener(event -> {
            enableButton.setEnabled(true);
            disableButton.setEnabled(false);
            secondsBefore.setEnabled(true);
            warningMessage.setEnabled(true);

            IdleAlarm.unload();
        });

        // -- Labels for debugging --

        row = createRow(layout);

        IdleCountdownLabel label = new IdleCountdownLabel();
        label.setCaption("IdleCountdownLabel (mainly for debugging):");
        row.addComponent(label);

        IdleCountdownLabel styledLabel = new IdleCountdownLabel(STYLED_FORMATTING);
        styledLabel.setContentMode(ContentMode.HTML);
        styledLabel.setCaption("IdleCountdownLabel (formatting & styling)");
        row.addComponent(styledLabel);

        Button resetTimeout = new Button("Reset timeout by calling server", event -> {
           Notification.show("Idle time reset");
        });
        layout.addComponent(resetTimeout);

    }

    private HorizontalLayout createRow(ComponentContainer parent) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        parent.addComponent(row);
        return row;
    }

}