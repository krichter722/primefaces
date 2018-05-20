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
package org.primefaces.component.button;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import static org.primefaces.component.Literals.BUTTON;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DISABLED;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.ONCLICK;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.TYPE;
import static org.primefaces.component.Literals.VALUE;
import org.primefaces.renderkit.OutcomeTargetRenderer;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.SharedStringBuilder;
import org.primefaces.util.WidgetBuilder;

public class ButtonRenderer extends OutcomeTargetRenderer {

    private static final String SB_BUILD_ONCLICK = ButtonRenderer.class.getName() + "#buildOnclick";

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Button button = (Button) component;

        encodeMarkup(context, button);
        encodeScript(context, button);
    }

    public void encodeMarkup(FacesContext context, Button button) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = button.getClientId(context);
        String value = (String) button.getValue();
        String icon = button.resolveIcon();

        writer.startElement(BUTTON, button);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(NAME, clientId, NAME);
        writer.writeAttribute(TYPE, BUTTON, null);
        writer.writeAttribute(CLASS, button.resolveStyleClass(), "styleClass");

        renderPassThruAttributes(context, button, HTML.BUTTON_ATTRS, HTML.CLICK_EVENT);

        if (button.isDisabled()) {
            writer.writeAttribute(DISABLED, DISABLED, DISABLED);
        }

        writer.writeAttribute(ONCLICK, buildOnclick(context, button), null);

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
            if (button.isEscape()) {
                writer.writeText(value, VALUE);
            }
            else {
                writer.write(value);
            }
        }

        writer.endElement(SPAN);

        writer.endElement(BUTTON);
    }

    public void encodeScript(FacesContext context, Button button) throws IOException {
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init(BUTTON, button.resolveWidgetVar(), button.getClientId(context));
        wb.finish();
    }

    protected String buildOnclick(FacesContext context, Button button) {
        String userOnclick = button.getOnclick();
        StringBuilder onclick = SharedStringBuilder.get(context, SB_BUILD_ONCLICK);
        String targetURL = getTargetURL(context, button);

        if (userOnclick != null) {
            onclick.append(userOnclick).append(';');
        }

        String onclickBehaviors = getEventBehaviors(context, button, "click", null);
        if (onclickBehaviors != null) {
            onclick.append(onclickBehaviors).append(';');
        }

        if (targetURL != null) {
            onclick.append("window.open('").append(ComponentUtils.escapeText(targetURL)).append("','");
            onclick.append(ComponentUtils.escapeText(button.getTarget())).append("')");
        }

        return onclick.toString();
    }

}
