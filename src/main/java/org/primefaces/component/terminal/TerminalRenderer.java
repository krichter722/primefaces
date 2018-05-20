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
package org.primefaces.component.terminal;

import java.io.IOException;
import java.util.Arrays;

import javax.el.MethodExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.model.terminal.TerminalAutoCompleteModel;
import org.primefaces.model.terminal.TerminalAutoCompleteMatches;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.WidgetBuilder;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.INPUT;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.NULL;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TYPE;

public class TerminalRenderer extends CoreRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Terminal terminal = (Terminal) component;
           
        if (terminal.isCommandRequest()) {
            handleCommand(context, terminal);
        }
        else if (terminal.isAutoCompleteRequest()) {
            autoCompleteCommand(context, terminal);
        }
        else {
            encodeMarkup(context, terminal);
            encodeScript(context, terminal);
        }
    }

    protected void encodeMarkup(FacesContext context, Terminal terminal) throws IOException {
        String clientId = terminal.getClientId(context);
        String style = terminal.getStyle();
        String styleClass = terminal.getStyleClass();
        styleClass = (styleClass == null) ? Terminal.CONTAINER_CLASS : Terminal.CONTAINER_CLASS + " " + styleClass;
        String welcomeMessage = terminal.getWelcomeMessage();
        String prompt = terminal.getPrompt();
        String inputId = clientId + "_input";
        
        ResponseWriter writer = context.getResponseWriter();
        
        writer.startElement(DIV, terminal);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(CLASS, styleClass, "styleClass");
        if (style != null) {
            writer.writeAttribute(STYLE, style, STYLE);
        }
        
        if (welcomeMessage != null) {
            writer.startElement(DIV, null);
            if (terminal.isEscape()) {
                writer.writeText(welcomeMessage, null);
            }
            else {
                writer.write(welcomeMessage);
            }
            writer.endElement(DIV);
        }

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, Terminal.CONTENT_CLASS, null);
        writer.endElement(DIV);

        writer.startElement(DIV, null);
        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, Terminal.PROMPT_CLASS, null);
        if (terminal.isEscape()) {
            writer.writeText(prompt, null);
        }
        else {
            writer.write(prompt);
        }
        writer.endElement(SPAN);

        writer.startElement(INPUT, null);
        writer.writeAttribute("id", inputId, null);
        writer.writeAttribute(NAME, inputId, null);
        writer.writeAttribute(TYPE, "text", null);
        writer.writeAttribute("autocomplete", "off", null);
        writer.writeAttribute(CLASS, Terminal.INPUT_CLASS, null);
        writer.endElement(INPUT);

        writer.endElement(DIV);
        writer.endElement(DIV);
    }

    protected void encodeScript(FacesContext context, Terminal terminal) throws IOException {
        String clientId = terminal.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("Terminal", terminal.resolveWidgetVar(), clientId);
        wb.finish();
    }
    
    protected void handleCommand(FacesContext context, Terminal terminal) throws IOException {
        String tokens[] = getValueTokens(context, terminal);
        String command = tokens[0];
        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

        MethodExpression commandHandler = terminal.getCommandHandler();
        String result = (String) commandHandler.invoke(context.getELContext(), new Object[]{command, args});

        ResponseWriter writer = context.getResponseWriter();
        writer.writeText(result, null);
    }

    protected void autoCompleteCommand(FacesContext context, Terminal terminal) throws IOException {
        String tokens[] = getValueTokens(context, terminal);
        String command = tokens[0];
        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

        TerminalAutoCompleteModel autoCompleteModel = terminal.getAutoCompleteModel();
        ResponseWriter writer = context.getResponseWriter();
        if (autoCompleteModel == null) {
            writer.write(NULL);
        }
        else {
            TerminalAutoCompleteMatches matches = terminal.traverseAutoCompleteModel(autoCompleteModel, command, args);
            writer.writeText(matches.toString(), null);
        }
    }

    private String[] getValueTokens(FacesContext context, Terminal terminal) {
        String clientId = terminal.getClientId(context);
        String value = context.getExternalContext().getRequestParameterMap().get(clientId + "_input");
        String tokens[] = value.trim().split(" ");

        return tokens;
    }

}

