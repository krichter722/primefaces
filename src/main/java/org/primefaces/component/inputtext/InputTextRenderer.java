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
package org.primefaces.component.inputtext;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DISABLED;
import static org.primefaces.component.Literals.INPUT;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.READONLY;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TYPE;
import static org.primefaces.component.Literals.VALUE;
import org.primefaces.context.PrimeApplicationContext;
import org.primefaces.renderkit.InputRenderer;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.WidgetBuilder;

public class InputTextRenderer extends InputRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        InputText inputText = (InputText) component;

        if (inputText.isDisabled() || inputText.isReadonly()) {
            return;
        }

        decodeBehaviors(context, inputText);

        String clientId = inputText.getClientId(context);
        String submittedValue = (String) context.getExternalContext().getRequestParameterMap().get(clientId);

        if (submittedValue != null) {
            inputText.setSubmittedValue(submittedValue);
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        InputText inputText = (InputText) component;

        encodeMarkup(context, inputText);
        encodeScript(context, inputText);
    }

    protected void encodeScript(FacesContext context, InputText inputText) throws IOException {
        String clientId = inputText.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("InputText", inputText.resolveWidgetVar(), clientId).finish();
    }

    protected void encodeMarkup(FacesContext context, InputText inputText) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = inputText.getClientId(context);

        writer.startElement(INPUT, inputText);
        writer.writeAttribute("id", clientId, null);
        writer.writeAttribute(NAME, clientId, null);
        writer.writeAttribute(TYPE, inputText.getType(), null);

        String valueToRender = ComponentUtils.getValueToRender(context, inputText);
        if (valueToRender != null) {
            writer.writeAttribute(VALUE, valueToRender, null);
        }

        renderRTLDirection(context, inputText);
        renderPassThruAttributes(context, inputText, HTML.INPUT_TEXT_ATTRS_WITHOUT_EVENTS);
        renderDomEvents(context, inputText, HTML.INPUT_TEXT_EVENTS);

        if (inputText.isDisabled()) writer.writeAttribute(DISABLED, DISABLED, null);
        if (inputText.isReadonly()) writer.writeAttribute(READONLY, READONLY, null);
        if (inputText.getStyle() != null) writer.writeAttribute(STYLE, inputText.getStyle(), null);
        if (inputText.isRequired()) writer.writeAttribute("aria-required", "true", null);

        writer.writeAttribute(CLASS, createStyleClass(inputText), "styleClass");

        if (PrimeApplicationContext.getCurrentInstance(context).getConfig().isClientSideValidationEnabled()) {
            renderValidationMetadata(context, inputText);
        }

        writer.endElement(INPUT);
    }

    protected String createStyleClass(InputText inputText) {
        String defaultClass = InputText.STYLE_CLASS;
        defaultClass = inputText.isValid() ? defaultClass : defaultClass + " ui-state-error";
        defaultClass = !inputText.isDisabled() ? defaultClass : defaultClass + " ui-state-disabled";

        String styleClass = inputText.getStyleClass();
        styleClass = styleClass == null ? defaultClass : defaultClass + " " + styleClass;

        return styleClass;
    }
}
