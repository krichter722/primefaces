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
package org.primefaces.component.selectonebutton;

import java.io.IOException;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.model.SelectItem;
import javax.faces.render.Renderer;
import org.primefaces.renderkit.SelectOneRenderer;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.WidgetBuilder;
import static org.primefaces.component.Literals.CHECKED;
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

public class SelectOneButtonRenderer extends SelectOneRenderer {

    @Override
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
        Renderer renderer = ComponentUtils.getUnwrappedRenderer(
                context,
                "javax.faces.SelectOne",
                "javax.faces.Radio",
                Renderer.class);
        return renderer.getConvertedValue(context, component, submittedValue);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        SelectOneButton button = (SelectOneButton) component;

        encodeMarkup(context, button);
        encodeScript(context, button);
    }

    protected void encodeMarkup(FacesContext context, SelectOneButton button) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = button.getClientId(context);
        List<SelectItem> selectItems = getSelectItems(context, button);
        int selectItemsSize = selectItems.size();
        String style = button.getStyle();
        String styleClass = button.getStyleClass();
        styleClass = styleClass == null ? SelectOneButton.STYLE_CLASS : SelectOneButton.STYLE_CLASS + " " + styleClass;
        styleClass = styleClass + " ui-buttonset-" + selectItemsSize;
        styleClass = !button.isValid() ? styleClass + " ui-state-error" : styleClass;

        writer.startElement(DIV, button);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(CLASS, styleClass, "styleClass");
        if (style != null) {
            writer.writeAttribute(STYLE, style, STYLE);
        }

        encodeSelectItems(context, button, selectItems);

        writer.endElement(DIV);
    }

    protected void encodeSelectItems(FacesContext context, SelectOneButton button, List<SelectItem> selectItems) throws IOException {
        int selectItemsSize = selectItems.size();
        Converter converter = button.getConverter();
        String name = button.getClientId(context);
        Object value = button.getSubmittedValue();
        if (value == null) {
            value = button.getValue();
        }

        Class type = value == null ? String.class : value.getClass();

        for (int i = 0; i < selectItems.size(); i++) {
            SelectItem selectItem = selectItems.get(i);
            boolean disabled = selectItem.isDisabled() || button.isDisabled();
            String id = name + UINamingContainer.getSeparatorChar(context) + i;

            boolean selected;
            if (value == null && selectItem.getValue() == null) {
                selected = true;
            }
            else {
                Object coercedItemValue = coerceToModelType(context, selectItem.getValue(), type);
                selected = (coercedItemValue != null) && coercedItemValue.equals(value);
            }

            encodeOption(context, button, selectItem, id, name, converter, selected, disabled, i, selectItemsSize);
        }
    }

    protected void encodeOption(FacesContext context, SelectOneButton button, SelectItem option, String id, String name, Converter converter,
            boolean selected, boolean disabled, int idx, int size) throws IOException {
        
        ResponseWriter writer = context.getResponseWriter();
        String itemValueAsString = getOptionAsString(context, button, converter, option.getValue());

        String buttonStyle = HTML.BUTTON_TEXT_ONLY_BUTTON_FLAT_CLASS;
        if (size == 1) {
            buttonStyle = buttonStyle + " ui-corner-all";
        }
        else if (idx == 0) {
            buttonStyle = buttonStyle + " ui-corner-left";
        }
        else if (idx == (size - 1)) {
            buttonStyle = buttonStyle + " ui-corner-right";
        }

        buttonStyle = selected ? buttonStyle + " ui-state-active" : buttonStyle;
        buttonStyle = disabled ? buttonStyle + " ui-state-disabled" : buttonStyle;

        //button
        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, buttonStyle, null);
        writer.writeAttribute(TABINDEX, button.getTabindex(), null);
        if (option.getDescription() != null) writer.writeAttribute(TITLE, option.getDescription(), null);

        //input
        writer.startElement(INPUT, null);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute(NAME, name, null);
        writer.writeAttribute(TYPE, "radio", null);
        writer.writeAttribute(VALUE, itemValueAsString, null);
        writer.writeAttribute(CLASS, "ui-helper-hidden-accessible", null);
        writer.writeAttribute(TABINDEX, "-1", null);

        if (selected) writer.writeAttribute(CHECKED, CHECKED, null);
        if (disabled) writer.writeAttribute(DISABLED, DISABLED, null);

        writer.endElement(INPUT);

        //item label
        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, HTML.BUTTON_TEXT_CLASS, null);
        
        if (option.isEscape()) {
            writer.writeText(option.getLabel(), "itemLabel");
        }
        else {
            writer.write(option.getLabel());
        }
        
        writer.endElement(SPAN);

        writer.endElement(DIV);
    }

    protected void encodeScript(FacesContext context, SelectOneButton button) throws IOException {
        String clientId = button.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("SelectOneButton", button.resolveWidgetVar(), clientId)
                .attr("unselectable", button.isUnselectable(), true)
                .callback("change", "function()", button.getOnchange());

        encodeClientBehaviors(context, button);

        wb.finish();
    }

    @Override
    protected String getSubmitParam(FacesContext context, UISelectOne selectOne) {
        return selectOne.getClientId(context);
    }
}
