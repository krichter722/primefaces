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
package org.primefaces.component.fileupload;

import java.io.IOException;
import javax.faces.FacesException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;
import static org.primefaces.component.Literals.BUTTON;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DISABLED;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.INPUT;
import static org.primefaces.component.Literals.NAME;
import static org.primefaces.component.Literals.ROLE;
import static org.primefaces.component.Literals.SPAN;
import static org.primefaces.component.Literals.STYLE;
import static org.primefaces.component.Literals.TABINDEX;
import static org.primefaces.component.Literals.TYPE;
import static org.primefaces.component.Literals.VALUE;
import org.primefaces.context.PrimeApplicationContext;
import org.primefaces.expression.SearchExpressionFacade;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.HTML;
import org.primefaces.util.WidgetBuilder;

public class FileUploadRenderer extends CoreRenderer {

    private static final String MULTIPLE = "multiple";

    @Override
    public void decode(FacesContext context, UIComponent component) {

        if (!context.getExternalContext().getRequestContentType().toLowerCase().startsWith("multipart/")) {
            return;
        }

        FileUpload fileUpload = (FileUpload) component;

        if (!fileUpload.isDisabled()) {
            PrimeApplicationContext applicationContext = PrimeApplicationContext.getCurrentInstance(context);
            String uploader = applicationContext.getConfig().getUploader();
            boolean isAtLeastJSF22 = applicationContext.getEnvironment().isAtLeastJsf22();
            String inputToDecodeId = getSimpleInputDecodeId(fileUpload, context);

            if (uploader.equals("auto")) {
                if (isAtLeastJSF22) {
                    NativeFileUploadDecoder.decode(context, fileUpload, inputToDecodeId);
                }
                else {
                    CommonsFileUploadDecoder.decode(context, fileUpload, inputToDecodeId);
                }
            }
            else if (uploader.equals("native")) {
                if (!isAtLeastJSF22) {
                    throw new FacesException("native uploader requires at least a JSF 2.2 runtime");
                }

                NativeFileUploadDecoder.decode(context, fileUpload, inputToDecodeId);
            }
            else if (uploader.equals("commons")) {
                CommonsFileUploadDecoder.decode(context, fileUpload, inputToDecodeId);
            }
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        FileUpload fileUpload = (FileUpload) component;

        encodeMarkup(context, fileUpload);
        encodeScript(context, fileUpload);
    }

    protected void encodeScript(FacesContext context, FileUpload fileUpload) throws IOException {
        String clientId = fileUpload.getClientId(context);
        String update = fileUpload.getUpdate();
        String process = fileUpload.getProcess();
        WidgetBuilder wb = getWidgetBuilder(context);

        if (fileUpload.getMode().equals("advanced")) {
            wb.init("FileUpload", fileUpload.resolveWidgetVar(), clientId);

            wb.attr("auto", fileUpload.isAuto(), false)
                    .attr("dnd", fileUpload.isDragDropSupport(), true)
                    .attr("update", SearchExpressionFacade.resolveClientIds(context, fileUpload, update), null)
                    .attr("process", SearchExpressionFacade.resolveClientIds(context, fileUpload, process), null)
                    .attr("maxFileSize", fileUpload.getSizeLimit(), Long.MAX_VALUE)
                    .attr("fileLimit", fileUpload.getFileLimit(), Integer.MAX_VALUE)
                    .attr("invalidFileMessage", escapeText(fileUpload.getInvalidFileMessage()), null)
                    .attr("invalidSizeMessage", escapeText(fileUpload.getInvalidSizeMessage()), null)
                    .attr("fileLimitMessage", escapeText(fileUpload.getFileLimitMessage()), null)
                    .attr("messageTemplate", escapeText(fileUpload.getMessageTemplate()), null)
                    .attr("previewWidth", fileUpload.getPreviewWidth(), 80)
                    .attr(DISABLED, fileUpload.isDisabled(), false)
                    .attr("sequentialUploads", fileUpload.isSequential(), false)
                    .callback("onstart", "function()", fileUpload.getOnstart())
                    .callback("onerror", "function()", fileUpload.getOnerror())
                    .callback("oncomplete", "function(args)", fileUpload.getOncomplete());

            String allowTypes = fileUpload.getAllowTypes();

            if (allowTypes != null) {
                wb.append(",allowTypes:").append(allowTypes);
            }
        }
        else {
            wb.init("SimpleFileUpload", fileUpload.resolveWidgetVar(), clientId)
                    .attr("skinSimple", fileUpload.isSkinSimple(), false)
                    .attr("maxFileSize", fileUpload.getSizeLimit(), Long.MAX_VALUE)
                    .attr("invalidSizeMessage", escapeText(fileUpload.getInvalidSizeMessage()), null);
        }

        wb.finish();
    }

    protected void encodeMarkup(FacesContext context, FileUpload fileUpload) throws IOException {
        if (fileUpload.getMode().equals("simple")) {
            encodeSimpleMarkup(context, fileUpload);
        }
        else {
            encodeAdvancedMarkup(context, fileUpload);
        }
    }

    protected void encodeAdvancedMarkup(FacesContext context, FileUpload fileUpload) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = fileUpload.getClientId(context);
        String style = fileUpload.getStyle();
        String styleClass = fileUpload.getStyleClass();
        styleClass = styleClass == null ? FileUpload.CONTAINER_CLASS : FileUpload.CONTAINER_CLASS + " " + styleClass;
        boolean disabled = fileUpload.isDisabled();

        writer.startElement(DIV, fileUpload);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(CLASS, styleClass, styleClass);
        if (style != null) {
            writer.writeAttribute(STYLE, style, STYLE);
        }

        //buttonbar
        writer.startElement(DIV, fileUpload);
        writer.writeAttribute(CLASS, FileUpload.BUTTON_BAR_CLASS, null);

        //choose button
        encodeChooseButton(context, fileUpload, disabled);

        if (!fileUpload.isAuto()) {
            encodeButton(context, fileUpload.getUploadLabel(), FileUpload.UPLOAD_BUTTON_CLASS, " " + fileUpload.getUploadIcon());
            encodeButton(context, fileUpload.getCancelLabel(), FileUpload.CANCEL_BUTTON_CLASS, " " + fileUpload.getCancelIcon());
        }

        writer.endElement(DIV);

        renderChildren(context, fileUpload);

        //content
        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, FileUpload.CONTENT_CLASS, null);

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, FileUpload.FILES_CLASS, null);
        writer.startElement(DIV, null);
        writer.endElement(DIV);
        writer.endElement(DIV);

        writer.endElement(DIV);

        writer.endElement(DIV);
    }

    protected void encodeSimpleMarkup(FacesContext context, FileUpload fileUpload) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = fileUpload.getClientId(context);
        String style = fileUpload.getStyle();
        String styleClass = fileUpload.getStyleClass();
        String label = fileUpload.getLabel();

        if (fileUpload.isSkinSimple()) {
            styleClass = (styleClass == null) ? FileUpload.CONTAINER_CLASS_SIMPLE : FileUpload.CONTAINER_CLASS_SIMPLE + " " + styleClass;
            styleClass = isValueBlank(label) ? FileUpload.BUTTON_ICON_ONLY + " " + styleClass : styleClass;
            String buttonClass = HTML.BUTTON_TEXT_ICON_LEFT_BUTTON_CLASS;
            if (fileUpload.isDisabled()) {
                buttonClass += " ui-state-disabled";
            }

            writer.startElement(SPAN, fileUpload);
            writer.writeAttribute("id", clientId, "id");
            writer.writeAttribute(CLASS, styleClass, "styleClass");
            if (style != null) {
                writer.writeAttribute(STYLE, style, STYLE);
            }

            writer.startElement(SPAN, null);
            writer.writeAttribute(CLASS, buttonClass, null);

            //button icon
            writer.startElement(SPAN, null);
            writer.writeAttribute(CLASS, HTML.BUTTON_LEFT_ICON_CLASS + " ui-icon-plusthick", null);
            writer.endElement(SPAN);

            //text
            writer.startElement(SPAN, null);
            writer.writeAttribute("id", clientId + "_label", null);
            writer.writeAttribute(CLASS, HTML.BUTTON_TEXT_CLASS, null);
            if (isValueBlank(label)) {
                writer.write("&nbsp;");
            }
            else {
                writer.writeText(label, VALUE);
            }

            writer.endElement(SPAN);

            encodeInputField(context, fileUpload, fileUpload.getClientId(context));

            writer.endElement(SPAN);

            writer.startElement(SPAN, fileUpload);
            writer.writeAttribute(CLASS, FileUpload.FILENAME_CLASS, null);
            writer.endElement(SPAN);

            writer.endElement(SPAN);
        }
        else {
            encodeSimpleInputField(context, fileUpload, fileUpload.getClientId(context), style, styleClass);
        }
    }

    protected void encodeChooseButton(FacesContext context, FileUpload fileUpload, boolean disabled) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = fileUpload.getClientId(context);
        String label = fileUpload.getLabel();
        String cssClass = HTML.BUTTON_TEXT_ICON_LEFT_BUTTON_CLASS + " " + FileUpload.CHOOSE_BUTTON_CLASS;
        cssClass = isValueBlank(label) ? FileUpload.BUTTON_ICON_ONLY + " " + cssClass : cssClass;
        String tabindex = (disabled) ? "-1" : "0";
        if (disabled) {
            cssClass += " ui-state-disabled";
        }

        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, cssClass, null);
        writer.writeAttribute(TABINDEX, tabindex, null);
        writer.writeAttribute(ROLE, BUTTON, null);
        writer.writeAttribute("aria-labelledby", clientId + "_label", null);

        //button icon
        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, HTML.BUTTON_LEFT_ICON_CLASS + " " + fileUpload.getChooseIcon(), null);
        writer.endElement(SPAN);

        //text
        writer.startElement(SPAN, null);
        writer.writeAttribute("id", clientId + "_label", null);
        writer.writeAttribute(CLASS, HTML.BUTTON_TEXT_CLASS, null);
        if (isValueBlank(label)) {
            writer.write("&nbsp;");
        }
        else {
            writer.writeText(label, VALUE);
        }

        writer.endElement(SPAN);

        if (!disabled) {
            encodeInputField(context, fileUpload, clientId);
        }

        writer.endElement(SPAN);
    }

    protected void encodeInputField(FacesContext context, FileUpload fileUpload, String clientId) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String inputId = clientId + "_input";

        writer.startElement(INPUT, null);
        writer.writeAttribute(TYPE, "file", null);
        writer.writeAttribute("id", inputId, null);
        writer.writeAttribute(NAME, inputId, null);
        writer.writeAttribute(TABINDEX, "-1", null);

        if (fileUpload.isMultiple()) {
            writer.writeAttribute(MULTIPLE, MULTIPLE, null);
        }
        if (fileUpload.isDisabled()) {
            writer.writeAttribute(DISABLED, DISABLED, DISABLED);
        }
        if (fileUpload.getAccept() != null) {
            writer.writeAttribute("accept", fileUpload.getAccept(), null);
        }

        renderDynamicPassThruAttributes(context, fileUpload);

        writer.endElement(INPUT);
    }

    protected void encodeSimpleInputField(FacesContext context, FileUpload fileUpload, String clientId, String style, String styleClass) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement(INPUT, null);
        writer.writeAttribute(TYPE, "file", null);
        writer.writeAttribute("id", clientId, null);
        writer.writeAttribute(NAME, clientId, null);

        if (fileUpload.isMultiple()) {
            writer.writeAttribute(MULTIPLE, MULTIPLE, null);
        }
        if (fileUpload.isDisabled()) {
            writer.writeAttribute(DISABLED, DISABLED, DISABLED);
        }
        if (fileUpload.getAccept() != null) {
            writer.writeAttribute("accept", fileUpload.getAccept(), null);
        }
        if (style != null) {
            writer.writeAttribute(STYLE, style, STYLE);
        }
        if (styleClass != null) {
            writer.writeAttribute(CLASS, styleClass, "styleClass");
        }

        renderDynamicPassThruAttributes(context, fileUpload);

        writer.endElement(INPUT);
    }

    protected void encodeButton(FacesContext context, String label, String styleClass, String icon) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String cssClass = HTML.BUTTON_TEXT_ICON_LEFT_BUTTON_CLASS + " ui-state-disabled " + styleClass;
        cssClass = isValueBlank(label) ? FileUpload.BUTTON_ICON_ONLY + " " + cssClass : cssClass;

        writer.startElement(BUTTON, null);
        writer.writeAttribute(TYPE, BUTTON, null);
        writer.writeAttribute(CLASS, cssClass, null);
        writer.writeAttribute(DISABLED, DISABLED, null);

        //button icon
        String iconClass = HTML.BUTTON_LEFT_ICON_CLASS;
        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, iconClass + " " + icon, null);
        writer.endElement(SPAN);

        //text
        writer.startElement(SPAN, null);
        writer.writeAttribute(CLASS, HTML.BUTTON_TEXT_CLASS, null);
        if (isValueBlank(label)) {
            writer.write("&nbsp;");
        }
        else {
            writer.writeText(label, VALUE);
        }

        writer.endElement(SPAN);

        writer.endElement(BUTTON);
    }

    @Override
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
        FileUpload fileUpload = (FileUpload) component;

        if (fileUpload.getMode().equals("simple") && submittedValue != null && submittedValue.equals("")) {
            return null;
        }
        else {
            return submittedValue;
        }
    }

    public String getSimpleInputDecodeId(FileUpload fileUpload, FacesContext context) {
        String clientId = fileUpload.getClientId(context);

        if (fileUpload.getMode().equals("simple") && !fileUpload.isSkinSimple()) {
            return clientId;
        }
        else {
            return clientId + "_input";
        }
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        // Do nothing
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
