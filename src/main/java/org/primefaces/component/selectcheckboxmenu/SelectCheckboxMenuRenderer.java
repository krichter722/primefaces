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
package org.primefaces.component.selectcheckboxmenu;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UISelectMany;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.faces.render.Renderer;
import static org.primefaces.component.Literals.UI_STATE_ERROR;
import org.primefaces.expression.SearchExpressionFacade;
import org.primefaces.renderkit.SelectManyRenderer;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.WidgetBuilder;
import static org.primefaces.component.Literals.CHECKED;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DISABLED;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.FILTER;
import static org.primefaces.component.Literals.INPUT;
import static org.primefaces.component.Literals.LABEL;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.READONLY;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TABINDEX;
import static org.primefaces.component.Literals.TITLE;
import static org.primefaces.component.Literals.TYPE;
import static org.primefaces.component.Literals.VALUE;

public class SelectCheckboxMenuRenderer extends SelectManyRenderer {

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
        SelectCheckboxMenu menu = (SelectCheckboxMenu) component;

        encodeMarkup(context, menu);
        encodeScript(context, menu);
    }

    protected void encodeMarkup(FacesContext context, SelectCheckboxMenu menu) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = menu.getClientId(context);
        List<SelectItem> selectItems = getSelectItems(context, menu);
        boolean valid = menu.isValid();
        String title = menu.getTitle();

        String style = menu.getStyle();
        String styleclass = menu.getStyleClass();
        styleclass = styleclass == null ? SelectCheckboxMenu.STYLE_CLASS : SelectCheckboxMenu.STYLE_CLASS + " " + styleclass;
        styleclass = menu.isDisabled() ? styleclass + " ui-state-disabled" : styleclass;
        styleclass = !valid ? styleclass + " " + UI_STATE_ERROR : styleclass;
        styleclass = menu.isMultiple() ? SelectCheckboxMenu.MULTIPLE_CLASS + " " + styleclass : styleclass;

        writer.startElement(DIV, menu);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(CLASS, styleclass, "styleclass");
        if (style != null) writer.writeAttribute(STYLE, style, STYLE);
        if (title != null) writer.writeAttribute(TITLE, title, TITLE);

        encodeKeyboardTarget(context, menu);
        encodeInputs(context, menu, selectItems);
        if (menu.isMultiple()) {
            encodeMultipleLabel(context, menu, selectItems, valid);
        }
        else {
            encodeLabel(context, menu, selectItems, valid);
        }

        encodeMenuIcon(context, menu, valid);

        writer.endElement(DIV);
    }

    protected void encodeInputs(FacesContext context, SelectCheckboxMenu menu, List<SelectItem> selectItems) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        Converter converter = menu.getConverter();
        Object values = getValues(menu);
        Object submittedValues = getSubmittedValues(menu);

        writer.startElement(DIV, menu);
        writer.writeAttribute(CLASS, "ui-helper-hidden", null);

        int idx = -1;
        for (int i = 0; i < selectItems.size(); i++) {
            SelectItem selectItem = selectItems.get(i);
            if (selectItem instanceof SelectItemGroup) {
                SelectItemGroup selectItemGroup = (SelectItemGroup) selectItem;
                String selectItemGroupLabel = selectItemGroup.getLabel() == null ? "" : selectItemGroup.getLabel();
                for (SelectItem childSelectItem : selectItemGroup.getSelectItems()) {
                    idx++;
                    encodeOption(context, menu, values, submittedValues, converter, childSelectItem, idx, selectItemGroupLabel);
                }
            }
            else {
                idx++;
                encodeOption(context, menu, values, submittedValues, converter, selectItem, idx);
            }
        }

        writer.endElement(DIV);
    }

    protected void encodeOption(FacesContext context, SelectCheckboxMenu menu, Object values, Object submittedValues,
            Converter converter, SelectItem option, int idx) throws IOException {
        encodeOption(context, menu, values, submittedValues, converter, option, idx, null);
    }

    protected void encodeOption(FacesContext context, SelectCheckboxMenu menu, Object values, Object submittedValues,
            Converter converter, SelectItem option, int idx, String selectItemGroupLabel) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String itemValueAsString = getOptionAsString(context, menu, converter, option.getValue());
        String name = menu.getClientId(context);
        String id = name + UINamingContainer.getSeparatorChar(context) + idx;
        boolean disabled = option.isDisabled() || menu.isDisabled();
        boolean escaped = option.isEscape();
        String itemLabel = option.getLabel();
        itemLabel = isValueBlank(itemLabel) ? "&nbsp;" : itemLabel;

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

        boolean checked = isSelected(context, menu, itemValue, valuesArray, converter);
        if (option.isNoSelectionOption() && values != null && !checked) {
            return;
        }

        //input
        writer.startElement(INPUT, null);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute(NAME, name, null);
        writer.writeAttribute(TYPE, "checkbox", null);
        writer.writeAttribute(VALUE, itemValueAsString, null);
        writer.writeAttribute("data-escaped", String.valueOf(escaped), null);
        if (selectItemGroupLabel != null) {
            writer.writeAttribute("group-label", selectItemGroupLabel, null);
        }

        if (checked) writer.writeAttribute(CHECKED, CHECKED, null);
        if (disabled) writer.writeAttribute(DISABLED, DISABLED, null);
        if (option.getDescription() != null) writer.writeAttribute(TITLE, option.getDescription(), null);
        if (menu.getOnchange() != null) writer.writeAttribute("onchange", menu.getOnchange(), null);

        writer.endElement(INPUT);

        //label
        writer.startElement(LABEL, null);
        writer.writeAttribute("for", id, null);
        if (disabled) {
            writer.writeAttribute(CLASS, "ui-state-disabled", null);
        }

        if (itemLabel.equals("&nbsp;")) {
            writer.write(itemLabel);
        }
        else {
            if (escaped) {
                writer.writeText(itemLabel, VALUE);
            }
            else {
                writer.write(itemLabel);
            }
        }

        writer.endElement(LABEL);
    }

    protected void encodeLabel(FacesContext context, SelectCheckboxMenu menu, List<SelectItem> selectItems, boolean valid) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String label = menu.getLabel();
        String labelClass = !valid ? SelectCheckboxMenu.LABEL_CLASS + " " + UI_STATE_ERROR : SelectCheckboxMenu.LABEL_CLASS;
        if (label == null) {
            label = "&nbsp;";
        }

        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, SelectCheckboxMenu.LABEL_CONTAINER_CLASS, null);
        writer.startElement(LABEL, null);
        writer.writeAttribute(CLASS, labelClass, null);
        writer.writeText(label, null);
        writer.endElement(LABEL);
        writer.endElement(SPAN);
    }

    protected void encodeMultipleLabel(FacesContext context, SelectCheckboxMenu menu, List<SelectItem> selectItems, boolean valid)
            throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        Converter converter = menu.getConverter();
        Object values = getValues(menu);
        Object submittedValues = getSubmittedValues(menu);
        Object valuesArray = (submittedValues != null) ? submittedValues : values;
        String listClass = menu.isDisabled() ?
                SelectCheckboxMenu.MULTIPLE_CONTAINER_CLASS + " ui-state-disabled" : SelectCheckboxMenu.MULTIPLE_CONTAINER_CLASS;
        listClass = valid ? listClass : listClass + " " + UI_STATE_ERROR;

        writer.startElement("ul", null);
        writer.writeAttribute(CLASS, listClass, null);
        if (valuesArray != null) {
            int length = Array.getLength(valuesArray);
            for (int i = 0; i < length; i++) {
                Object value = Array.get(valuesArray, i);
                String itemValueAsString = getOptionAsString(context, menu, converter, value);
                writer.startElement("li", null);
                writer.writeAttribute(CLASS, SelectCheckboxMenu.TOKEN_DISPLAY_CLASS, null);
                writer.writeAttribute("data-item-value", itemValueAsString, null);

                writer.startElement(SPAN, null);
                writer.writeAttribute(CLASS, SelectCheckboxMenu.TOKEN_LABEL_CLASS, null);

                SelectItem selectedItem = null;
                for (SelectItem item : selectItems) {
                    if (item instanceof SelectItemGroup) {
                        SelectItemGroup group = (SelectItemGroup) item;
                        for (SelectItem groupItem : group.getSelectItems()) {
                            if (value.equals(groupItem.getValue())) {
                                selectedItem = groupItem;
                                break;
                            }
                        }
                    } 
                    else if (value.equals(item.getValue())) {
                        selectedItem = item;
                        break;
                    }
                }

                if (selectedItem != null && selectedItem.getLabel() != null) {
                    if (selectedItem.isEscape()) {
                        writer.writeText(selectedItem.getLabel(), null);
                    }
                    else {
                        writer.write(selectedItem.getLabel());
                    }
                }
                else {
                    writer.writeText(value, null);
                }

                writer.endElement(SPAN);

                writer.startElement(SPAN, null);
                writer.writeAttribute(CLASS, SelectCheckboxMenu.TOKEN_ICON_CLASS, null);
                writer.endElement(SPAN);

                writer.endElement("li");
            }
        }

        writer.endElement("ul");
    }

    protected void encodeMenuIcon(FacesContext context, SelectCheckboxMenu menu, boolean valid) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String iconClass = valid ? SelectCheckboxMenu.TRIGGER_CLASS : SelectCheckboxMenu.TRIGGER_CLASS + " " + UI_STATE_ERROR;

        writer.startElement(DIV, menu);
        writer.writeAttribute(CLASS, iconClass, null);

        writer.startElement(SPAN, menu);
        writer.writeAttribute(CLASS, "ui-icon ui-icon-triangle-1-s", null);
        writer.endElement(SPAN);

        writer.endElement(DIV);
    }

    protected void encodeScript(FacesContext context, SelectCheckboxMenu menu) throws IOException {
        String clientId = menu.getClientId(context);

        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("SelectCheckboxMenu", menu.resolveWidgetVar(), clientId)
                .callback("onShow", "function()", menu.getOnShow())
                .callback("onHide", "function()", menu.getOnHide())
                .callback("onChange", "function()", menu.getOnchange())
                .attr("scrollHeight", menu.getScrollHeight(), Integer.MAX_VALUE)
                .attr("showHeader", menu.isShowHeader(), true)
                .attr("updateLabel", menu.isUpdateLabel(), false)
                .attr("multiple", menu.isMultiple(), false)
                .attr("dynamic", menu.isDynamic(), false)
                .attr("appendTo", SearchExpressionFacade.resolveClientId(context, menu, menu.getAppendTo()), null);

        if (menu.isFilter()) {
            wb.attr(FILTER, true)
                    .attr("filterMatchMode", menu.getFilterMatchMode(), null)
                    .nativeAttr("filterFunction", menu.getFilterFunction(), null)
                    .attr("caseSensitive", menu.isCaseSensitive(), false);
        }

        wb.attr("panelStyle", menu.getPanelStyle(), null).attr("panelStyleClass", menu.getPanelStyleClass(), null);

        encodeClientBehaviors(context, menu);

        wb.finish();
    }

    @Override
    protected String getSubmitParam(FacesContext context, UISelectMany selectMany) {
        return selectMany.getClientId(context);
    }

    protected void encodeKeyboardTarget(FacesContext context, SelectCheckboxMenu menu) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String inputId = menu.getClientId(context) + "_focus";
        String tabindex = menu.getTabindex();

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, "ui-helper-hidden-accessible", null);
        writer.startElement(INPUT, menu);
        writer.writeAttribute("id", inputId, null);
        writer.writeAttribute(NAME, inputId, null);
        writer.writeAttribute(TYPE, "text", null);
        writer.writeAttribute(READONLY, READONLY, null);
        if (tabindex != null) {
            writer.writeAttribute(TABINDEX, tabindex, null);
        }
        writer.endElement(INPUT);
        writer.endElement(DIV);
    }
}
