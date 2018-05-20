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
package org.primefaces.component.selectmanymenu;

import java.io.IOException;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectMany;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.model.SelectItem;
import javax.faces.render.Renderer;
import org.primefaces.component.column.Column;
import org.primefaces.context.PrimeApplicationContext;
import org.primefaces.renderkit.RendererUtils;
import org.primefaces.renderkit.SelectManyRenderer;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.WidgetBuilder;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DISABLED;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.FILTER;
import static org.primefaces.component.Literals.INPUT;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.OPTION;
import static org.primefaces.component.Literals.SELECT;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TABINDEX;
import static org.primefaces.component.Literals.TABLE;
import static org.primefaces.component.Literals.TBODY;
import static org.primefaces.component.Literals.TITLE;
import static org.primefaces.component.Literals.TYPE;
import static org.primefaces.component.Literals.VALUE;

public class SelectManyMenuRenderer extends SelectManyRenderer {

    @Override
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
        Renderer renderer = ComponentUtils.getUnwrappedRenderer(
                context,
                "javax.faces.SelectMany",
                "javax.faces.Menu",
                Renderer.class);
        return renderer.getConvertedValue(context, component, submittedValue);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        SelectManyMenu menu = (SelectManyMenu) component;

        encodeMarkup(context, menu);
        encodeScript(context, menu);
    }

    protected void encodeMarkup(FacesContext context, SelectManyMenu menu) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = menu.getClientId(context);
        List<SelectItem> selectItems = getSelectItems(context, menu);

        String style = menu.getStyle();
        String styleClass = menu.getStyleClass();
        styleClass = styleClass == null ? SelectManyMenu.CONTAINER_CLASS : SelectManyMenu.CONTAINER_CLASS + " " + styleClass;
        styleClass = menu.isDisabled() ? styleClass + " ui-state-disabled" : styleClass;
        styleClass = !menu.isValid() ? styleClass + " ui-state-error" : styleClass;

        writer.startElement(DIV, menu);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(CLASS, styleClass, "styleClass");
        if (style != null) {
            writer.writeAttribute(STYLE, style, STYLE);
        }

        if (menu.isFilter()) {
            encodeFilter(context, menu);
        }

        encodeInput(context, menu, clientId, selectItems);
        encodeList(context, menu, selectItems);

        writer.endElement(DIV);
    }

    protected void encodeScript(FacesContext context, SelectManyMenu menu) throws IOException {
        String clientId = menu.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("SelectManyMenu", menu.resolveWidgetVar(), clientId)
                .attr(DISABLED, menu.isDisabled(), false)
                .attr("showCheckbox", menu.isShowCheckbox(), false);

        if (menu.isFilter()) {
            wb.attr(FILTER, true)
                    .attr("filterMatchMode", menu.getFilterMatchMode(), null)
                    .nativeAttr("filterFunction", menu.getFilterFunction(), null)
                    .attr("caseSensitive", menu.isCaseSensitive(), false);
        }

        wb.finish();
    }

    protected void encodeInput(FacesContext context, SelectManyMenu menu, String clientId, List<SelectItem> selectItems)
            throws IOException {
        
        ResponseWriter writer = context.getResponseWriter();
        String inputid = clientId + "_input";
        String labelledBy = menu.getLabelledBy();

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, "ui-helper-hidden-accessible", null);

        writer.startElement(SELECT, null);
        writer.writeAttribute("id", inputid, "id");
        writer.writeAttribute(NAME, inputid, null);
        writer.writeAttribute("multiple", "multiple", null);
        writer.writeAttribute("size", "2", null);   //prevent browser to send value when no item is selected

        renderDomEvents(context, menu, SelectManyMenu.DOM_EVENTS);

        if (labelledBy != null) {
            writer.writeAttribute("aria-labelledby", labelledBy, null);
        }

        if (menu.getTabindex() != null) {
            writer.writeAttribute(TABINDEX, menu.getTabindex(), null);
        }
        
        if (menu.isDisabled()) {
            writer.writeAttribute(DISABLED, DISABLED, null);
        }

        if (PrimeApplicationContext.getCurrentInstance(context).getConfig().isClientSideValidationEnabled()) {
            renderValidationMetadata(context, menu);
        }

        encodeSelectItems(context, menu, selectItems);

        writer.endElement(SELECT);

        writer.endElement(DIV);
    }

    protected void encodeList(FacesContext context, SelectManyMenu menu, List<SelectItem> selectItems) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        Converter converter = menu.getConverter();
        Object values = getValues(menu);
        Object submittedValues = getSubmittedValues(menu);
        boolean customContent = menu.getVar() != null;
        boolean showCheckbox = menu.isShowCheckbox();

        writer.startElement(DIV, menu);
        writer.writeAttribute(CLASS, SelectManyMenu.LIST_CONTAINER_CLASS, null);
        writer.writeAttribute(STYLE, "height:" + calculateWrapperHeight(menu, countSelectItems(selectItems)), null);

        if (customContent) {
            writer.startElement(TABLE, null);
            writer.writeAttribute(CLASS, SelectManyMenu.LIST_CLASS, null);
            writer.startElement(TBODY, null);
            for (int i = 0; i < selectItems.size(); i++) {
                SelectItem selectItem = selectItems.get(i);
                encodeItem(context, menu, selectItem, values, submittedValues, converter, customContent, showCheckbox);
            }
            writer.endElement(TBODY);
            writer.endElement(TABLE);
        }
        else {
            writer.startElement("ul", null);
            writer.writeAttribute(CLASS, SelectManyMenu.LIST_CLASS, null);
            for (int i = 0; i < selectItems.size(); i++) {
                SelectItem selectItem = selectItems.get(i);
                encodeItem(context, menu, selectItem, values, submittedValues, converter, customContent, showCheckbox);
            }
            writer.endElement("ul");
        }

        writer.endElement(DIV);
    }

    protected void encodeItem(FacesContext context, SelectManyMenu menu, SelectItem option, Object values, Object submittedValues,
            Converter converter, boolean customContent, boolean showCheckbox) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String itemValueAsString = getOptionAsString(context, menu, converter, option.getValue());
        boolean disabled = option.isDisabled() || menu.isDisabled();
        String itemClass = disabled ? SelectManyMenu.ITEM_CLASS + " ui-state-disabled" : SelectManyMenu.ITEM_CLASS;

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

        boolean selected = isSelected(context, menu, itemValue, valuesArray, converter);
        if (option.isNoSelectionOption() && values != null && !selected) {
            return;
        }

        if (selected) {
            itemClass = itemClass + " ui-state-highlight";
        }

        if (customContent) {
            String var = menu.getVar();
            context.getExternalContext().getRequestMap().put(var, option.getValue());

            writer.startElement("tr", null);
            writer.writeAttribute(CLASS, itemClass, null);
            if (option.getDescription() != null) {
                writer.writeAttribute(TITLE, option.getDescription(), null);
            }

            if (showCheckbox) {
                writer.startElement("td", null);
                RendererUtils.encodeCheckbox(context, selected);
                writer.endElement("td");
            }

            for (UIComponent child : menu.getChildren()) {
                if (child instanceof Column && child.isRendered()) {
                    String style = ((Column) child).getStyle();
                    String styleClass = ((Column) child).getStyleClass();

                    writer.startElement("td", null);
                    if (styleClass != null) writer.writeAttribute(CLASS, styleClass, "styleClass");
                    if (style != null) writer.writeAttribute(STYLE, style, STYLE);

                    renderChildren(context, child);
                    writer.endElement("td");
                }
            }

            writer.endElement("tr");
        }
        else {
            writer.startElement("li", null);
            writer.writeAttribute(CLASS, itemClass, null);

            if (showCheckbox) {
                RendererUtils.encodeCheckbox(context, selected);
            }

            if (option.isEscape()) {
                writer.writeText(option.getLabel(), null);
            }
            else {
                writer.write(option.getLabel());
            }

            writer.endElement("li");
        }

    }

    protected void encodeSelectItems(FacesContext context, SelectManyMenu menu, List<SelectItem> selectItems) throws IOException {
        Converter converter = menu.getConverter();
        Object values = getValues(menu);
        Object submittedValues = getSubmittedValues(menu);

        for (int i = 0; i < selectItems.size(); i++) {
            SelectItem selectItem = selectItems.get(i);
            encodeOption(context, menu, selectItem, values, submittedValues, converter);
        }
    }

    protected void encodeOption(FacesContext context, SelectManyMenu menu, SelectItem option, Object values, Object submittedValues,
            Converter converter) throws IOException {
        
        ResponseWriter writer = context.getResponseWriter();
        String itemValueAsString = getOptionAsString(context, menu, converter, option.getValue());
        boolean disabled = option.isDisabled() || menu.isDisabled();

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

        boolean selected = isSelected(context, menu, itemValue, valuesArray, converter);
        if (option.isNoSelectionOption() && values != null && !selected) {
            return;
        }

        writer.startElement(OPTION, null);
        writer.writeAttribute(VALUE, itemValueAsString, null);
        if (disabled) writer.writeAttribute(DISABLED, DISABLED, null);
        if (selected) writer.writeAttribute("selected", "selected", null);

        if (option.isEscape()) {
            writer.writeText(option.getLabel(), null);
        }
        else {
            writer.write(option.getLabel());
        }

        writer.endElement(OPTION);
    }

    protected void encodeFilter(FacesContext context, SelectManyMenu menu) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String id = menu.getClientId(context) + "_filter";
        boolean disabled = menu.isDisabled();
        String filterClass = disabled ? SelectManyMenu.FILTER_CLASS + " ui-state-disabled" : SelectManyMenu.FILTER_CLASS;

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, SelectManyMenu.FILTER_CONTAINER_CLASS, null);

        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, SelectManyMenu.FILTER_ICON_CLASS, id);
        writer.endElement(SPAN);

        writer.startElement(INPUT, null);
        writer.writeAttribute(CLASS, filterClass, null);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute(NAME, id, null);
        writer.writeAttribute(TYPE, "text", null);
        writer.writeAttribute("autocomplete", "off", null);
        if (disabled) writer.writeAttribute(DISABLED, DISABLED, null);

        writer.endElement(INPUT);

        writer.endElement(DIV);
    }

    protected String calculateWrapperHeight(SelectManyMenu menu, int itemSize) {
        int height = menu.getScrollHeight();

        if (height != Integer.MAX_VALUE) {
            return height + "px";
        } 
        else if (itemSize > 10) {
            return 200 + "px";
        }

        return "auto";
    }
    
    @Override
    protected String getSubmitParam(FacesContext context, UISelectMany selectMany) {
        return selectMany.getClientId(context) + "_input";
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        //Rendering happens on encodeEnd
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public String getHighlighter() {
        return "listbox";
    }
}
