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
package org.primefaces.component.selectbooleanbutton;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;
import org.primefaces.context.PrimeApplicationContext;

import org.primefaces.renderkit.InputRenderer;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.WidgetBuilder;
import static org.primefaces.component.Literals.CHECKED;
import static org.primefaces.component.Literals.BUTTON;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DISABLED;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.INPUT;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TABINDEX;
import static org.primefaces.component.Literals.TITLE;
import static org.primefaces.component.Literals.TYPE;
import static org.primefaces.component.Literals.VALUE;

public class SelectBooleanButtonRenderer extends InputRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        SelectBooleanButton button = (SelectBooleanButton) component;

        if (button.isDisabled()) {
            return;
        }

        decodeBehaviors(context, button);

        String clientId = button.getClientId(context);
        String submittedValue = (String) context.getExternalContext().getRequestParameterMap().get(clientId + "_input");

        if (submittedValue != null && submittedValue.equalsIgnoreCase("on")) {
            button.setSubmittedValue(true);
        }
        else {
            button.setSubmittedValue(false);
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        SelectBooleanButton button = (SelectBooleanButton) component;

        encodeMarkup(context, button);
        encodeScript(context, button);
    }

    protected void encodeMarkup(FacesContext context, SelectBooleanButton button) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = button.getClientId(context);
        boolean checked = Boolean.valueOf(ComponentUtils.getValueToRender(context, button));
        boolean disabled = button.isDisabled();
        String inputId = clientId + "_input";
        String label = checked ? button.getOnLabel() : button.getOffLabel();
        String icon = checked ? button.getOnIcon() : button.getOffIcon();
        String title = button.getTitle();
        String style = button.getStyle();
        String styleClass = "ui-selectbooleanbutton " + button.resolveStyleClass(checked, disabled);

        //button
        writer.startElement(DIV, null);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(TYPE, BUTTON, null);
        writer.writeAttribute(CLASS,styleClass, null);
        if (disabled) {
            writer.writeAttribute(DISABLED, DISABLED, null);
        }
        if (title != null) {
            writer.writeAttribute(TITLE, title, null);
        }
        if (style != null) {
            writer.writeAttribute(STYLE, style, STYLE);
        }

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, "ui-helper-hidden-accessible", null);

        //input
        writer.startElement(INPUT, null);
        writer.writeAttribute("id", inputId, "id");
        writer.writeAttribute(NAME, inputId, null);
        writer.writeAttribute(TYPE, "checkbox", null);

        if (checked) {
            writer.writeAttribute(CHECKED, CHECKED, null);
        }
        if (disabled) {
            writer.writeAttribute(DISABLED, DISABLED, null);
        }

        if (PrimeApplicationContext.getCurrentInstance(context).getConfig().isClientSideValidationEnabled()) {
            renderValidationMetadata(context, button);
        }

        renderOnchange(context, button);
        renderDomEvents(context, button, HTML.BLUR_FOCUS_EVENTS);

        // tabindex
        if (button.getTabindex() != null) {
            writer.writeAttribute(TABINDEX, button.getTabindex(), null);
        }

        writer.endElement(INPUT);

        writer.endElement(DIV);

        //icon
        if (icon != null) {
            writer.startElement(SPAN, null);
            writer.writeAttribute(CLASS, HTML.BUTTON_LEFT_ICON_CLASS + " " + icon, null);
            writer.endElement(SPAN);
        }

        //label
        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, HTML.BUTTON_TEXT_CLASS, null);

        if (isValueBlank(label)) {
            writer.write("ui-button");
        }
        else {
            writer.writeText(label, VALUE);
        }

        writer.endElement(SPAN);

        writer.endElement(DIV);
    }

    protected void encodeScript(FacesContext context, SelectBooleanButton button) throws IOException {
        
        String onLabel = button.getOnLabel();
        String offLabel = button.getOffLabel();
        
        String clientId = button.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("SelectBooleanButton", button.resolveWidgetVar(), clientId)
                .attr("onLabel", isValueBlank(onLabel) ? "ui-button" : escapeText(onLabel))
                .attr("offLabel", isValueBlank(offLabel) ? "ui-button" : escapeText(offLabel))
                .attr("onIcon", button.getOnIcon(), null)
                .attr("offIcon", button.getOffIcon(), null);

        wb.finish();
    }

    @Override
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
        return ((submittedValue instanceof Boolean) ? submittedValue : Boolean.valueOf(submittedValue.toString()));
    }

    @Override
    protected String getHighlighter() {
        return "booleanbutton";
    }
}
