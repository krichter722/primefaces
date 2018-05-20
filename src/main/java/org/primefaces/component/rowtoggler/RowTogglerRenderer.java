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
package org.primefaces.component.rowtoggler;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import static org.primefaces.component.Literals.BUTTON;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.MessageFactory;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.ROLE;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.TABINDEX;

public class RowTogglerRenderer extends CoreRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        RowToggler toggler = (RowToggler) component;
        DataTable parentTable = toggler.getParentTable(context);
        boolean expanded = parentTable.isExpandedRow();
        String icon = expanded ? RowToggler.EXPANDED_ICON : RowToggler.COLLAPSED_ICON;
        String expandLabel = toggler.getExpandLabel();
        String collapseLabel = toggler.getCollapseLabel();
        boolean iconOnly = (expandLabel == null && collapseLabel == null);
        String togglerClass = iconOnly ? DataTable.ROW_TOGGLER_CLASS + " " + icon : DataTable.ROW_TOGGLER_CLASS;
        String ariaLabel = MessageFactory.getMessage(RowToggler.ROW_TOGGLER, null);

        writer.startElement(DIV, toggler);
        writer.writeAttribute(CLASS, togglerClass, null);
        writer.writeAttribute(TABINDEX, toggler.getTabindex(), null);
        writer.writeAttribute(ROLE, BUTTON, null);
        writer.writeAttribute("aria-expanded", String.valueOf(expanded), null);
        writer.writeAttribute("aria-label", ariaLabel, null);

        if (!iconOnly) {
            writeLabel(writer, expandLabel, !expanded);
            writeLabel(writer, collapseLabel, expanded);
        }

        writer.endElement(DIV);
    }

    protected void writeLabel(ResponseWriter writer, String label, boolean visible) throws IOException {
        writer.startElement(SPAN, null);
        if (!visible) {
            writer.writeAttribute(CLASS, "ui-helper-hidden", null);
        }
        writer.writeText(label, null);
        writer.endElement(SPAN);
    }
}
