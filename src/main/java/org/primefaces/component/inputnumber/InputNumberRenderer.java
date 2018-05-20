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
package org.primefaces.component.inputnumber;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DISABLED;
import static org.primefaces.component.Literals.INPUT;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.READONLY;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TYPE;
import static org.primefaces.component.Literals.VALUE;

import org.primefaces.component.inputtext.InputText;
import org.primefaces.context.PrimeApplicationContext;
import org.primefaces.renderkit.InputRenderer;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.WidgetBuilder;

public class InputNumberRenderer extends InputRenderer {

    private static final String LITERAL0 = "\",";

    @Override
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue)
            throws ConverterException {

        String submittedValueString = (String) submittedValue;

        if (ComponentUtils.isValueBlank(submittedValueString)) {
            return null;
        }

        Converter converter = ComponentUtils.getConverter(context, component);
        if (converter != null) {
            return converter.getAsObject(context, component, submittedValueString);
        }

        return submittedValue;
    }

    @Override
    public void decode(FacesContext context, UIComponent component) {
        InputNumber inputNumber = (InputNumber) component;

        if (inputNumber.isDisabled() || inputNumber.isReadonly()) {
            return;
        }

        decodeBehaviors(context, inputNumber);

        String inputId = inputNumber.getClientId(context) + "_hinput";
        String submittedValue = context.getExternalContext().getRequestParameterMap().get(inputId);

        try {
            if (ComponentUtils.isValueBlank(submittedValue)) {
                ValueExpression valueExpression = inputNumber.getValueExpression(VALUE);
                if (valueExpression != null) {
                    Class<?> type = valueExpression.getType(context.getELContext());
                    if (type != null && type.isPrimitive() && !ComponentUtils.isValueBlank(inputNumber.getMinValue())) {
                        // avoid coercion of null or empty string to 0 which may be out of [minValue, maxValue] range
                        submittedValue = String.valueOf(new BigDecimal(inputNumber.getMinValue()).doubleValue());
                    }
                    else if (type != null && type.isPrimitive() && !ComponentUtils.isValueBlank(inputNumber.getMaxValue())) {
                        // avoid coercion of null or empty string to 0 which may be out of [minValue, maxValue] range
                        submittedValue = String.valueOf(new BigDecimal(inputNumber.getMaxValue()).doubleValue());
                    }
                    else {
                        submittedValue = "";
                    }
                }
            }
            else {
                BigDecimal value = new BigDecimal(submittedValue);
                if (!ComponentUtils.isValueBlank(inputNumber.getMinValue())) {
                    BigDecimal min = new BigDecimal(inputNumber.getMinValue());
                    if (value.compareTo(min) < 0) {
                        submittedValue = String.valueOf(min.doubleValue());
                    }
                }
                if (!ComponentUtils.isValueBlank(inputNumber.getMaxValue())) {
                    BigDecimal max = new BigDecimal(inputNumber.getMaxValue());
                    if (value.compareTo(max) > 0) {
                        submittedValue = String.valueOf(max.doubleValue());
                    }
                }
            }
        }
        catch (NumberFormatException ex) {
            throw new FacesException("Invalid number", ex);
        }

        inputNumber.setSubmittedValue(submittedValue);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        InputNumber inputNumber = (InputNumber) component;

        Object value = inputNumber.getValue();
        String valueToRender = ComponentUtils.getValueToRender(context, inputNumber, value);
        if (valueToRender == null) {
            valueToRender = "";
        }

        encodeMarkup(context, inputNumber, value, valueToRender);
        encodeScript(context, inputNumber, value, valueToRender);
    }

    protected void encodeMarkup(FacesContext context, InputNumber inputNumber, Object value, String valueToRender)
            throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = inputNumber.getClientId(context);

        String styleClass = inputNumber.getStyleClass();
        styleClass = styleClass == null ? InputNumber.STYLE_CLASS : InputNumber.STYLE_CLASS + " " + styleClass;

        writer.startElement(SPAN, inputNumber);
        writer.writeAttribute("id", clientId, null);
        writer.writeAttribute(CLASS, styleClass, "styleClass");

        if (inputNumber.getStyle() != null) {
            writer.writeAttribute(STYLE, inputNumber.getStyle(), STYLE);
        }

        encodeInput(context, inputNumber, clientId, valueToRender);
        encodeHiddenInput(context, inputNumber, clientId);

        writer.endElement(SPAN);
    }

    protected void encodeHiddenInput(FacesContext context, InputNumber inputNumber, String clientId) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String inputId = clientId + "_hinput";

        writer.startElement(INPUT, null);
        writer.writeAttribute("id", inputId, null);
        writer.writeAttribute(NAME, inputId, null);
        writer.writeAttribute(TYPE, "hidden", null);
        writer.writeAttribute("autocomplete", "off", null);

        if (inputNumber.getOnchange() != null) {
            writer.writeAttribute("onchange", inputNumber.getOnchange(), null);
        }

        if (inputNumber.getOnkeydown() != null) {
            writer.writeAttribute("onkeydown", inputNumber.getOnkeydown(), null);
        }

        if (inputNumber.getOnkeyup() != null) {
            writer.writeAttribute("onkeyup", inputNumber.getOnkeyup(), null);
        }

        if (PrimeApplicationContext.getCurrentInstance(context).getConfig().isClientSideValidationEnabled()) {
            renderValidationMetadata(context, inputNumber);
        }

        writer.endElement(INPUT);

    }

    protected void encodeInput(FacesContext context, InputNumber inputNumber, String clientId, String valueToRender)
            throws IOException {

        ResponseWriter writer = context.getResponseWriter();
        String inputId = clientId + "_input";

        String inputStyle = inputNumber.getInputStyle();
        String inputStyleClass = inputNumber.getInputStyleClass();

        String style = inputStyle;

        String styleClass = InputText.STYLE_CLASS;
        styleClass = inputNumber.isValid() ? styleClass : styleClass + " ui-state-error";
        styleClass = !inputNumber.isDisabled() ? styleClass : styleClass + " ui-state-disabled";
        if (!isValueBlank(inputStyleClass)) {
            styleClass += " " + inputStyleClass;
        }

        writer.startElement(INPUT, null);
        writer.writeAttribute("id", inputId, null);
        writer.writeAttribute(NAME, inputId, null);
        writer.writeAttribute(TYPE, inputNumber.getType(), null);
        writer.writeAttribute(VALUE, valueToRender, null);

        renderPassThruAttributes(context, inputNumber, HTML.INPUT_TEXT_ATTRS_WITHOUT_EVENTS);
        renderDomEvents(context, inputNumber, HTML.INPUT_TEXT_EVENTS);

        if (inputNumber.isReadonly()) {
            writer.writeAttribute(READONLY, READONLY, READONLY);
        }
        if (inputNumber.isDisabled()) {
            writer.writeAttribute(DISABLED, DISABLED, DISABLED);
        }

        if (!isValueBlank(style)) {
            writer.writeAttribute(STYLE, style, null);
        }

        writer.writeAttribute(CLASS, styleClass, null);

        if (PrimeApplicationContext.getCurrentInstance(context).getConfig().isClientSideValidationEnabled()) {
            renderValidationMetadata(context, inputNumber);
        }

        writer.endElement(INPUT);
    }

    protected void encodeScript(FacesContext context, InputNumber inputNumber, Object value, String valueToRender)
            throws IOException {
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init(InputNumber.class.getSimpleName(), inputNumber.resolveWidgetVar(), inputNumber.getClientId());
        wb.attr(DISABLED, inputNumber.isDisabled())
                .attr("valueToRender", formatForPlugin(valueToRender, value));

        String metaOptions = getOptions(inputNumber);
        if (!metaOptions.isEmpty()) {
            wb.nativeAttr("pluginOptions", metaOptions);
        }

        wb.finish();
    }

    protected String getOptions(InputNumber inputNumber) {

        String decimalSeparator = inputNumber.getDecimalSeparator();
        String thousandSeparator = inputNumber.getThousandSeparator();
        String symbol = inputNumber.getSymbol();
        String symbolPosition = inputNumber.getSymbolPosition();
        String minValue = inputNumber.getMinValue();
        String maxValue = inputNumber.getMaxValue();
        String roundMethod = inputNumber.getRoundMethod();
        String decimalPlaces = inputNumber.getDecimalPlaces();
        String emptyValue = inputNumber.getEmptyValue();
        String lZero = inputNumber.getLeadingZero();
        boolean padControl = inputNumber.isPadControl();

        String options = "";
        options += isValueBlank(decimalSeparator) ? "" : "aDec:\"" + escapeText(decimalSeparator) + LITERAL0;
        //empty thousandSeparator must be explicity defined.
        options += isValueBlank(thousandSeparator) ? "aSep:''," : "aSep:\"" + escapeText(thousandSeparator) + LITERAL0;
        options += isValueBlank(symbol) ? "" : "aSign:\"" + escapeText(symbol) + LITERAL0;
        options += isValueBlank(symbolPosition) ? "" : "pSign:\"" + escapeText(symbolPosition) + LITERAL0;
        options += isValueBlank(minValue) ? "" : "vMin:\"" + escapeText(minValue) + LITERAL0;
        options += isValueBlank(maxValue) ? "" : "vMax:\"" + escapeText(maxValue) + LITERAL0;
        options += isValueBlank(roundMethod) ? "" : "mRound:\"" + escapeText(roundMethod) + LITERAL0;
        options += isValueBlank(decimalPlaces) ? "" : "mDec:\"" + escapeText(decimalPlaces) + LITERAL0;
        options += "wEmpty:\"" + escapeText(emptyValue) + LITERAL0;
        options += "lZero:\"" + escapeText(lZero) + LITERAL0;
        options += "aPad:" + padControl + ",";

        //if all options are empty return empty
        if (options.isEmpty()) {
            return "";
        }

        //delete the last comma
        int lastInd = options.length() - 1;
        if (options.charAt(lastInd) == ',') {
            options = options.substring(0, lastInd);
        }
        return "{" + options + "}";

    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private String formatForPlugin(String valueToRender, Object value) {

        if (isValueBlank(valueToRender)) {
            return "";
        }
        else {
            try {
                Object objectToRender;
                if (value instanceof BigDecimal || doubleValueCheck(valueToRender)) {
                    objectToRender = new BigDecimal(valueToRender);
                }
                else {
                    objectToRender = new Double(valueToRender);
                }

                NumberFormat formatter = new DecimalFormat("#0.0#");
                formatter.setRoundingMode(RoundingMode.FLOOR);
                //autoNumeric jquery plugin max and min limits
                formatter.setMinimumFractionDigits(15);
                formatter.setMaximumFractionDigits(15);
                formatter.setMaximumIntegerDigits(20);
                String f = formatter.format(objectToRender);

                //force to english decimal separator
                f = f.replace(',', '.');
                return f;
            }
            catch (Exception e) {
                throw new IllegalArgumentException("Error converting  [" + valueToRender + "] to a double value;", e);
            }
        }
    }

    protected boolean doubleValueCheck(String valueToRender) {
        int counter = 0;
        int length = valueToRender.length();

        for (int i = 0; i < length; i++) {
            if (valueToRender.charAt(i) == '9') {
                counter++;
            }
        }

        return (counter > 15 || length > 15);
    }

    @Override
    protected String getHighlighter() {
        return "inputnumber";
    }

}
