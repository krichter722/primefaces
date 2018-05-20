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
package org.primefaces.component.password;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.primefaces.context.PrimeApplicationContext;

import org.primefaces.renderkit.InputRenderer;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.WidgetBuilder;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DISABLED;
import static org.primefaces.component.Literals.INPUT;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.READONLY;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TYPE;
import static org.primefaces.component.Literals.VALUE;

public class PasswordRenderer extends InputRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        Password password = (Password) component;

        if (password.isDisabled() || password.isReadonly()) {
            return;
        }

        decodeBehaviors(context, password);

        String submittedValue = context.getExternalContext().getRequestParameterMap().get(password.getClientId(context));

        if (submittedValue != null) {
            password.setSubmittedValue(submittedValue);
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Password password = (Password) component;

        encodeMarkup(context, password);
        encodeScript(context, password);
    }

    protected void encodeScript(FacesContext context, Password password) throws IOException {
        String clientId = password.getClientId(context);
        boolean feedback = password.isFeedback();
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("Password", password.resolveWidgetVar(), clientId);

        if (feedback) {
            wb.attr("feedback", true)
                    .attr("inline", password.isInline())
                    .attr("promptLabel", escapeText(password.getPromptLabel()))
                    .attr("weakLabel", escapeText(password.getWeakLabel()))
                    .attr("goodLabel", escapeText(password.getGoodLabel()))
                    .attr("strongLabel", escapeText(password.getStrongLabel()));
        }

        wb.finish();
    }

    protected void encodeMarkup(FacesContext context, Password password) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = password.getClientId(context);
        boolean disabled = password.isDisabled();

        String inputClass = Password.STYLE_CLASS;
        inputClass = password.isValid() ? inputClass : inputClass + " ui-state-error";
        inputClass = !disabled ? inputClass : inputClass + " ui-state-disabled";
        String styleClass = password.getStyleClass() == null ? inputClass : inputClass + " " + password.getStyleClass();

        writer.startElement(INPUT, password);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(NAME, clientId, null);
        writer.writeAttribute(TYPE, "password", null);
        writer.writeAttribute(CLASS, styleClass, null);
        if (password.getStyle() != null) {
            writer.writeAttribute(STYLE, password.getStyle(), null);
        }

        String valueToRender = ComponentUtils.getValueToRender(context, password);
        if (!ComponentUtils.isValueBlank(valueToRender) && password.isRedisplay()) {
            writer.writeAttribute(VALUE, valueToRender, null);
        }

        renderPassThruAttributes(context, password, HTML.INPUT_TEXT_ATTRS_WITHOUT_EVENTS);
        renderDomEvents(context, password, HTML.INPUT_TEXT_EVENTS);

        if (disabled) writer.writeAttribute(DISABLED, DISABLED, null);
        if (password.isReadonly()) writer.writeAttribute(READONLY, READONLY, null);
        if (password.isRequired()) writer.writeAttribute("aria-required", "true", null);

        if (PrimeApplicationContext.getCurrentInstance(context).getConfig().isClientSideValidationEnabled()) {
            renderValidationMetadata(context, password);
        }

        writer.endElement(INPUT);
    }
}
