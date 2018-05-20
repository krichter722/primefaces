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
package org.primefaces.component.spacer;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.primefaces.renderkit.CoreRenderer;
import static org.primefaces.component.Literals.ALT;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.IMG;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TITLE;

public class SpacerRenderer extends CoreRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Spacer spacer = (Spacer) component;
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement(IMG, spacer);
        writer.writeAttribute("id", spacer.getClientId(context), "id");
        writer.writeAttribute("width", spacer.getWidth(), "width");
        writer.writeAttribute("height", spacer.getHeight(), "height");
        writer.writeAttribute(ALT, "", null);
        writer.writeAttribute("src", getResourceRequestPath(context, "spacer/dot_clear.gif"), null);

        if (spacer.getStyle() != null) writer.writeAttribute(STYLE, spacer.getStyle(), STYLE);
        if (spacer.getStyleClass() != null) writer.writeAttribute(CLASS, spacer.getStyleClass(), "styleClass");
        if (spacer.getTitle() != null) writer.writeAttribute(TITLE, spacer.getTitle(), TITLE);

        writer.endElement(IMG);
    }
}
