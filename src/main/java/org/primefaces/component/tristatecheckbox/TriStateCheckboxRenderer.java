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
package org.primefaces.component.tristatecheckbox;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DISABLED;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.INPUT;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.VALUE;
import org.primefaces.renderkit.InputRenderer;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.WidgetBuilder;

public class TriStateCheckboxRenderer extends InputRenderer {

    private static final String LITERAL0 = "\",\"";

    @Override
    public void decode(final FacesContext context, final UIComponent component) {
        TriStateCheckbox checkbox = (TriStateCheckbox) component;

        if (checkbox.isDisabled()) {
            return;
        }

        decodeBehaviors(context, checkbox);

        String clientId = checkbox.getClientId(context);
        String submittedValue = context.getExternalContext().getRequestParameterMap().get(clientId + "_input");

        if (submittedValue != null) {
            checkbox.setSubmittedValue(submittedValue);
        }
    }

    @Override
    public void encodeEnd(final FacesContext context, final UIComponent component) throws IOException {
        TriStateCheckbox checkbox = (TriStateCheckbox) component;

        encodeMarkup(context, checkbox);
        encodeScript(context, checkbox);
    }

    protected void encodeMarkup(final FacesContext context, final TriStateCheckbox checkbox) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = checkbox.getClientId(context);
        String valueToRenderer = ComponentUtils.getValueToRender(context, checkbox);

        int valCheck = ComponentUtils.isValueBlank(valueToRenderer) ? 0 : Integer.parseInt(valueToRenderer);

        if (valCheck > 2 || valCheck < 0) {
            valCheck = 0;
        }

        boolean disabled = checkbox.isDisabled();

        String style = checkbox.getStyle();
        String styleClass = checkbox.getStyleClass();
        styleClass = styleClass == null ? HTML.CHECKBOX_CLASS : HTML.CHECKBOX_CLASS + " " + styleClass;

        writer.startElement(DIV, checkbox);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(CLASS, styleClass, "styleClass");
        if (style != null) {
            writer.writeAttribute(STYLE, style, STYLE);
        }

        encodeInput(context, checkbox, clientId, valCheck, disabled);
        encodeOutput(context, checkbox, valCheck, disabled);
        encodeItemLabel(context, checkbox);

        writer.endElement(DIV);
    }

    protected void encodeInput(final FacesContext context, final TriStateCheckbox checkbox, final String clientId,
            final int valCheck, final boolean disabled) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String inputId = clientId + "_input";

        writer.startElement(DIV, checkbox);
        writer.writeAttribute(CLASS, HTML.CHECKBOX_INPUT_WRAPPER_CLASS, null);

        writer.startElement(INPUT, null);
        writer.writeAttribute("id", inputId, "id");
        writer.writeAttribute(NAME, inputId, null);

        writer.writeAttribute(VALUE, valCheck, null);

        if (disabled) {
            writer.writeAttribute(DISABLED, DISABLED, null);
        }

        if (checkbox.getOnchange() != null) {
            writer.writeAttribute("onchange", checkbox.getOnchange(), null);
        }

        writer.endElement(INPUT);

        writer.endElement(DIV);
    }

    protected void encodeOutput(final FacesContext context, final TriStateCheckbox checkbox, final int valCheck,
            final boolean disabled) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String styleClass = HTML.CHECKBOX_BOX_CLASS;
        styleClass = (valCheck == 1 || valCheck == 2) ? styleClass + " ui-state-active" : styleClass;
        styleClass = !checkbox.isValid() ? styleClass + " ui-state-error" : styleClass;
        styleClass = disabled ? styleClass + " ui-state-disabled" : styleClass;

        //if stateIcon is defined use it insted of default icons.
        String stateOneIconClass
                = checkbox.getStateOneIcon() != null ? TriStateCheckbox.UI_ICON + checkbox.getStateOneIcon() : "";
        String stateTwoIconClass
                = checkbox.getStateTwoIcon() != null ? TriStateCheckbox.UI_ICON + checkbox.getStateTwoIcon()
                : TriStateCheckbox.UI_ICON + "ui-icon-check";
        String stataThreeIconClass
                = checkbox.getStateThreeIcon() != null ? TriStateCheckbox.UI_ICON + checkbox.getStateThreeIcon()
                : TriStateCheckbox.UI_ICON + "ui-icon-closethick";

        String statesIconsClasses = "[\"" + stateOneIconClass + LITERAL0 + stateTwoIconClass + LITERAL0 + stataThreeIconClass + "\"]";

        String stateOneTitle = checkbox.getStateOneTitle() == null ? "" : checkbox.getStateOneTitle();
        String stateTwoTitle = checkbox.getStateTwoTitle() == null ? "" : checkbox.getStateTwoTitle();
        String stateThreeTitle = checkbox.getStateThreeTitle() == null ? "" : checkbox.getStateThreeTitle();

        String statesTitles = "[\"" + stateOneTitle + LITERAL0
                + stateTwoTitle + LITERAL0 + stateThreeTitle + "\"]";

        String iconClass = "ui-chkbox-icon ui-c"; //HTML.CHECKBOX_ICON_CLASS;
        String activeTitle = "";
        if (valCheck == 0) {
            iconClass = iconClass + " " + stateOneIconClass;
            activeTitle = stateOneTitle;
        }
        else if (valCheck == 1) {
            iconClass = iconClass + " " + stateTwoIconClass;
            activeTitle = stateTwoTitle;
        }
        else if (valCheck == 2) {
            iconClass = iconClass + " " + stataThreeIconClass;
            activeTitle = stateThreeTitle;
        }

        String dataTitles = "";
        String titleAtt = "";

        if (!ComponentUtils.isValueBlank(stateOneTitle) || !ComponentUtils.isValueBlank(stateTwoTitle) || !ComponentUtils.isValueBlank(stateThreeTitle)) {
            dataTitles = "data-titlestates='" + statesTitles + "' ";
            titleAtt = " title=\"" + activeTitle + "\" ";
        }

        String tabIndexTag = " tabIndex=0 ";
        if (checkbox.getTabindex() != null) {
            tabIndexTag = "tabIndex=" + checkbox.getTabindex() + " ";
        }

        // preparation with singe quotes for .data('iconstates')
        writer.write("<div " + tabIndexTag + titleAtt + "class=\"" + styleClass + "\" data-iconstates='" + statesIconsClasses + "' "
                + dataTitles + ">"
                + "<span class=\"" + iconClass + "\"></span></div>");

    }

    protected void encodeItemLabel(final FacesContext context, final TriStateCheckbox checkbox) throws IOException {
        String label = checkbox.getItemLabel();

        if (label != null) {
            ResponseWriter writer = context.getResponseWriter();

            writer.startElement(SPAN, null);
            writer.writeAttribute(CLASS, HTML.CHECKBOX_LABEL_CLASS, null);
            writer.writeText(label, "itemLabel");
            writer.endElement(SPAN);
        }
    }

    protected void encodeScript(final FacesContext context, final TriStateCheckbox checkbox) throws IOException {
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("TriStateCheckbox", checkbox.resolveWidgetVar(), checkbox.getClientId());
        encodeClientBehaviors(context, checkbox);
        wb.finish();
    }
}
