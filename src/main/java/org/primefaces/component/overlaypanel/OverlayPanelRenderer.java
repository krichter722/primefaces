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
package org.primefaces.component.overlaypanel;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.expression.SearchExpressionFacade;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.WidgetBuilder;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.STYLE;

public class OverlayPanelRenderer extends CoreRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        OverlayPanel panel = (OverlayPanel) component;

        if (panel.isContentLoadRequest(context)) {
            renderChildren(context, panel);
        }
        else {
            encodeMarkup(context, panel);
            encodeScript(context, panel);
        }
    }

    protected void encodeMarkup(FacesContext context, OverlayPanel panel) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = panel.getClientId(context);
        String style = panel.getStyle();
        String styleClass = panel.getStyleClass();
        styleClass = styleClass == null ? OverlayPanel.STYLE_CLASS : OverlayPanel.STYLE_CLASS + " " + styleClass;

        writer.startElement(DIV, panel);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute(CLASS, styleClass, "styleClass");
        if (style != null) {
            writer.writeAttribute(STYLE, style, STYLE);
        }

        writer.startElement(DIV, null);
        writer.writeAttribute(CLASS, OverlayPanel.CONTENT_CLASS, "styleClass");
        if (!panel.isDynamic()) {
            renderChildren(context, panel);
        }
        writer.endElement(DIV);

        writer.endElement(DIV);
    }

    protected void encodeScript(FacesContext context, OverlayPanel panel) throws IOException {
        String target = SearchExpressionFacade.resolveClientId(context, panel, panel.getFor());
        String clientId = panel.getClientId(context);

        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("OverlayPanel", panel.resolveWidgetVar(), clientId)
                .attr("target", target)
                .attr("showEvent", panel.getShowEvent(), null)
                .attr("hideEvent", panel.getHideEvent(), null)
                .attr("showEffect", panel.getShowEffect(), null)
                .attr("hideEffect", panel.getHideEffect(), null)
                .callback("onShow", "function()", panel.getOnShow())
                .callback("onHide", "function()", panel.getOnHide())
                .attr("my", panel.getMy(), null)
                .attr("at", panel.getAt(), null)
                .attr("appendTo", panel.getAppendTo(), null)
                .attr("dynamic", panel.isDynamic(), false)
                .attr("dismissable", panel.isDismissable(), true)
                .attr("showCloseIcon", panel.isShowCloseIcon(), false)
                .attr("modal", panel.isModal(), false)
                .attr("showDelay", panel.getShowDelay(), 0);

        wb.finish();
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        //Do nothing
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
