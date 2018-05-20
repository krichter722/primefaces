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
package org.primefaces.component.tieredmenu;

import java.io.IOException;
import java.util.List;
import javax.faces.component.UIComponent;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.MENUITEM;
import static org.primefaces.component.Literals.ROLE;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TABINDEX;
import static org.primefaces.component.Literals.VALUE;
import org.primefaces.component.menu.AbstractMenu;
import org.primefaces.component.menu.BaseMenuRenderer;
import org.primefaces.component.menu.Menu;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.model.menu.Separator;
import org.primefaces.model.menu.Submenu;
import org.primefaces.util.WidgetBuilder;

public class TieredMenuRenderer extends BaseMenuRenderer {

    @Override
    protected void encodeScript(FacesContext context, AbstractMenu abstractMenu) throws IOException {
        TieredMenu menu = (TieredMenu) abstractMenu;
        String clientId = menu.getClientId(context);

        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("TieredMenu", menu.resolveWidgetVar(), clientId)
                .attr("autoDisplay", menu.isAutoDisplay())
                .attr("toggleEvent", menu.getToggleEvent(), null);

        if (menu.isOverlay()) {
            encodeOverlayConfig(context, menu, wb);
        }

        wb.finish();
    }

    @Override
    protected void encodeMarkup(FacesContext context, AbstractMenu abstractMenu) throws IOException {
        TieredMenu menu = (TieredMenu) abstractMenu;
        String style = menu.getStyle();
        String styleClass = menu.getStyleClass();
        String defaultStyleClass = menu.isOverlay() ? TieredMenu.DYNAMIC_CONTAINER_CLASS : TieredMenu.STATIC_CONTAINER_CLASS;
        styleClass = styleClass == null ? defaultStyleClass : defaultStyleClass + " " + styleClass;

        encodeMenu(context, menu, style, styleClass, "menu");
    }

    protected void encodeMenu(FacesContext context, AbstractMenu menu, String style, String styleClass, String role) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        UIComponent optionsFacet = menu.getFacet("options");

        writer.startElement(DIV, menu);
        writer.writeAttribute("id", menu.getClientId(context), "id");
        writer.writeAttribute(CLASS, styleClass, "styleClass");
        if (style != null) {
            writer.writeAttribute(STYLE, style, STYLE);
        }
        writer.writeAttribute(ROLE, "menubar", null);

        encodeKeyboardTarget(context, menu);

        writer.startElement("ul", null);
        writer.writeAttribute(CLASS, Menu.LIST_CLASS, null);

        if (menu.getElementsCount() > 0) {
            encodeElements(context, menu, menu.getElements());
        }

        if (optionsFacet != null) {
            writer.startElement("li", null);
            writer.writeAttribute(CLASS, Menu.OPTIONS_CLASS, null);
            writer.writeAttribute(ROLE, MENUITEM, null);
            optionsFacet.encodeAll(context);
            writer.endElement("li");
        }

        writer.endElement("ul");

        writer.endElement(DIV);
    }

    protected void encodeElements(FacesContext context, AbstractMenu menu, List<MenuElement> elements) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        for (MenuElement element : elements) {
            if (element.isRendered()) {
                if (element instanceof MenuItem) {
                    MenuItem menuItem = (MenuItem) element;
                    String containerStyle = menuItem.getContainerStyle();
                    String containerStyleClass = menuItem.getContainerStyleClass();
                    containerStyleClass = (containerStyleClass == null) ? Menu.MENUITEM_CLASS : Menu.MENUITEM_CLASS + " " + containerStyleClass;

                    writer.startElement("li", null);
                    writer.writeAttribute(CLASS, containerStyleClass, null);
                    writer.writeAttribute(ROLE, MENUITEM, null);
                    if (containerStyle != null) {
                        writer.writeAttribute(STYLE, containerStyle, null);
                    }
                    encodeMenuItem(context, menu, menuItem);
                    writer.endElement("li");
                }
                else if (element instanceof Submenu) {
                    Submenu submenu = (Submenu) element;
                    String style = submenu.getStyle();
                    String styleClass = submenu.getStyleClass();
                    styleClass = styleClass == null ? Menu.TIERED_SUBMENU_CLASS : Menu.TIERED_SUBMENU_CLASS + " " + styleClass;

                    writer.startElement("li", null);
                    if (shouldRenderId(submenu)) {
                        writer.writeAttribute("id", submenu.getClientId(), null);
                    }
                    writer.writeAttribute(CLASS, styleClass, null);
                    if (style != null) {
                        writer.writeAttribute(STYLE, style, null);
                    }
                    writer.writeAttribute(ROLE, MENUITEM, null);
                    writer.writeAttribute("aria-haspopup", "true", null);
                    encodeSubmenu(context, menu, submenu);
                    writer.endElement("li");
                }
                else if (element instanceof Separator) {
                    encodeSeparator(context, (Separator) element);
                }
            }
        }
    }

    protected void encodeSubmenu(FacesContext context, AbstractMenu menu, Submenu submenu) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String icon = submenu.getIcon();
        String label = submenu.getLabel();

        //title
        writer.startElement("a", null);
        writer.writeAttribute("href", "#", null);
        writer.writeAttribute(CLASS, Menu.SUBMENU_LINK_CLASS, null);
        writer.writeAttribute(TABINDEX, "-1", null);

        if (icon != null) {
            writer.startElement(SPAN, null);
            writer.writeAttribute(CLASS, Menu.MENUITEM_ICON_CLASS + " " + icon, null);
            writer.endElement(SPAN);
        }

        if (label != null) {
            writer.startElement(SPAN, null);
            writer.writeAttribute(CLASS, Menu.MENUITEM_TEXT_CLASS, null);
            writer.writeText(submenu.getLabel(), VALUE);
            writer.endElement(SPAN);
        }

        encodeSubmenuIcon(context, submenu);

        writer.endElement("a");

        //submenus and menuitems
        if (submenu.getElementsCount() > 0) {
            writer.startElement("ul", null);
            writer.writeAttribute(CLASS, Menu.TIERED_CHILD_SUBMENU_CLASS, null);
            writer.writeAttribute(ROLE, "menu", null);
            encodeElements(context, menu, submenu.getElements());
            writer.endElement("ul");
        }
    }

    protected void encodeSubmenuIcon(FacesContext context, Submenu submenu) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, Menu.SUBMENU_RIGHT_ICON_CLASS, null);
        writer.endElement(SPAN);
    }
}
