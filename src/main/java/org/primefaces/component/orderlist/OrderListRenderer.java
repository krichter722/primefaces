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
package org.primefaces.component.orderlist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import static org.primefaces.component.Literals.BUTTON;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.SELECT;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TABLE;
import static org.primefaces.component.Literals.TBODY;
import static org.primefaces.component.Literals.TITLE;
import static org.primefaces.component.Literals.TYPE;
import org.primefaces.component.column.Column;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.HTML;
import org.primefaces.util.WidgetBuilder;

public class OrderListRenderer extends CoreRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        OrderList pickList = (OrderList) component;
        Map<String, String[]> params = context.getExternalContext().getRequestParameterValuesMap();
        String values = pickList.getClientId(context) + "_values";

        if (values != null) {
            pickList.setSubmittedValue(params.get(values));
        }

        decodeBehaviors(context, component);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        OrderList ol = (OrderList) component;

        encodeMarkup(context, ol);
        encodeScript(context, ol);
    }

    protected void encodeMarkup(FacesContext context, OrderList ol) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = ol.getClientId(context);
        String controlsLocation = ol.getControlsLocation();
        String style = ol.getStyle();
        String styleClass = ol.getStyleClass();
        styleClass = styleClass == null ? OrderList.CONTAINER_CLASS : OrderList.CONTAINER_CLASS + " " + styleClass;

        if (ol.isDisabled()) {
            styleClass = styleClass + " ui-state-disabled";
        }

        if (ol.isResponsive()) {
            styleClass = styleClass + " ui-grid-responsive";
        }

        writer.startElement(DIV, ol);
        writer.writeAttribute("id", clientId, null);
        writer.writeAttribute(CLASS, styleClass, null);
        if (style != null) {
            writer.writeAttribute(STYLE, style, null);
        }

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, "ui-g", null);

        if (controlsLocation.equals("left")) {
            encodeControls(context, ol);
        }

        encodeList(context, ol);

        if (controlsLocation.equals("right")) {
            encodeControls(context, ol);
        }

        writer.endElement(DIV);
        writer.endElement(DIV);
    }

    protected void encodeList(FacesContext context, OrderList ol) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = ol.getClientId(context);
        UIComponent caption = ol.getFacet("caption");
        String listStyleClass = OrderList.LIST_CLASS;
        String columnGridClass = ol.getControlsLocation().equals("none") ? "ui-g-12 ui-md-12" : "ui-g-12 ui-md-10";

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, columnGridClass, null);

        if (caption != null) {
            encodeCaption(context, caption);
            listStyleClass += " ui-corner-bottom";
        }
        else {
            listStyleClass += " ui-corner-all";
        }

        writer.startElement("ul", null);
        writer.writeAttribute(CLASS, listStyleClass, null);

        encodeOptions(context, ol, (List) ol.getValue());

        writer.endElement("ul");

        encodeInput(context, clientId + "_values");

        writer.endElement(DIV);
    }

    protected void encodeInput(FacesContext context, String clientId) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement(SELECT, null);
        writer.writeAttribute("id", clientId, null);
        writer.writeAttribute(NAME, clientId, null);
        writer.writeAttribute("multiple", "true", null);
        writer.writeAttribute(CLASS, "ui-helper-hidden", null);

        //options generated at client side
        writer.endElement(SELECT);
    }

    protected void encodeControls(FacesContext context, OrderList ol) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, OrderList.CONTROLS_CLASS, null);
        encodeButton(context, ol.getMoveUpLabel(), OrderList.MOVE_UP_BUTTON_CLASS, OrderList.MOVE_UP_BUTTON_ICON_CLASS);
        encodeButton(context, ol.getMoveTopLabel(), OrderList.MOVE_TOP_BUTTON_CLASS, OrderList.MOVE_TOP_BUTTON_ICON_CLASS);
        encodeButton(context, ol.getMoveDownLabel(), OrderList.MOVE_DOWN_BUTTON_CLASS, OrderList.MOVE_DOWN_BUTTON_ICON_CLASS);
        encodeButton(context, ol.getMoveBottomLabel(), OrderList.MOVE_BOTTOM_BUTTON_CLASS, OrderList.MOVE_BOTTOM_BUTTON_ICON_CLASS);
        writer.endElement(DIV);
    }

    @SuppressWarnings("unchecked")
    protected void encodeOptions(FacesContext context, OrderList old, List model) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String var = old.getVar();
        Converter converter = old.getConverter();

        for (Object item : model) {
            context.getExternalContext().getRequestMap().put(var, item);
            String value = converter != null ? converter.getAsString(context, old, old.getItemValue()) : old.getItemValue().toString();

            writer.startElement("li", null);
            writer.writeAttribute(CLASS, OrderList.ITEM_CLASS, null);
            writer.writeAttribute("data-item-value", value, null);

            if (old.getChildCount() > 0) {

                writer.startElement(TABLE, null);
                writer.startElement(TBODY, null);
                writer.startElement("tr", null);

                for (UIComponent kid : old.getChildren()) {
                    if (kid instanceof Column && kid.isRendered()) {
                        Column column = (Column) kid;

                        writer.startElement("td", null);
                        if (column.getStyle() != null) writer.writeAttribute(STYLE, column.getStyle(), null);
                        if (column.getStyleClass() != null) writer.writeAttribute(CLASS, column.getStyleClass(), null);

                        renderChildren(context, column);
                        writer.endElement("td");
                    }
                }

                writer.endElement("tr");
                writer.endElement(TBODY);
                writer.endElement(TABLE);
            }
            else {
                writer.writeText(old.getItemLabel(), null);
            }

            writer.endElement("li");
        }

        context.getExternalContext().getRequestMap().remove(var);
    }

    protected void encodeButton(FacesContext context, String title, String styleClass, String icon) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement(BUTTON, null);
        writer.writeAttribute(TYPE, BUTTON, null);
        writer.writeAttribute(CLASS, HTML.BUTTON_ICON_ONLY_BUTTON_CLASS + " " + styleClass, null);
        writer.writeAttribute(TITLE, title, null);

        //icon
        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, HTML.BUTTON_LEFT_ICON_CLASS + " " + icon, null);
        writer.endElement(SPAN);

        //text
        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, HTML.BUTTON_TEXT_CLASS, null);
        writer.write("ui-button");
        writer.endElement(SPAN);

        writer.endElement(BUTTON);
    }

    protected void encodeScript(FacesContext context, OrderList ol) throws IOException {
        String clientId = ol.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("OrderList", ol.resolveWidgetVar(), clientId)
                .attr("effect", ol.getEffect(), null);

        encodeClientBehaviors(context, ol);

        wb.finish();
    }

    @Override
    @SuppressWarnings({"unchecked",
        "PMD.AvoidCatchingGenericException"})
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
        try {
            OrderList ol = (OrderList) component;
            List orderedList = new ArrayList();
            Converter converter = ol.getConverter();
            String[] values = (String[]) submittedValue;

            for (String item : values) {
                if (isValueBlank(item)) {
                    continue;
                }

                Object convertedValue = converter != null ? converter.getAsObject(context, ol, item) : item;

                if (convertedValue != null) {
                    orderedList.add(convertedValue);
                }
            }

            return orderedList;
        }
        catch (Exception exception) {
            throw new ConverterException(exception);
        }
    }

    protected void encodeCaption(FacesContext context, UIComponent caption) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, OrderList.CAPTION_CLASS, null);
        caption.encodeAll(context);
        writer.endElement(DIV);
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        //Rendering happens on encodeEnd
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
