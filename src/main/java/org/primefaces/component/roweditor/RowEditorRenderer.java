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
package org.primefaces.component.roweditor;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.renderkit.CoreRenderer;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TITLE;

public class RowEditorRenderer extends CoreRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        RowEditor rowEditor = (RowEditor) component;
        String style = rowEditor.getStyle();
        String styleClass = rowEditor.getStyleClass();
        styleClass = (styleClass == null) ? DataTable.ROW_EDITOR_CLASS : DataTable.ROW_EDITOR_CLASS + " " + styleClass;

        writer.startElement(DIV, null);
        writer.writeAttribute("id", component.getClientId(context), null);
        writer.writeAttribute(CLASS, styleClass, null);
        if (style != null) {
            writer.writeAttribute(STYLE, style, null);
        }

        encodeIcon(writer, "pencil", rowEditor.getEditTitle());
        encodeIcon(writer, "check", rowEditor.getSaveTitle());
        encodeIcon(writer, "close", rowEditor.getCancelTitle());

        writer.endElement(DIV);
    }

    protected void encodeIcon(ResponseWriter writer, String type, String title) throws IOException {
        String iconClass = "ui-icon ui-icon-" + type;
        iconClass = ("pencil".equals(type)) ? iconClass : iconClass + " ui-c";

        writer.startElement("a", null);
        writer.writeAttribute("href", "#", null);
        writer.writeAttribute(CLASS, "ui-row-editor-" + type, null);

        writer.startElement(SPAN, null);
        if (title != null) {
            writer.writeAttribute(TITLE, title, null);
        }
        writer.writeAttribute(CLASS, iconClass, null);
        writer.endElement(SPAN);

        writer.endElement("a");
    }
}
