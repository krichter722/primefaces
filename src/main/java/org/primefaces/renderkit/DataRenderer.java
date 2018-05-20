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
package org.primefaces.renderkit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.component.api.Pageable;
import org.primefaces.component.api.UIData;
import org.primefaces.component.paginator.CurrentPageReportRenderer;
import org.primefaces.component.paginator.FirstPageLinkRenderer;
import org.primefaces.component.paginator.JumpToPageDropdownRenderer;
import org.primefaces.component.paginator.LastPageLinkRenderer;
import org.primefaces.component.paginator.NextPageLinkRenderer;
import org.primefaces.component.paginator.PageLinksRenderer;
import org.primefaces.component.paginator.PaginatorElementRenderer;
import org.primefaces.component.paginator.PrevPageLinkRenderer;
import org.primefaces.component.paginator.RowsPerPageDropdownRenderer;
import org.primefaces.util.MessageFactory;
import org.primefaces.util.WidgetBuilder;
import static org.primefaces.component.Literals.CLASS;
import static org.primefaces.component.Literals.DIV;
import static org.primefaces.component.Literals.ROLE;

public class DataRenderer extends CoreRenderer {

    private static final Map<String, PaginatorElementRenderer> PAGINATOR_ELEMENTS = new HashMap<String, PaginatorElementRenderer>();

    static {
        PAGINATOR_ELEMENTS.put("{CurrentPageReport}", new CurrentPageReportRenderer());
        PAGINATOR_ELEMENTS.put("{FirstPageLink}", new FirstPageLinkRenderer());
        PAGINATOR_ELEMENTS.put("{PreviousPageLink}", new PrevPageLinkRenderer());
        PAGINATOR_ELEMENTS.put("{NextPageLink}", new NextPageLinkRenderer());
        PAGINATOR_ELEMENTS.put("{LastPageLink}", new LastPageLinkRenderer());
        PAGINATOR_ELEMENTS.put("{PageLinks}", new PageLinksRenderer());
        PAGINATOR_ELEMENTS.put("{RowsPerPageDropdown}", new RowsPerPageDropdownRenderer());
        PAGINATOR_ELEMENTS.put("{JumpToPageDropdown}", new JumpToPageDropdownRenderer());
    }

    public static void addPaginatorElement(String element, PaginatorElementRenderer renderer) {
        PAGINATOR_ELEMENTS.put(element, renderer);
    }

    public static PaginatorElementRenderer removePaginatorElement(String element) {
        return PAGINATOR_ELEMENTS.remove(element);
    }

    protected void encodePaginatorMarkup(FacesContext context, Pageable pageable, String position) throws IOException {
        if (!pageable.isPaginatorAlwaysVisible() && pageable.getPageCount() <= 1) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        boolean isTop = position.equals("top");
        UIComponent leftTopContent = pageable.getFacet("paginatorTopLeft");
        UIComponent rightTopContent = pageable.getFacet("paginatorTopRight");
        UIComponent leftBottomContent = pageable.getFacet("paginatorBottomLeft");
        UIComponent rightBottomContent = pageable.getFacet("paginatorBottomRight");

        String styleClass = isTop ? UIData.PAGINATOR_TOP_CONTAINER_CLASS : UIData.PAGINATOR_BOTTOM_CONTAINER_CLASS;
        String id = pageable.getClientId(context) + "_paginator_" + position;

        //add corners
        if (!isTop && pageable.getFooter() == null) {
            styleClass = styleClass + " ui-corner-bottom";
        }
        else if (isTop && pageable.getHeader() == null) {
            styleClass = styleClass + " ui-corner-top";
        }

        String ariaMessage = MessageFactory.getMessage(UIData.ARIA_HEADER_LABEL, new Object[]{});

        writer.startElement(DIV, null);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute(CLASS, styleClass, null);
        writer.writeAttribute(ROLE, "navigation", null);
        writer.writeAttribute("aria-label", ariaMessage, null);

        if (leftTopContent != null && isTop) {
            writer.startElement(DIV, null);
            writer.writeAttribute(CLASS, UIData.PAGINATOR_TOP_LEFT_CONTENT_CLASS, null);
            renderChild(context, leftTopContent);
            writer.endElement(DIV);
        }

        if (rightTopContent != null && isTop) {
            writer.startElement(DIV, null);
            writer.writeAttribute(CLASS, UIData.PAGINATOR_TOP_RIGHT_CONTENT_CLASS, null);
            renderChild(context, rightTopContent);
            writer.endElement(DIV);
        }

        String[] elements = pageable.getPaginatorTemplate().split(" ");
        for (String element : elements) {
            PaginatorElementRenderer renderer = PAGINATOR_ELEMENTS.get(element);
            if (renderer != null) {
                renderer.render(context, pageable);
            }
            else {
                if (element.startsWith("{") && element.endsWith("}")) {
                    UIComponent elementFacet = pageable.getFacet(element);
                    if (elementFacet != null) {
                        elementFacet.encodeAll(context);
                    }
                }
                else {
                    writer.write(element + " ");
                }
            }
        }
        if (leftBottomContent != null && !isTop) {
            writer.startElement(DIV, null);
            writer.writeAttribute(CLASS, UIData.PAGINATOR_BOTTOM_LEFT_CONTENT_CLASS, null);
            renderChild(context, leftBottomContent);
            writer.endElement(DIV);
        }
        if (rightBottomContent != null && !isTop) {
            writer.startElement(DIV, null);
            writer.writeAttribute(CLASS, UIData.PAGINATOR_BOTTOM_RIGHT_CONTENT_CLASS, null);
            renderChild(context, rightBottomContent);
            writer.endElement(DIV);
        }

        writer.endElement(DIV);
    }

    protected void encodePaginatorConfig(FacesContext context, Pageable pageable, WidgetBuilder wb) throws IOException {
        String clientId = pageable.getClientId(context);
        String paginatorPosition = pageable.getPaginatorPosition();
        String paginatorContainers = null;
        String currentPageTemplate = pageable.getCurrentPageReportTemplate();

        if (paginatorPosition.equalsIgnoreCase("both")) {
            paginatorContainers = "'" + clientId + "_paginator_top','" + clientId + "_paginator_bottom'";
        }
        else {
            paginatorContainers = "'" + clientId + "_paginator_" + paginatorPosition + "'";
        }

        wb.append(",paginator:{")
                .append("id:[").append(paginatorContainers).append(']')
                .append(",rows:").append(pageable.getRows())
                .append(",rowCount:").append(pageable.getRowCount())
                .append(",page:").append(pageable.getPage());

        if (currentPageTemplate != null) {
            String currentPageTemplateTmp = currentPageTemplate.replace("'", "\\'");
            wb.append(",currentPageTemplate:'").append(currentPageTemplateTmp).append('\'');
        }

        if (pageable.getPageLinks() != 10) {
            wb.append(",pageLinks:").append(pageable.getPageLinks());
        }

        if (!pageable.isPaginatorAlwaysVisible()) {
            wb.append(",alwaysVisible:false");
        }

        wb.append("}");
    }

    public void encodeFacet(FacesContext context, UIData data, String facet, String styleClass) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        UIComponent component = data.getFacet(facet);

        if (component != null && component.isRendered()) {
            writer.startElement(DIV, null);
            writer.writeAttribute(CLASS, styleClass, null);
            component.encodeAll(context);
            writer.endElement(DIV);
        }
    }
}
