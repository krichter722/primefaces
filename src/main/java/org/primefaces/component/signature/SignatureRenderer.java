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
package org.primefaces.component.signature;

import java.io.IOException;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.WidgetBuilder;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.INPUT;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.READONLY;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TYPE;
import static org.primefaces.component.Literals.VALUE;

public class SignatureRenderer extends CoreRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        Signature signature = (Signature) component;
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String value = params.get(signature.getClientId(context) + "_value");
        String base64Value = params.get(signature.getClientId(context) + "_base64");
        signature.setSubmittedValue(value);

        if (base64Value != null) {
            signature.setBase64Value(base64Value);
        }
    }

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException {
        Signature signature = (Signature) component;

        encodeMarkup(facesContext, signature);
        encodeScript(facesContext, signature);
    }

    protected void encodeMarkup(FacesContext context, Signature signature) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = signature.getClientId(context);
        String style = signature.getStyle();
        String styleClass = signature.getStyleClass();
        String defaultStyle = signature.resolveStyleClass();
        styleClass = styleClass == null ? defaultStyle : defaultStyle + " " + styleClass;

        writer.startElement(DIV, null);
        writer.writeAttribute("id", clientId, null);
        if (style != null) writer.writeAttribute(STYLE, style, null);
        if (styleClass != null) writer.writeAttribute(CLASS, styleClass, null);

        encodeInputField(context, signature, clientId + "_value", signature.getValue());

        if (signature.getValueExpression(Signature.PropertyKeys.base64Value.toString()) != null) {
            encodeInputField(context, signature, clientId + "_base64", null);
        }

        writer.endElement(DIV);
    }

    protected void encodeScript(FacesContext context, Signature signature) throws IOException {
        String clientId = signature.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("Signature", signature.resolveWidgetVar(), clientId)
                .attr("background", signature.getBackgroundColor(), null)
                .attr("color", signature.getColor(), null)
                .attr("thickness", signature.getThickness(), 2)
                .attr(READONLY, signature.isReadonly(), false)
                .attr("guideline", signature.isGuideline(), false)
                .attr("guidelineColor", signature.getGuidelineColor(), null)
                .attr("guidelineOffset", signature.getGuidelineOffset(), 25)
                .attr("guidelineIndent", signature.getGuidelineIndent(), 10)
                .callback("onchange", "function()", signature.getOnchange());

        if (signature.getValueExpression(Signature.PropertyKeys.base64Value.toString()) != null) {
            wb.attr("base64", true);
        }

        wb.finish();
    }

    protected void encodeInputField(FacesContext context, Signature signature, String name, Object value) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement(INPUT, null);
        writer.writeAttribute(TYPE, "hidden", null);
        writer.writeAttribute("id", name, null);
        writer.writeAttribute(NAME, name, null);
        writer.writeAttribute("autocomplete", "off", null);
        if (value != null) {
            writer.writeAttribute(VALUE, value, null);
        }
        writer.endElement(INPUT);
    }
}
