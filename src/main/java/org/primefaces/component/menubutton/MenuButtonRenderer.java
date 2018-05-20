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
package org.primefaces.component.menubutton;

import java.io.IOException;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import static org.primefaces.component.Literals.BUTTON;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DISABLED;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.MENUITEM;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.ROLE;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TITLE;
import static org.primefaces.component.Literals.TYPE;
import static org.primefaces.component.Literals.VALUE;
import org.primefaces.component.menu.AbstractMenu;
import org.primefaces.component.menu.BaseMenuRenderer;
import org.primefaces.component.menu.Menu;
import org.primefaces.expression.SearchExpressionFacade;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.model.menu.Separator;
import org.primefaces.util.ComponentTraversalUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.WidgetBuilder;

public class MenuButtonRenderer extends BaseMenuRenderer {

    @Override
    protected void encodeMarkup(FacesContext context, AbstractMenu abstractMenu) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        MenuButton button = (MenuButton) abstractMenu;
        String clientId = button.getClientId(context);
        String styleClass = button.getStyleClass();
        styleClass = styleClass == null ? MenuButton.CONTAINER_CLASS : MenuButton.CONTAINER_CLASS + " " + styleClass;
        boolean disabled = button.isDisabled();

        writer.startElement(SPAN, button);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(CLASS, styleClass, CLASS);
        
        if (button.getStyle() != null) {
            writer.writeAttribute(STYLE, button.getStyle(), STYLE);
        }
        if (button.getTitle() != null) {
            writer.writeAttribute(TITLE, button.getTitle(), TITLE);
        }
        encodeButton(context, button, clientId + "_button", disabled);
        if (!disabled) {
            encodeMenu(context, button, clientId + "_menu");
        }

        writer.endElement(SPAN);
    }

    protected void encodeButton(FacesContext context, MenuButton button, String buttonId, boolean disabled) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        boolean isIconLeft = button.getIconPos().equals("left");
        String value = button.getValue();
        String buttonTextClass = isIconLeft ? HTML.BUTTON_TEXT_ICON_LEFT_BUTTON_CLASS : HTML.BUTTON_TEXT_ICON_RIGHT_BUTTON_CLASS;
        if (isValueBlank(value)) {
            buttonTextClass = HTML.BUTTON_ICON_ONLY_BUTTON_CLASS;
        }
        String buttonClass = disabled ? buttonTextClass + " ui-state-disabled" : buttonTextClass;

        writer.startElement(BUTTON, null);
        writer.writeAttribute("id", buttonId, null);
        writer.writeAttribute(NAME, buttonId, null);
        writer.writeAttribute(TYPE, BUTTON, null);
        writer.writeAttribute(CLASS, buttonClass, null);
        writer.writeAttribute("aria-label", button.getAriaLabel(), "ariaLabel");
        if (button.isDisabled()) {
            writer.writeAttribute(DISABLED, DISABLED, null);
        }

        // button icon
        String iconClass = isValueBlank(button.getIcon()) ? MenuButton.ICON_CLASS : button.getIcon();

        //button icon pos
        String iconPosClass = isIconLeft ? HTML.BUTTON_LEFT_ICON_CLASS : HTML.BUTTON_RIGHT_ICON_CLASS;
        iconClass = iconPosClass + " " + iconClass;

        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, iconClass, null);
        writer.endElement(SPAN);

        //text
        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, HTML.BUTTON_TEXT_CLASS, null);

        if (isValueBlank(value)) {
            writer.write("ui-button");
        }
        else {
            writer.writeText(value, VALUE);
        }

        writer.endElement(SPAN);

        writer.endElement(BUTTON);
    }

    protected void encodeMenu(FacesContext context, MenuButton button, String menuId) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String menuStyleClass = button.getMenuStyleClass();
        menuStyleClass = (menuStyleClass == null) ? Menu.DYNAMIC_CONTAINER_CLASS : Menu.DYNAMIC_CONTAINER_CLASS + " " + menuStyleClass;

        writer.startElement(DIV, null);
        writer.writeAttribute("id", menuId, null);
        writer.writeAttribute(CLASS, menuStyleClass, "styleClass");
        writer.writeAttribute(ROLE, "menu", null);

        writer.startElement("ul", null);
        writer.writeAttribute(CLASS, MenuButton.LIST_CLASS, "styleClass");

        if (button.getElementsCount() > 0) {
            List<MenuElement> elements = (List<MenuElement>) button.getElements();

            for (MenuElement element : elements) {
                if (element.isRendered()) {
                    if (element instanceof MenuItem) {
                        writer.startElement("li", null);
                        writer.writeAttribute(CLASS, Menu.MENUITEM_CLASS, null);
                        writer.writeAttribute(ROLE, MENUITEM, null);
                        encodeMenuItem(context, button, (MenuItem) element);
                        writer.endElement("li");
                    }
                    else if (element instanceof Separator) {
                        encodeSeparator(context, (Separator) element);
                    }
                }
            }
        }

        writer.endElement("ul");
        writer.endElement(DIV);

    }

    @Override
    protected void encodeScript(FacesContext context, AbstractMenu abstractMenu) throws IOException {
        MenuButton button = (MenuButton) abstractMenu;
        String clientId = button.getClientId(context);

        UIComponent form = ComponentTraversalUtils.closestForm(context, button);
        if (form == null) {
            throw new FacesException("MenuButton : \"" + clientId + "\" must be inside a form element");
        }

        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("MenuButton", button.resolveWidgetVar(), clientId);
        wb.attr("appendTo", SearchExpressionFacade.resolveClientId(context, button, button.getAppendTo()), null);
        wb.finish();
    }
}
