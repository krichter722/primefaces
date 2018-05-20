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
package org.primefaces.component.selectmanybutton;

import java.io.IOException;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UISelectMany;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.model.SelectItem;
import javax.faces.render.Renderer;
import org.primefaces.renderkit.SelectManyRenderer;
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

public class SelectManyButtonRenderer extends SelectManyRenderer {

    @Override
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
        Renderer renderer = ComponentUtils.getUnwrappedRenderer(
                context,
                "javax.faces.SelectMany",
                "javax.faces.Checkbox",
                Renderer.class);
        return renderer.getConvertedValue(context, component, submittedValue);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        SelectManyButton button = (SelectManyButton) component;

        encodeMarkup(context, button);
        encodeScript(context, button);
    }

    protected void encodeMarkup(FacesContext context, SelectManyButton button) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = button.getClientId(context);
        List<SelectItem> selectItems = getSelectItems(context, button);
        int selectItemsSize = selectItems.size();
        String style = button.getStyle();
        String styleClass = button.getStyleClass();
        styleClass = styleClass == null ? SelectManyButton.STYLE_CLASS : SelectManyButton.STYLE_CLASS + " " + styleClass;
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

    protected void encodeSelectItems(FacesContext context, SelectManyButton button, List<SelectItem> selectItems) throws IOException {
        Converter converter = button.getConverter();
        Object values = getValues(button);
        Object submittedValues = getSubmittedValues(button);

        for (int i = 0; i < selectItems.size(); i++) {
            SelectItem selectItem = selectItems.get(i);
            encodeOption(context, button, values, submittedValues, converter, selectItem, i, selectItems.size());
        }
    }

    protected void encodeOption(FacesContext context, UIInput component, Object values, Object submittedValues, Converter converter,
            SelectItem option, int idx, int size) throws IOException {
        
        ResponseWriter writer = context.getResponseWriter();
        SelectManyButton button = (SelectManyButton) component;
        String itemValueAsString = getOptionAsString(context, component, converter, option.getValue());
        String name = button.getClientId(context);
        String id = name + UINamingContainer.getSeparatorChar(context) + idx;
        boolean disabled = option.isDisabled() || button.isDisabled();
        String tabindex = button.getTabindex();

        Object valuesArray;
        Object itemValue;
        if (submittedValues != null) {
            valuesArray = submittedValues;
            itemValue = itemValueAsString;
        }
        else {
            valuesArray = values;
            itemValue = option.getValue();
        }

        boolean selected = isSelected(context, component, itemValue, valuesArray, converter);
        if (option.isNoSelectionOption() && values != null && !selected) {
            return;
        }

        String buttonStyle = HTML.BUTTON_TEXT_ONLY_BUTTON_FLAT_CLASS;
        if (size == 0) {
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
        if (option.getDescription() != null) writer.writeAttribute(TITLE, option.getDescription(), null);

        //input
        writer.startElement(INPUT, null);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute(NAME, name, null);
        writer.writeAttribute(TYPE, "checkbox", null);
        writer.writeAttribute(VALUE, itemValueAsString, null);
        writer.writeAttribute(CLASS, "ui-helper-hidden-accessible", null);

        renderOnchange(context, button);

        if (selected) writer.writeAttribute(CHECKED, CHECKED, null);
        if (disabled) writer.writeAttribute(DISABLED, DISABLED, null);
        if (tabindex != null) writer.writeAttribute(TABINDEX, tabindex, null);

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

    protected void encodeScript(FacesContext context, SelectManyButton button) throws IOException {
        String clientId = button.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("SelectManyButton", button.resolveWidgetVar(), clientId).finish();
    }

    @Override
    protected String getSubmitParam(FacesContext context, UISelectMany selectMany) {
        return selectMany.getClientId(context);
    }

}
