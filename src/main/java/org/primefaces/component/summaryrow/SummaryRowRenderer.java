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
package org.primefaces.component.summaryrow;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.primefaces.component.column.Column;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.renderkit.CoreRenderer;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.COLSPAN;
import static org.primefaces.component.Literals.STYLE;

public class SummaryRowRenderer extends CoreRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        SummaryRow row = (SummaryRow) component;
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement("tr", null);
        writer.writeAttribute(CLASS, DataTable.SUMMARY_ROW_CLASS, null);

        for (UIComponent kid : row.getChildren()) {
            if (kid.isRendered() && kid instanceof Column) {
                Column column = (Column) kid;
                String style = column.getStyle();
                String styleClass = column.getStyleClass();

                writer.startElement("td", null);
                if (style != null) writer.writeAttribute(STYLE, style, null);
                if (styleClass != null) writer.writeAttribute(CLASS, styleClass, null);
                if (column.getRowspan() != 1) writer.writeAttribute("rowspan", column.getRowspan(), null);
                if (column.getColspan() != 1) writer.writeAttribute(COLSPAN, column.getColspan(), null);

                column.encodeAll(context);

                writer.endElement("td");
            }
        }

        writer.endElement("tr");
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
