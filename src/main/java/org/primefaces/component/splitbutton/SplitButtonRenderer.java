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
package org.primefaces.component.splitbutton;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import org.primefaces.behavior.confirm.ConfirmBehavior;
import org.primefaces.component.api.AjaxSource;
import org.primefaces.component.api.UIOutcomeTarget;
import org.primefaces.component.menu.AbstractMenu;
import org.primefaces.component.menu.Menu;
import org.primefaces.component.menubutton.MenuButton;
import org.primefaces.component.menuitem.UIMenuItem;
import org.primefaces.component.separator.UISeparator;
import org.primefaces.expression.SearchExpressionFacade;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.renderkit.OutcomeTargetRenderer;
import org.primefaces.util.ComponentTraversalUtils;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.SharedStringBuilder;
import org.primefaces.util.WidgetBuilder;
import static org.primefaces.component.Literals.BUTTON;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DISABLED;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.MENUITEM;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.ONCLICK;
import static org.primefaces.component.Literals.ROLE;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TITLE;
import static org.primefaces.component.Literals.TYPE;
import static org.primefaces.component.Literals.VALUE;

public class SplitButtonRenderer extends OutcomeTargetRenderer {

    private static final String SB_BUILD_ONCLICK = SplitButtonRenderer.class.getName() + "#buildOnclick";

    @Override
    public void decode(FacesContext context, UIComponent component) {
        SplitButton button = (SplitButton) component;
        if (button.isDisabled()) {
            return;
        }

        String clientId = button.getClientId(context);

        String param = button.isAjax() ? clientId : clientId + "_button";
        if (context.getExternalContext().getRequestParameterMap().containsKey(param)) {
            component.queueEvent(new ActionEvent(component));
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        SplitButton button = (SplitButton) component;

        encodeMarkup(context, button);
        encodeScript(context, button);
    }

    protected void encodeMarkup(FacesContext context, SplitButton button) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = button.getClientId(context);
        String menuId = clientId + "_menu";
        String menuButtonId = clientId + "_menuButton";
        String buttonId = clientId + "_button";
        String styleClass = button.getStyleClass();
        styleClass = styleClass == null ? SplitButton.STYLE_CLASS : SplitButton.STYLE_CLASS + " " + styleClass;

        writer.startElement(DIV, button);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(CLASS, styleClass, "id");
        if (button.getStyle() != null) {
            writer.writeAttribute(STYLE, button.getStyle(), "id");
        }

        encodeDefaultButton(context, button, buttonId);
        if (ComponentUtils.shouldRenderChildren(button)) {
            encodeMenuIcon(context, button, menuButtonId);
            encodeMenu(context, button, menuId);
        }

        writer.endElement(DIV);
    }

    protected void encodeDefaultButton(FacesContext context, SplitButton button, String id) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String value = (String) button.getValue();
        String icon = button.getIcon();
        String onclick = buildOnclick(context, button);

        writer.startElement(BUTTON, button);
        writer.writeAttribute("id", id, "id");
        writer.writeAttribute(NAME, id, NAME);
        writer.writeAttribute(CLASS, button.resolveStyleClass(), "styleClass");

        if (onclick.length() > 0) {
            writer.writeAttribute(ONCLICK, onclick, ONCLICK);
        }

        renderPassThruAttributes(context, button, HTML.BUTTON_ATTRS, HTML.CLICK_EVENT);

        if (button.isDisabled()) {
            writer.writeAttribute(DISABLED, DISABLED, DISABLED);
        }

        //icon
        if (!isValueBlank(icon)) {
            String defaultIconClass = button.getIconPos().equals("left") ? HTML.BUTTON_LEFT_ICON_CLASS : HTML.BUTTON_RIGHT_ICON_CLASS;
            String iconClass = defaultIconClass + " " + icon;

            writer.startElement(SPAN, null);
            writer.writeAttribute(CLASS, iconClass, null);
            writer.endElement(SPAN);
        }

        //text
        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, HTML.BUTTON_TEXT_CLASS, null);

        if (value == null) {
            writer.write("ui-button");
        }
        else {
            writer.writeText(value, VALUE);
        }

        writer.endElement(SPAN);

        writer.endElement(BUTTON);
    }

    protected void encodeMenuIcon(FacesContext context, SplitButton button, String id) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String buttonClass = SplitButton.MENU_ICON_BUTTON_CLASS;
        if (button.isDisabled()) {
            buttonClass += " ui-state-disabled";
        }

        writer.startElement(BUTTON, button);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute(NAME, id, null);
        writer.writeAttribute(TYPE, BUTTON, null);
        writer.writeAttribute(CLASS, buttonClass, null);

        if (button.isDisabled()) {
            writer.writeAttribute(DISABLED, DISABLED, DISABLED);
        }

        //icon    
        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, "ui-button-icon-left ui-icon ui-icon-triangle-1-s", null);
        writer.endElement(SPAN);

        //text
        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, HTML.BUTTON_TEXT_CLASS, null);
        writer.write("ui-button");
        writer.endElement(SPAN);

        writer.endElement(BUTTON);
    }

    protected void encodeScript(FacesContext context, SplitButton button) throws IOException {
        String clientId = button.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("SplitButton", button.resolveWidgetVar(), clientId);
        wb.attr("appendTo", SearchExpressionFacade.resolveClientId(context, button, button.getAppendTo()), null);
        wb.finish();
    }

    protected String buildOnclick(FacesContext context, SplitButton button) throws IOException {
        StringBuilder onclick = SharedStringBuilder.get(context, SB_BUILD_ONCLICK);
        if (button.getOnclick() != null) {
            onclick.append(button.getOnclick()).append(';');
        }

        if (button.isAjax()) {
            onclick.append(buildAjaxRequest(context, button, null));
        }
        else {
            UIComponent form = ComponentTraversalUtils.closestForm(context, button);
            if (form == null) {
                throw new FacesException("SplitButton : \"" + button.getClientId(context) + "\" must be inside a form element");
            }

            onclick.append(buildNonAjaxRequest(context, button, form, null, false));
        }

        String onclickBehaviors = getEventBehaviors(context, button, "click", null);
        if (onclickBehaviors != null) {
            onclick.append(onclickBehaviors).append(';');
        }

        return onclick.toString();
    }

    protected void encodeMenu(FacesContext context, SplitButton button, String menuId) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String menuStyleClass = button.getMenuStyleClass();
        menuStyleClass = (menuStyleClass == null) ? SplitButton.SPLITBUTTON_CONTAINER_CLASS : SplitButton.SPLITBUTTON_CONTAINER_CLASS + " " + menuStyleClass;

        writer.startElement(DIV, null);
        writer.writeAttribute("id", menuId, null);
        writer.writeAttribute(CLASS, menuStyleClass, "styleClass");
        writer.writeAttribute(ROLE, "menu", null);

        writer.startElement("ul", null);
        writer.writeAttribute(CLASS, MenuButton.LIST_CLASS, "styleClass");

        for (int i = 0; i < button.getChildCount(); i++) {
            UIComponent child = button.getChildren().get(i);
            if (child.isRendered()) {
                if (child instanceof UIMenuItem) {
                    UIMenuItem item = (UIMenuItem) child;

                    writer.startElement("li", null);
                    writer.writeAttribute(CLASS, Menu.MENUITEM_CLASS, null);
                    writer.writeAttribute(ROLE, MENUITEM, null);
                    encodeMenuItem(context, item);
                    writer.endElement("li");
                }
                else if (child instanceof UISeparator) {
                    encodeSeparator(context, (UISeparator) child);
                }
            }
        }

        writer.endElement("ul");
        writer.endElement(DIV);

    }

    protected void encodeMenuItem(FacesContext context, UIMenuItem menuitem) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String icon = menuitem.getIcon();
        String title = menuitem.getTitle();

        if (menuitem.shouldRenderChildren()) {
            renderChildren(context, (UIComponent) menuitem);
        }
        else {
            boolean disabled = menuitem.isDisabled();

            writer.startElement("a", null);
            writer.writeAttribute("id", menuitem.getClientId(), null);
            if (title != null) {
                writer.writeAttribute(TITLE, title, null);
            }

            String styleClass = menuitem.getStyleClass();
            styleClass = styleClass == null ? AbstractMenu.MENUITEM_LINK_CLASS : AbstractMenu.MENUITEM_LINK_CLASS + " " + styleClass;
            styleClass = disabled ? styleClass + " ui-state-disabled" : styleClass;

            writer.writeAttribute(CLASS, styleClass, null);

            if (menuitem.getStyle() != null) {
                writer.writeAttribute(STYLE, menuitem.getStyle(), null);
            }

            if (disabled) {
                writer.writeAttribute("href", "#", null);
                writer.writeAttribute(ONCLICK, "return false;", null);
            }
            else {
                setConfirmationScript(context, menuitem);
                String onclick = menuitem.getOnclick();

                //GET
                if (menuitem.getUrl() != null || menuitem.getOutcome() != null) {
                    String targetURL = getTargetURL(context, (UIOutcomeTarget) menuitem);
                    writer.writeAttribute("href", targetURL, null);

                    if (menuitem.getTarget() != null) {
                        writer.writeAttribute("target", menuitem.getTarget(), null);
                    }
                }
                //POST
                else {
                    writer.writeAttribute("href", "#", null);

                    UIComponent form = ComponentTraversalUtils.closestForm(context, menuitem);
                    if (form == null) {
                        throw new FacesException("MenuItem must be inside a form element");
                    }

                    String command = menuitem.isAjax()
                            ? buildAjaxRequest(context, (AjaxSource) menuitem, form)
                            : buildNonAjaxRequest(context, ((UIComponent) menuitem), form, ((UIComponent) menuitem).getClientId(context), true);

                    onclick = (onclick == null) ? command : onclick + ";" + command;
                }

                if (onclick != null) {
                    if (menuitem.requiresConfirmation()) {
                        writer.writeAttribute("data-pfconfirmcommand", onclick, null);
                        writer.writeAttribute(ONCLICK, menuitem.getConfirmationScript(), ONCLICK);
                    }
                    else {
                        writer.writeAttribute(ONCLICK, onclick, null);
                    }
                }
            }

            if (icon != null) {
                writer.startElement(SPAN, null);
                writer.writeAttribute(CLASS, AbstractMenu.MENUITEM_ICON_CLASS + " " + icon, null);
                writer.endElement(SPAN);
            }

            if (menuitem.getValue() != null) {
                writer.startElement(SPAN, null);
                writer.writeAttribute(CLASS, AbstractMenu.MENUITEM_TEXT_CLASS, null);
                writer.writeText((String) menuitem.getValue(), VALUE);
                writer.endElement(SPAN);
            }

            writer.endElement("a");
        }
    }

    protected void encodeSeparator(FacesContext context, UISeparator separator) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String style = separator.getStyle();
        String styleClass = separator.getStyleClass();
        styleClass = styleClass == null ? Menu.SEPARATOR_CLASS : Menu.SEPARATOR_CLASS + " " + styleClass;

        //title
        writer.startElement("li", null);
        writer.writeAttribute(CLASS, styleClass, null);
        if (style != null) {
            writer.writeAttribute(STYLE, style, null);
        }

        writer.endElement("li");
    }

    @Override
    public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException {
        //Do nothing
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    protected void setConfirmationScript(FacesContext context, MenuItem item) {
        if (item instanceof ClientBehaviorHolder) {
            Map<String, List<ClientBehavior>> behaviors = ((ClientBehaviorHolder) item).getClientBehaviors();
            List<ClientBehavior> clickBehaviors = (behaviors == null) ? null : behaviors.get("click");

            if (clickBehaviors != null && !clickBehaviors.isEmpty()) {
                for (int i = 0; i < clickBehaviors.size(); i++) {
                    ClientBehavior clientBehavior = clickBehaviors.get(i);
                    if (clientBehavior instanceof ConfirmBehavior) {
                        ClientBehaviorContext cbc = ClientBehaviorContext.createClientBehaviorContext(
                                context, (UIComponent) item, "click", item.getClientId(), Collections.EMPTY_LIST);
                        clientBehavior.getScript(cbc);
                        break;
                    }
                }
            }
        }
    }
}
