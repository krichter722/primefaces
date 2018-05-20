/**
 * Copyright 2009-2018 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.component.message;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.ROLE;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TITLE;
import org.primefaces.component.api.InputHolder;
import org.primefaces.context.PrimeApplicationContext;

import org.primefaces.expression.SearchExpressionFacade;
import org.primefaces.renderkit.UINotificationRenderer;
import org.primefaces.util.WidgetBuilder;

public class MessageRenderer extends UINotificationRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Message uiMessage = (Message) component;

        UIComponent target = SearchExpressionFacade.resolveComponent(context, uiMessage, uiMessage.getFor());
        String targetClientId = target.getClientId(context);

        encodeMarkup(context, uiMessage, targetClientId);
        encodeScript(context, uiMessage, target);
    }

    protected void encodeMarkup(FacesContext context, Message uiMessage, String targetClientId) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String display = uiMessage.getDisplay();
        boolean iconOnly = display.equals("icon");
        boolean escape = uiMessage.isEscape();
        String style = uiMessage.getStyle();
        String containerClass = display.equals("tooltip") ? "ui-message ui-helper-hidden" : "ui-message";
        String styleClass = uiMessage.getStyleClass();
        styleClass = styleClass == null ? containerClass : styleClass + " " + containerClass;

        Iterator<FacesMessage> msgs = context.getMessages(targetClientId);

        writer.startElement(DIV, uiMessage);
        writer.writeAttribute("id", uiMessage.getClientId(context), null);
        writer.writeAttribute("aria-live", "polite", null);

        if (style != null) {
            writer.writeAttribute(STYLE, style, null);
        }

        if (PrimeApplicationContext.getCurrentInstance(context).getConfig().isClientSideValidationEnabled()) {
            writer.writeAttribute("data-display", display, null);
            writer.writeAttribute("data-target", targetClientId, null);
            writer.writeAttribute("data-redisplay", String.valueOf(uiMessage.isRedisplay()), null);
        }

        if (msgs.hasNext()) {
            FacesMessage msg = msgs.next();
            String severityName = getSeverityName(msg);

            if (!shouldRender(uiMessage, msg, severityName)) {
                writer.writeAttribute(CLASS, styleClass, null);
                writer.endElement(DIV);

                return;
            }
            else {
                Severity severity = msg.getSeverity();
                String severityKey = null;

                if (severity.equals(FacesMessage.SEVERITY_ERROR)) {
                    severityKey = "error";
                }
                else if (severity.equals(FacesMessage.SEVERITY_INFO)) {
                    severityKey = "info";
                }
                else if (severity.equals(FacesMessage.SEVERITY_WARN)) {
                    severityKey = "warn";
                }
                else if (severity.equals(FacesMessage.SEVERITY_FATAL)) {
                    severityKey = "fatal";
                }

                styleClass += " ui-message-" + severityKey + " ui-widget ui-corner-all";

                if (iconOnly) {
                    styleClass += " ui-message-icon-only ui-helper-clearfix";
                }

                writer.writeAttribute(CLASS, styleClass, null);
                writer.writeAttribute(ROLE, "alert", null);
                writer.writeAttribute("aria-atomic", "true", null);

                if (!display.equals("text")) {
                    encodeIcon(writer, severityKey, msg.getDetail(), iconOnly);
                }

                if (!iconOnly) {
                    if (uiMessage.isShowSummary()) {
                        encodeText(writer, msg.getSummary(), severityKey + "-summary", escape);
                    }
                    if (uiMessage.isShowDetail()) {
                        encodeText(writer, msg.getDetail(), severityKey + "-detail", escape);
                    }
                }

                msg.rendered();
            }
        }
        else {
            writer.writeAttribute(CLASS, styleClass, null);
        }

        writer.endElement(DIV);
    }

    protected void encodeText(ResponseWriter writer, String text, String severity, boolean escape) throws IOException {
        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, "ui-message-" + severity, null);

        if (escape) {
            writer.writeText(text, null);
        }
        else {
            writer.write(text);
        }

        writer.endElement(SPAN);
    }

    protected void encodeIcon(ResponseWriter writer, String severity, String title, boolean iconOnly) throws IOException {
        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, "ui-message-" + severity + "-icon", null);
        if (iconOnly) {
            writer.writeAttribute(TITLE, title, null);
        }
        writer.endElement(SPAN);
    }

    protected void encodeScript(FacesContext context, Message uiMessage, UIComponent target) throws IOException {
        if (uiMessage.getDisplay().equals("tooltip")) {
            String clientId = uiMessage.getClientId(context);
            String targetClientId = (target instanceof InputHolder) ? ((InputHolder) target).getInputClientId() : target.getClientId(context);
            WidgetBuilder wb = getWidgetBuilder(context);

            wb.init("Message", uiMessage.resolveWidgetVar(), clientId)
                    .attr("target", targetClientId)
                    .finish();
        }
    }
}
