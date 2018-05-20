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
package org.primefaces.component.fieldset;

import java.io.IOException;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import static org.primefaces.component.Literals.BUTTON;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.INPUT;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.ROLE;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TABINDEX;
import static org.primefaces.component.Literals.TITLE;
import static org.primefaces.component.Literals.TYPE;
import static org.primefaces.component.Literals.VALUE;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.HTML;
import org.primefaces.util.WidgetBuilder;

public class FieldsetRenderer extends CoreRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        Fieldset fieldset = (Fieldset) component;
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String clientId = fieldset.getClientId(context);
        String toggleStateParam = clientId + "_collapsed";

        if (params.containsKey(toggleStateParam)) {
            fieldset.setCollapsed(Boolean.valueOf(params.get(toggleStateParam)));
        }

        decodeBehaviors(context, component);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Fieldset fieldset = (Fieldset) component;

        encodeMarkup(context, fieldset);
        encodeScript(context, fieldset);
    }

    protected void encodeMarkup(FacesContext context, Fieldset fieldset) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = fieldset.getClientId(context);
        String widgetVar = fieldset.resolveWidgetVar();
        boolean toggleable = fieldset.isToggleable();
        String title = fieldset.getTitle();

        String styleClass = toggleable ? Fieldset.TOGGLEABLE_FIELDSET_CLASS : Fieldset.FIELDSET_CLASS;
        if (fieldset.isCollapsed()) {
            styleClass = styleClass + " ui-hidden-container";
        }
        if (fieldset.getStyleClass() != null) {
            styleClass = styleClass + " " + fieldset.getStyleClass();
        }

        writer.startElement("fieldset", fieldset);
        if (title != null) {
            writer.writeAttribute(TITLE, fieldset.getTitle(), null);
        }
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(CLASS, styleClass, "styleClass");
        if (fieldset.getStyle() != null) {
            writer.writeAttribute(STYLE, fieldset.getStyle(), STYLE);
        }

        writer.writeAttribute(HTML.WIDGET_VAR, widgetVar, null);

        renderDynamicPassThruAttributes(context, fieldset);
        
        encodeLegend(context, fieldset);

        encodeContent(context, fieldset);

        if (toggleable) {
            encodeStateHolder(context, fieldset);
        }

        writer.endElement("fieldset");
    }

    protected void encodeContent(FacesContext context, Fieldset fieldset) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, Fieldset.CONTENT_CLASS, null);
        if (fieldset.isCollapsed()) {
            writer.writeAttribute(STYLE, "display:none", null);
        }

        renderChildren(context, fieldset);

        writer.endElement(DIV);
    }

    protected void encodeScript(FacesContext context, Fieldset fieldset) throws IOException {
        String clientId = fieldset.getClientId(context);
        boolean toggleable = fieldset.isToggleable();
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("Fieldset", fieldset.resolveWidgetVar(), clientId);

        if (toggleable) {
            wb.attr("toggleable", true)
                    .attr("collapsed", fieldset.isCollapsed())
                    .attr("toggleSpeed", fieldset.getToggleSpeed());
        }

        encodeClientBehaviors(context, fieldset);

        wb.finish();
    }

    protected void encodeLegend(FacesContext context, Fieldset fieldset) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String legendText = fieldset.getLegend();
        UIComponent legend = fieldset.getFacet("legend");

        if (legendText != null || legend != null) {
            writer.startElement("legend", null);
            writer.writeAttribute(CLASS, Fieldset.LEGEND_CLASS, null);

            if (fieldset.isToggleable()) {
                writer.writeAttribute(ROLE, BUTTON, null);
                writer.writeAttribute(TABINDEX, fieldset.getTabindex(), null);

                String togglerClass = fieldset.isCollapsed() ? Fieldset.TOGGLER_PLUS_CLASS : Fieldset.TOGGLER_MINUS_CLASS;

                writer.startElement(SPAN, null);
                writer.writeAttribute(CLASS, togglerClass, null);
                writer.endElement(SPAN);
            }

            if (legend != null) {
                legend.encodeAll(context);
            }
            else {
                if (fieldset.isEscape()) {
                    writer.writeText(legendText, VALUE);
                }
                else {
                    writer.write(legendText);
                }
            }

            writer.endElement("legend");
        }
    }

    protected void encodeStateHolder(FacesContext context, Fieldset fieldset) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String name = fieldset.getClientId(context) + "_collapsed";

        writer.startElement(INPUT, null);
        writer.writeAttribute(TYPE, "hidden", null);
        writer.writeAttribute("id", name, null);
        writer.writeAttribute(NAME, name, null);
        writer.writeAttribute(VALUE, String.valueOf(fieldset.isCollapsed()), null);
        writer.endElement(INPUT);
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
