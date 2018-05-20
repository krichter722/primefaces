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
package org.primefaces.component.selectmanycheckbox;

import java.io.IOException;
import java.util.List;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UISelectMany;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.faces.render.Renderer;
import org.primefaces.context.PrimeApplicationContext;
import org.primefaces.renderkit.SelectManyRenderer;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.GridLayoutUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.WidgetBuilder;
import static org.primefaces.component.Literals.CHECKED;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DISABLED;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.GRID;
import static org.primefaces.component.Literals.INPUT;
import static org.primefaces.component.Literals.LABEL;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.ROLE;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TABINDEX;
import static org.primefaces.component.Literals.TABLE;
import static org.primefaces.component.Literals.TITLE;
import static org.primefaces.component.Literals.TYPE;
import static org.primefaces.component.Literals.VALUE;

public class SelectManyCheckboxRenderer extends SelectManyRenderer {

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
        SelectManyCheckbox checkbox = (SelectManyCheckbox) component;

        encodeMarkup(context, checkbox);
        encodeScript(context, checkbox);
    }

    protected void encodeMarkup(FacesContext context, SelectManyCheckbox checkbox) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String layout = checkbox.getLayout();
        if (layout == null) {
            layout = "lineDirection";
        }
        boolean custom = (layout.equals("custom"));

        if (custom) {
            writer.startElement(SPAN, checkbox);
            writer.writeAttribute("id", checkbox.getClientId(context), "id");
            writer.writeAttribute(CLASS, "ui-helper-hidden", null);
            encodeCustomLayout(context, checkbox);
            writer.endElement(SPAN);
        }
        else if (layout.equals("responsive")) {
            encodeResponsiveLayout(context, checkbox);
        }
        else {
            encodeTabularLayout(context, checkbox, layout);
        }
    }

    protected void encodeScript(FacesContext context, SelectManyCheckbox checkbox) throws IOException {
        String clientId = checkbox.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        String layout = checkbox.getLayout();
        boolean custom = (layout != null && layout.equals("custom"));

        wb.init("SelectManyCheckbox", checkbox.resolveWidgetVar(), clientId)
                .attr("custom", custom, false).finish();
    }

    protected void encodeResponsiveLayout(FacesContext context, SelectManyCheckbox checkbox) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = checkbox.getClientId(context);
        String style = checkbox.getStyle();
        String styleClass = checkbox.getStyleClass();
        styleClass = (styleClass == null) ? SelectManyCheckbox.STYLE_CLASS : SelectManyCheckbox.STYLE_CLASS + " " + styleClass;
        styleClass = styleClass + " ui-grid ui-grid-responsive";
        int columns = checkbox.getColumns();

        if (columns <= 0) {
            throw new FacesException("The value of columns attribute must be greater than zero.");
        }

        writer.startElement(DIV, checkbox);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(CLASS, styleClass, "styleClass");
        if (style != null) {
            writer.writeAttribute(STYLE, style, STYLE);
        }

        List<SelectItem> selectItems = getSelectItems(context, checkbox);
        Converter converter = checkbox.getConverter();
        Object values = getValues(checkbox);
        Object submittedValues = getSubmittedValues(checkbox);
        int idx = 0, groupIdx = 0, colMod;
        for (int i = 0; i < selectItems.size(); i++) {
            SelectItem selectItem = selectItems.get(i);
            if (selectItem instanceof SelectItemGroup) {
                writer.startElement(DIV, null);
                writer.writeAttribute(CLASS, "ui-selectmanycheckbox-responsive-group", null);
                encodeGroupLabel(context, checkbox, (SelectItemGroup) selectItem);
                writer.endElement(DIV);

                for (SelectItem childSelectItem : ((SelectItemGroup) selectItem).getSelectItems()) {
                    colMod = idx % columns;
                    if (colMod == 0) {
                        writer.startElement(DIV, null);
                        writer.writeAttribute(CLASS, "ui-g", null);
                    }

                    groupIdx++;

                    writer.startElement(DIV, null);
                    writer.writeAttribute(CLASS, GridLayoutUtils.getColumnClass(columns), null);
                    encodeOption(context, checkbox, values, submittedValues, converter, childSelectItem, groupIdx);
                    writer.endElement(DIV);

                    idx++;
                    colMod = idx % columns;

                    if (colMod == 0) {
                        writer.endElement(DIV);
                    }
                }

                if (idx != 0 && (idx % columns) != 0) {
                    writer.endElement(DIV);
                }

                idx = 0;
            }
            else {
                colMod = idx % columns;
                if (colMod == 0) {
                    writer.startElement(DIV, null);
                    writer.writeAttribute(CLASS, "ui-g", null);
                }

                writer.startElement(DIV, null);
                writer.writeAttribute(CLASS, GridLayoutUtils.getColumnClass(columns), null);
                encodeOption(context, checkbox, values, submittedValues, converter, selectItem, idx);
                writer.endElement(DIV);

                idx++;
                colMod = idx % columns;

                if (colMod == 0) {
                    writer.endElement(DIV);
                }
            }
        }

        if (idx != 0 && (idx % columns) != 0) {
            writer.endElement(DIV);
        }

        writer.endElement(DIV);
    }

    protected void encodeTabularLayout(FacesContext context, SelectManyCheckbox checkbox, String layout) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = checkbox.getClientId(context);
        String style = checkbox.getStyle();
        String styleClass = checkbox.getStyleClass();
        styleClass = styleClass == null ? SelectManyCheckbox.STYLE_CLASS : SelectManyCheckbox.STYLE_CLASS + " " + styleClass;

        writer.startElement(TABLE, checkbox);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(ROLE, "presentation", null);
        writer.writeAttribute(CLASS, styleClass, "styleClass");
        if (style != null) {
            writer.writeAttribute(STYLE, style, STYLE);
        }

        encodeSelectItems(context, checkbox, layout);

        writer.endElement(TABLE);
    }

    protected void encodeOptionInput(FacesContext context, SelectManyCheckbox checkbox, String id, String name, boolean checked,
            boolean disabled, String value) throws IOException {
        
        ResponseWriter writer = context.getResponseWriter();
        String tabindex = checkbox.getTabindex();

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, "ui-helper-hidden-accessible", null);

        writer.startElement(INPUT, null);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute(NAME, name, null);
        writer.writeAttribute(TYPE, "checkbox", null);
        writer.writeAttribute(VALUE, value, null);
        if (tabindex != null) {
            writer.writeAttribute(TABINDEX, tabindex, null);
        }

        renderOnchange(context, checkbox);

        if (checked) writer.writeAttribute(CHECKED, CHECKED, null);
        if (disabled) writer.writeAttribute(DISABLED, DISABLED, null);

        if (PrimeApplicationContext.getCurrentInstance(context).getConfig().isClientSideValidationEnabled()) {
            renderValidationMetadata(context, checkbox);
        }

        writer.endElement(INPUT);

        writer.endElement(DIV);
    }

    protected void encodeOptionLabel(FacesContext context, SelectManyCheckbox checkbox, String containerClientId, SelectItem option,
            boolean disabled) throws IOException {
        
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement(LABEL, null);
        if (disabled) {
            writer.writeAttribute(CLASS, "ui-state-disabled", null);
        }

        writer.writeAttribute("for", containerClientId, null);
        
        if (option.getDescription() != null) {
            writer.writeAttribute(TITLE, option.getDescription(), null);
        }

        if (option.isEscape()) {
            writer.writeText(option.getLabel(), null);
        }
        else {
            writer.write(option.getLabel());
        }

        writer.endElement(LABEL);
    }

    protected void encodeGroupLabel(FacesContext context, SelectManyCheckbox checkbox, SelectItemGroup group) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, "ui-selectmanycheckbox-item-group", null);

        if (group.isEscape()) {
            writer.writeText(group.getLabel(), null);
        }
        else {
            writer.write(group.getLabel());
        }

        writer.endElement(SPAN);
    }

    protected void encodeOptionOutput(FacesContext context, SelectManyCheckbox checkbox, boolean checked, boolean disabled) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String boxClass = HTML.CHECKBOX_BOX_CLASS;
        boxClass = checked ? boxClass + " ui-state-active" : boxClass;
        boxClass = disabled ? boxClass + " ui-state-disabled" : boxClass;
        boxClass = !checkbox.isValid() ? boxClass + " ui-state-error" : boxClass;

        String iconClass = checked ? HTML.CHECKBOX_CHECKED_ICON_CLASS : HTML.CHECKBOX_UNCHECKED_ICON_CLASS;

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, boxClass, null);

        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, iconClass, null);
        writer.endElement(SPAN);

        writer.endElement(DIV);
    }

    protected void encodeSelectItems(FacesContext context, SelectManyCheckbox checkbox, String layout) throws IOException {
        if (layout.equals("lineDirection")) {
            encodeLineLayout(context, checkbox);
        }
        else if (layout.equals("pageDirection")) {
            encodePageLayout(context, checkbox);
        }
        else if (layout.equals(GRID)) {
            encodeGridLayout(context, checkbox);
        }
        else {
            throw new FacesException("Invalid '" + layout + "' type for component '" + checkbox.getClientId(context) + "'.");
        }
    }

    protected void encodeLineLayout(FacesContext context, SelectManyCheckbox checkbox) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        List<SelectItem> selectItems = getSelectItems(context, checkbox);
        Converter converter = checkbox.getConverter();
        Object values = getValues(checkbox);
        Object submittedValues = getSubmittedValues(checkbox);

        writer.startElement("tr", null);
        for (int i = 0; i < selectItems.size(); i++) {
            SelectItem selectItem = selectItems.get(i);
            if (selectItem instanceof SelectItemGroup) {
                writer.startElement("td", null);
                encodeGroupLabel(context, checkbox, (SelectItemGroup) selectItem);
                writer.endElement("td");
            }
            else {
                writer.startElement("td", null);
                encodeOption(context, checkbox, values, submittedValues, converter, selectItem, i);
                writer.endElement("td");
            }
        }
        writer.endElement("tr");
    }

    protected void encodePageLayout(FacesContext context, SelectManyCheckbox checkbox) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        List<SelectItem> selectItems = getSelectItems(context, checkbox);
        Converter converter = checkbox.getConverter();
        Object values = getValues(checkbox);
        Object submittedValues = getSubmittedValues(checkbox);

        int idx = 0;
        for (int i = 0; i < selectItems.size(); i++) {
            SelectItem selectItem = selectItems.get(i);
            if (selectItem instanceof SelectItemGroup) {
                writer.startElement("tr", null);
                writer.startElement("td", null);
                encodeGroupLabel(context, checkbox, (SelectItemGroup) selectItem);
                writer.endElement("td");
                writer.endElement("tr");
                idx++;

                for (SelectItem childSelectItem : ((SelectItemGroup) selectItem).getSelectItems()) {
                    writer.startElement("tr", null);
                    writer.startElement("td", null);
                    encodeOption(context, checkbox, values, submittedValues, converter, childSelectItem, idx);
                    writer.endElement("td");
                    writer.endElement("tr");
                    idx++;
                }
            }
            else {
                writer.startElement("tr", null);
                writer.startElement("td", null);
                encodeOption(context, checkbox, values, submittedValues, converter, selectItem, idx);
                writer.endElement("td");
                writer.endElement("tr");
                idx++;
            }
        }
    }

    protected void encodeGridLayout(FacesContext context, SelectManyCheckbox checkbox) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        List<SelectItem> selectItems = getSelectItems(context, checkbox);
        Converter converter = checkbox.getConverter();
        Object values = getValues(checkbox);
        Object submittedValues = getSubmittedValues(checkbox);
        int columns = checkbox.getColumns();

        if (columns != 0) {
            int idx = 0, colMod;
            for (int i = 0; i < selectItems.size(); i++) {
                SelectItem selectItem = selectItems.get(i);
                colMod = idx % columns;
                if (colMod == 0) {
                    writer.startElement("tr", null);
                }

                writer.startElement("td", null);
                encodeOption(context, checkbox, values, submittedValues, converter, selectItem, idx);
                writer.endElement("td");

                idx++;
                colMod = idx % columns;

                if (colMod == 0) {
                    writer.endElement("tr");
                }
            }
        }
        else {
            throw new FacesException("The value of columns attribute must be greater than zero.");
        }
    }

    protected void encodeCustomLayout(FacesContext context, SelectManyCheckbox checkbox) throws IOException {
        List<SelectItem> selectItems = getSelectItems(context, checkbox);
        Converter converter = checkbox.getConverter();
        Object values = getValues(checkbox);
        Object submittedValues = getSubmittedValues(checkbox);

        int idx = 0;
        for (int i = 0; i < selectItems.size(); i++) {
            SelectItem selectItem = selectItems.get(i);
            String itemValueAsString = getOptionAsString(context, checkbox, converter, selectItem.getValue());
            String name = checkbox.getClientId(context);
            String id = name + UINamingContainer.getSeparatorChar(context) + idx;

            Object valuesArray;
            Object itemValue;
            if (submittedValues != null) {
                valuesArray = submittedValues;
                itemValue = itemValueAsString;
            }
            else {
                valuesArray = values;
                itemValue = selectItem.getValue();
            }

            boolean selected = isSelected(context, checkbox, itemValue, valuesArray, converter);
            if (selectItem.isNoSelectionOption() && values != null && !selected) {
                return;
            }

            encodeOptionInput(context, checkbox, id, name, selected, true, itemValueAsString);
            idx++;
        }
    }

    protected void encodeOption(FacesContext context, UIInput component, Object values, Object submittedValues, Converter converter,
            SelectItem option, int idx) throws IOException {
        
        ResponseWriter writer = context.getResponseWriter();
        SelectManyCheckbox checkbox = (SelectManyCheckbox) component;
        String itemValueAsString = getOptionAsString(context, component, converter, option.getValue());
        String name = checkbox.getClientId(context);
        String id = name + UINamingContainer.getSeparatorChar(context) + idx;
        boolean disabled = option.isDisabled() || checkbox.isDisabled();

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

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, HTML.CHECKBOX_CLASS, null);

        encodeOptionInput(context, checkbox, id, name, selected, disabled, itemValueAsString);
        encodeOptionOutput(context, checkbox, selected, disabled);

        writer.endElement(DIV);
        encodeOptionLabel(context, checkbox, id, option, disabled);
    }

    @Override
    protected String getSubmitParam(FacesContext context, UISelectMany selectMany) {
        return selectMany.getClientId(context);
    }

    @Override
    public String getHighlighter() {
        return "manychkbox";
    }

    @Override
    protected boolean isGrouped() {
        return true;
    }
}
