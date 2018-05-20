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
package org.primefaces.component.messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.ONCLICK;
import static org.primefaces.component.Literals.ROLE;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import org.primefaces.context.PrimeApplicationContext;
import org.primefaces.expression.SearchExpressionFacade;
import org.primefaces.expression.SearchExpressionHint;
import org.primefaces.renderkit.UINotificationRenderer;

public class MessagesRenderer extends UINotificationRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Messages uiMessages = (Messages) component;
        ResponseWriter writer = context.getResponseWriter();
        String clientId = uiMessages.getClientId(context);
        Map<String, List<FacesMessage>> messagesMap = new HashMap<String, List<FacesMessage>>();
        boolean globalOnly = uiMessages.isGlobalOnly();
        String containerClass = uiMessages.isShowIcon() ? Messages.CONTAINER_CLASS : Messages.ICONLESS_CONTAINER_CLASS;
        String style = uiMessages.getStyle();
        String styleClass = uiMessages.getStyleClass();
        styleClass = (styleClass == null) ? containerClass : containerClass + " " + styleClass;

        String _for = uiMessages.getFor();
        List<FacesMessage> messages = new ArrayList<FacesMessage>();
        if (!isValueBlank(_for)) {
            String forType = uiMessages.getForType();
            Iterator<FacesMessage> messagesIterator = context.getMessages(_for);
            
            // key case
            if (forType == null || forType.equals("key")) { 
                while (messagesIterator.hasNext()) {
                    messages.add(messagesIterator.next());
                }
            }

            // clientId / SearchExpression case
            if (forType == null || forType.equals("expression")) {
                UIComponent forComponent = SearchExpressionFacade.resolveComponent(
                        context, uiMessages, _for, SearchExpressionHint.IGNORE_NO_RESULT);
                if (forComponent != null) {
                    String forComponentClientId = forComponent.getClientId(context);
                    if (!_for.equals(forComponentClientId)) {
                        messagesIterator = context.getMessages(forComponentClientId);
                        while (messagesIterator.hasNext()) {
                            FacesMessage next = messagesIterator.next();
                            if (!messages.contains(next)) {
                                messages.add(next);
                            }
                        }
                    }
                }
            }
        }
        else {
            Iterator<FacesMessage> messagesIterator = uiMessages.isGlobalOnly() ? context.getMessages(null) : context.getMessages();
            while (messagesIterator.hasNext()) {
                messages.add(messagesIterator.next());
            }
        }

        for (int i = 0; i < messages.size(); i++) {
            FacesMessage message = messages.get(i);
            FacesMessage.Severity severity = message.getSeverity();

            if (severity.equals(FacesMessage.SEVERITY_INFO)) {
                addMessage(uiMessages, message, messagesMap, "info");
            }
            else if (severity.equals(FacesMessage.SEVERITY_WARN)) {
                addMessage(uiMessages, message, messagesMap, "warn");
            }
            else if (severity.equals(FacesMessage.SEVERITY_ERROR)) {
                addMessage(uiMessages, message, messagesMap, "error");
            }
            else if (severity.equals(FacesMessage.SEVERITY_FATAL)) {
                addMessage(uiMessages, message, messagesMap, "fatal");
            }
        }

        writer.startElement(DIV, uiMessages);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(CLASS, styleClass, null);

        if (style != null) {
            writer.writeAttribute(STYLE, style, null);
        }

        writer.writeAttribute("aria-live", "polite", null);

        if (PrimeApplicationContext.getCurrentInstance(context).getConfig().isClientSideValidationEnabled()) {
            writer.writeAttribute("data-global", String.valueOf(globalOnly), null);
            writer.writeAttribute("data-summary", uiMessages.isShowSummary(), null);
            writer.writeAttribute("data-detail", uiMessages.isShowDetail(), null);
            writer.writeAttribute("data-severity", getClientSideSeverity(uiMessages.getSeverity()), null);
            writer.writeAttribute("data-redisplay", String.valueOf(uiMessages.isRedisplay()), null);
        }

        for (String severity : messagesMap.keySet()) {
            List<FacesMessage> severityMessages = messagesMap.get(severity);

            if (severityMessages.size() > 0) {
                encodeSeverityMessages(context, uiMessages, severity, severityMessages);
            }
        }

        writer.endElement(DIV);
    }

    protected void addMessage(Messages uiMessages, FacesMessage message, Map<String, List<FacesMessage>> messagesMap, String severity) {
        if (shouldRender(uiMessages, message, severity)) {
            List<FacesMessage> severityMessages = messagesMap.get(severity);

            if (severityMessages == null) {
                severityMessages = new ArrayList<FacesMessage>();
                messagesMap.put(severity, severityMessages);
            }

            severityMessages.add(message);
        }
    }

    protected void encodeSeverityMessages(FacesContext context, Messages uiMessages, String severity, List<FacesMessage> messages) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String styleClassPrefix = Messages.SEVERITY_PREFIX_CLASS + severity;
        boolean escape = uiMessages.isEscape();

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, styleClassPrefix + " ui-corner-all", null);

        if (uiMessages.isClosable()) {
            encodeCloseIcon(context, uiMessages);
        }

        if (uiMessages.isShowIcon()) {
            writer.startElement(SPAN, null);
            writer.writeAttribute(CLASS, styleClassPrefix + "-icon", null);
            writer.endElement(SPAN);
        }

        writer.startElement("ul", null);

        for (int i = 0; i < messages.size(); i++) {
            FacesMessage message = messages.get(i);
            writer.startElement("li", null);

            writer.writeAttribute(ROLE, "alert", null);
            writer.writeAttribute("aria-atomic", "true", null);

            String summary = message.getSummary() != null ? message.getSummary() : "";
            String detail = message.getDetail() != null ? message.getDetail() : summary;

            if (uiMessages.isShowSummary()) {
                writer.startElement(SPAN, null);
                writer.writeAttribute(CLASS, styleClassPrefix + "-summary", null);

                if (escape) {
                    writer.writeText(summary, null);
                }
                else {
                    writer.write(summary);
                }

                writer.endElement(SPAN);
            }

            if (uiMessages.isShowDetail()) {
                writer.startElement(SPAN, null);
                writer.writeAttribute(CLASS, styleClassPrefix + "-detail", null);

                if (escape) {
                    writer.writeText(detail, null);
                }
                else {
                    writer.write(detail);
                }

                writer.endElement(SPAN);
            }

            writer.endElement("li");

            message.rendered();
        }

        writer.endElement("ul");

        writer.endElement(DIV);
    }

    protected void encodeCloseIcon(FacesContext context, Messages uiMessages) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement("a", null);
        writer.writeAttribute("href", "#", null);
        writer.writeAttribute(CLASS, Messages.CLOSE_LINK_CLASS, null);
        writer.writeAttribute(ONCLICK, "$(this).parent().slideUp();return false;", null);

        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, Messages.CLOSE_ICON_CLASS, null);
        writer.endElement(SPAN);

        writer.endElement("a");
    }
}
