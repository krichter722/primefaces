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
package org.primefaces.component.slidemenu;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.ROLE;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import org.primefaces.component.menu.AbstractMenu;
import org.primefaces.component.menu.Menu;
import org.primefaces.component.tieredmenu.TieredMenuRenderer;
import org.primefaces.util.WidgetBuilder;

public class SlideMenuRenderer extends TieredMenuRenderer {

    @Override
    protected void encodeScript(FacesContext context, AbstractMenu abstractMenu) throws IOException {
        SlideMenu menu = (SlideMenu) abstractMenu;
        String clientId = menu.getClientId(context);

        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("SlideMenu", menu.resolveWidgetVar(), clientId);

        if (menu.isOverlay()) {
            encodeOverlayConfig(context, menu, wb);
        }

        wb.finish();
    }

    @Override
    protected void encodeMarkup(FacesContext context, AbstractMenu abstractMenu) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        SlideMenu menu = (SlideMenu) abstractMenu;

        String style = menu.getStyle();
        String styleClass = menu.getStyleClass();
        String defaultStyleClass = menu.isOverlay() ? SlideMenu.DYNAMIC_CONTAINER_CLASS : SlideMenu.STATIC_CONTAINER_CLASS;
        styleClass = styleClass == null ? defaultStyleClass : defaultStyleClass + " " + styleClass;

        writer.startElement(DIV, menu);
        writer.writeAttribute("id", menu.getClientId(context), "id");
        writer.writeAttribute(CLASS, styleClass, "styleClass");
        if (style != null) {
            writer.writeAttribute(STYLE, style, STYLE);
        }
        writer.writeAttribute(ROLE, "menu", null);

        //wrapper
        writer.startElement(DIV, menu);
        writer.writeAttribute(CLASS, SlideMenu.WRAPPER_CLASS, "styleClass");

        //content
        writer.startElement(DIV, menu);
        writer.writeAttribute(CLASS, SlideMenu.CONTENT_CLASS, "styleClass");

        //root menu
        if (menu.getElementsCount() > 0) {
            writer.startElement("ul", null);
            writer.writeAttribute(CLASS, Menu.LIST_CLASS, null);
            encodeElements(context, abstractMenu, menu.getElements());
            writer.endElement("ul");
        }

        //content
        writer.endElement(DIV);

        //back navigator
        writer.startElement(DIV, menu);
        writer.writeAttribute(CLASS, SlideMenu.BACKWARD_CLASS, null);
        writer.startElement(SPAN, menu);
        writer.writeAttribute(CLASS, SlideMenu.BACKWARD_ICON_CLASS, null);
        writer.endElement(SPAN);
        writer.writeText(menu.getBackLabel(), "backLabel");
        writer.endElement(DIV);

        //wrapper
        writer.endElement(DIV);

        writer.endElement(DIV);
    }
}
