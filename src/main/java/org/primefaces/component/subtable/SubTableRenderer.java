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
package org.primefaces.component.subtable;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.primefaces.component.column.Column;
import org.primefaces.component.columngroup.ColumnGroup;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.row.Row;
import org.primefaces.renderkit.CoreRenderer;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.COLSPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.UI_WIDGET_HEADER;

public class SubTableRenderer extends CoreRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        SubTable table = (SubTable) component;
        int rowCount = table.getRowCount();

        encodeHeader(context, table);

        for (int i = 0; i < rowCount; i++) {
            encodeRow(context, table, i);
        }

        encodeFooter(context, table);
    }

    public void encodeHeader(FacesContext context, SubTable table) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        UIComponent header = table.getFacet("header");

        if (header != null) {
            writer.startElement("tr", null);
            writer.writeAttribute(CLASS, UI_WIDGET_HEADER, null);

            writer.startElement("td", null);
            writer.writeAttribute(CLASS, DataTable.SUBTABLE_HEADER, null);
            writer.writeAttribute(COLSPAN, table.getColumns().size(), null);

            header.encodeAll(context);

            writer.endElement("td");
            writer.endElement("tr");
        }

        ColumnGroup group = table.getColumnGroup("header");
        if (group != null && group.isRendered()) {
            for (UIComponent child : group.getChildren()) {
                if (child.isRendered() && child instanceof Row) {
                    Row headerRow = (Row) child;

                    writer.startElement("tr", null);
                    writer.writeAttribute(CLASS, UI_WIDGET_HEADER, null);

                    for (UIComponent headerRowChild : headerRow.getChildren()) {
                        if (headerRowChild.isRendered() && headerRowChild instanceof Column) {
                            Column footerColumn = (Column) headerRowChild;
                            encodeFacetColumn(context, table, footerColumn, "header", DataTable.COLUMN_HEADER_CLASS, footerColumn.getHeaderText());
                        }
                    }

                    writer.endElement("tr");
                }
            }
        }
    }

    public void encodeRow(FacesContext context, SubTable table, int rowIndex) throws IOException {
        table.setRowIndex(rowIndex);
        if (!table.isRowAvailable()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        String clientId = table.getClientId(context);

        writer.startElement("tr", null);
        writer.writeAttribute("id", clientId + "_row_" + rowIndex, null);
        writer.writeAttribute(CLASS, DataTable.ROW_CLASS, null);

        for (Column column : table.getColumns()) {
            String style = column.getStyle();
            String styleClass = column.getStyleClass();

            writer.startElement("td", null);
            if (style != null) writer.writeAttribute(STYLE, style, null);
            if (styleClass != null) writer.writeAttribute(CLASS, styleClass, null);

            column.encodeAll(context);

            writer.endElement("td");
        }

        writer.endElement("tr");
    }

    public void encodeFooter(FacesContext context, SubTable table) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        UIComponent footer = table.getFacet("footer");

        if (footer != null) {
            writer.startElement("tr", null);
            writer.writeAttribute(CLASS, UI_WIDGET_HEADER, null);

            writer.startElement("td", null);
            writer.writeAttribute(CLASS, DataTable.SUBTABLE_FOOTER, null);
            writer.writeAttribute(COLSPAN, table.getColumns().size(), null);

            footer.encodeAll(context);

            writer.endElement("td");
            writer.endElement("tr");
        }

        ColumnGroup group = table.getColumnGroup("footer");

        if (group == null || !group.isRendered()) {
            return;
        }

        for (UIComponent child : group.getChildren()) {
            if (child.isRendered() && child instanceof Row) {
                Row footerRow = (Row) child;

                writer.startElement("tr", null);
                writer.writeAttribute(CLASS, UI_WIDGET_HEADER, null);

                for (UIComponent footerRowChild : footerRow.getChildren()) {
                    if (footerRowChild.isRendered() && footerRowChild instanceof Column) {
                        Column footerColumn = (Column) footerRowChild;
                        encodeFacetColumn(context, table, footerColumn, "footer", DataTable.COLUMN_FOOTER_CLASS, footerColumn.getFooterText());
                    }
                }

                writer.endElement("tr");
            }
        }
    }

    protected void encodeFacetColumn(FacesContext context, SubTable table, Column column, String facetName, String styleClass, String text) throws IOException {
        if (!column.isRendered()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        String style = column.getStyle();
        String columnClass = column.getStyleClass();
        columnClass = (columnClass == null) ? styleClass : styleClass + " " + columnClass;

        writer.startElement("td", null);
        writer.writeAttribute(CLASS, columnClass, null);
        if (column.getRowspan() != 1) writer.writeAttribute("rowspan", column.getRowspan(), null);
        if (column.getColspan() != 1) writer.writeAttribute(COLSPAN, column.getColspan(), null);
        if (style != null) writer.writeAttribute(STYLE, style, null);

        // Footer content
        UIComponent facet = column.getFacet(facetName);
        if (facet != null) {
            facet.encodeAll(context);
        }
        else if (text != null) {
            writer.write(text);
        }

        writer.endElement("td");
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        // Rendering happens on encodeEnd
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
