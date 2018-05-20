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
package org.primefaces.component.chips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DISABLED;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.INPUT;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.OPTION;
import static org.primefaces.component.Literals.READONLY;
import static org.primefaces.component.Literals.SELECT;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TITLE;
import static org.primefaces.component.Literals.TYPE;
import static org.primefaces.component.Literals.VALUE;
import org.primefaces.renderkit.InputRenderer;
import org.primefaces.util.ArrayUtils;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.WidgetBuilder;

public class ChipsRenderer extends InputRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        Chips chips = (Chips) component;
        String clientId = chips.getClientId(context);

        if (chips.isDisabled() || chips.isReadonly()) {
            return;
        }

        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        Map<String, String[]> paramValues = context.getExternalContext().getRequestParameterValuesMap();
        String[] hInputValues = paramValues.get(clientId + "_hinput");
        String[] submittedValues = (hInputValues != null) ? hInputValues : new String[]{};
        String inputValue = params.get(clientId + "_input");

        if (!isValueBlank(inputValue)) {
            submittedValues = ArrayUtils.concat(submittedValues, new String[]{inputValue});
        }

        if (submittedValues.length > 0) {
            chips.setSubmittedValue(submittedValues);
        }
        else {
            chips.setSubmittedValue("");
        }

        decodeBehaviors(context, chips);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Chips chips = (Chips) component;

        encodeMarkup(context, chips);
        encodeScript(context, chips);
    }

    protected void encodeMarkup(FacesContext context, Chips chips) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = chips.getClientId(context);
        String inputId = clientId + "_input";
        List values = (List) chips.getValue();
        List<String> stringValues = new ArrayList<String>();
        boolean disabled = chips.isDisabled();
        String title = chips.getTitle();

        String style = chips.getStyle();
        String styleClass = chips.getStyleClass();
        styleClass = styleClass == null ? Chips.STYLE_CLASS : Chips.STYLE_CLASS + " " + styleClass;

        String inputStyle = chips.getInputStyle();
        String inputStyleClass = chips.getInputStyleClass();

        String listClass = disabled ? Chips.CONTAINER_CLASS + " ui-state-disabled" : Chips.CONTAINER_CLASS;
        listClass = listClass + " " + inputStyleClass;
        listClass = chips.isValid() ? listClass : listClass + " ui-state-error";

        writer.startElement(DIV, null);
        writer.writeAttribute("id", clientId, null);
        writer.writeAttribute(CLASS, styleClass, null);
        if (style != null) {
            writer.writeAttribute(STYLE, style, null);
        }
        if (title != null) {
            writer.writeAttribute(TITLE, title, null);
        }

        writer.startElement("ul", null);
        writer.writeAttribute(CLASS, listClass, null);
        if (inputStyle != null) {
            writer.writeAttribute(STYLE, inputStyle, null);
        }

        if (values != null && !values.isEmpty()) {
            Converter converter = ComponentUtils.getConverter(context, chips);

            for (Iterator<Object> it = values.iterator(); it.hasNext();) {
                Object value = it.next();

                String tokenValue = converter != null ? converter.getAsString(context, chips, value) : String.valueOf(value);

                writer.startElement("li", null);
                writer.writeAttribute("data-token-value", tokenValue, null);
                writer.writeAttribute(CLASS, Chips.TOKEN_DISPLAY_CLASS, null);

                writer.startElement(SPAN, null);
                writer.writeAttribute(CLASS, Chips.TOKEN_LABEL_CLASS, null);
                writer.writeText(tokenValue, null);
                writer.endElement(SPAN);

                writer.startElement(SPAN, null);
                writer.writeAttribute(CLASS, Chips.TOKEN_CLOSE_ICON_CLASS, null);
                writer.endElement(SPAN);

                writer.endElement("li");

                stringValues.add(tokenValue);
            }
        }

        writer.startElement("li", null);
        writer.writeAttribute(CLASS, Chips.TOKEN_INPUT_CLASS, null);
        writer.startElement(INPUT, null);
        writer.writeAttribute(TYPE, "text", null);
        writer.writeAttribute("id", inputId, null);
        writer.writeAttribute(CLASS, "ui-widget", null);
        writer.writeAttribute(NAME, inputId, null);
        writer.writeAttribute("autocomplete", "off", null);
        if (disabled) writer.writeAttribute(DISABLED, DISABLED, DISABLED);
        if (chips.isReadonly()) writer.writeAttribute(READONLY, READONLY, READONLY);

        renderPassThruAttributes(context, chips, HTML.INPUT_TEXT_ATTRS_WITHOUT_EVENTS);
        renderDomEvents(context, chips, HTML.INPUT_TEXT_EVENTS);

        writer.endElement(INPUT);
        writer.endElement("li");

        writer.endElement("ul");

        encodeHiddenSelect(context, chips, clientId, stringValues);

        writer.endElement(DIV);
    }

    protected void encodeHiddenSelect(FacesContext context, Chips chips, String clientId, List<String> values) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String id = clientId + "_hinput";

        writer.startElement(SELECT, null);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute(NAME, id, null);
        writer.writeAttribute("multiple", "multiple", null);
        writer.writeAttribute(CLASS, "ui-helper-hidden", null);

        if (chips.isDisabled()) {
            writer.writeAttribute(DISABLED, DISABLED, DISABLED);
        }

        for (String value : values) {
            writer.startElement(OPTION, null);
            writer.writeAttribute(VALUE, value, null);
            writer.writeAttribute("selected", "selected", null);
            writer.endElement(OPTION);
        }

        writer.endElement(SELECT);
    }

    protected void encodeScript(FacesContext context, Chips chips) throws IOException {
        String clientId = chips.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("Chips", chips.resolveWidgetVar(), clientId)
                .attr("max", chips.getMax(), Integer.MAX_VALUE);

        encodeClientBehaviors(context, chips);

        wb.finish();
    }

    @Override
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
        Chips chips = (Chips) component;

        if (submittedValue == null || submittedValue.equals("")) {
            return null;
        }

        Converter converter = ComponentUtils.getConverter(context, component);
        String[] values = (String[]) submittedValue;
        List list = new ArrayList();

        for (String value : values) {
            if (isValueBlank(value)) {
                continue;
            }

            Object convertedValue = converter != null ? converter.getAsObject(context, chips, value) : value;

            if (convertedValue != null) {
                list.add(convertedValue);
            }
        }

        return list;
    }
}
