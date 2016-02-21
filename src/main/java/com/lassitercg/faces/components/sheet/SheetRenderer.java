/*
 * The MIT License (MIT)
 * Copyright (c) 2013 Lassiter Consulting Group, LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.lassitercg.faces.components.sheet;

import com.lassitercg.faces.components.util.VarBuilder;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;
import org.primefaces.util.WidgetBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.component.UIComponent;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.model.SelectItem;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Sheet renderer.
 *
 * @author <a href="mailto:mlassiter@lassitercg.com">Mark Lassiter</a>
 */
@FacesRenderer(componentFamily = Sheet.FAMILY, rendererType = Sheet.RENDERERTYPE)
public class SheetRenderer extends Renderer {

    /**
     * Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(SheetRenderer.class);

    /**
     * Encodes the Sheet component
     */
    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        final Sheet sheet = (Sheet) component;

        // update column mappings on render
        sheet.updateColumnMappings();

        // sort data
        sheet.sortAndFilter();

        ResponseWriter responseWriter = context.getResponseWriter();

        // encode markup
        encodeMarkup(context, sheet, responseWriter);

        // encode javascript
        encodeJavascript(context, sheet, responseWriter);
    }

    /**
     * Encodes the HTML markup for the sheet.
     *
     * @param context
     * @param sheet
     * @throws IOException
     */
    protected void encodeMarkup(FacesContext context, Sheet sheet, ResponseWriter responseWriter) throws IOException {
        /*
         * <div id="..." name="..." class="" style="">
		 */
        String styleClass = sheet.getStyleClass();
        String clientId = sheet.getClientId(context);
        Integer width = sheet.getWidth();
        Integer height = sheet.getHeight();

        // outer div to wrapper table
        responseWriter.startElement("div", null);
        responseWriter.writeAttribute("id", clientId, "id");
        responseWriter.writeAttribute("name", clientId, "clientId");
        // note: can't use ui-datatable here because it will mess with
        // handsontable cell rendering
        String divclass = "ui-handsontable ui-widget";
        if (styleClass != null)
            divclass = divclass + " " + styleClass;
        if (!sheet.isValid())
            divclass = divclass + " ui-state-error";

        responseWriter.writeAttribute("class", divclass, "styleClass");
        if (width != null)
            responseWriter.writeAttribute("style", "width: " + width + "px;", null);

        encodeHeader(context, responseWriter, sheet);

        // handsontable div
        responseWriter.startElement("div", null);
        responseWriter.writeAttribute("id", clientId + "_tbl", "id");
        responseWriter.writeAttribute("name", clientId + "_tbl", "clientId");
        responseWriter.writeAttribute("class", "handsontable-inner", "styleClass");

        String style = "";
        if (width != null)
            style = style + "width: " + width + "px;";

        if (height != null)
            style = style + "height: " + height + "px;";

        if (style.length() > 0)
            responseWriter.writeAttribute("style", style, null);

        encodeHiddenInputs(responseWriter, sheet, clientId);
        encodeFilterValues(context, responseWriter, sheet, clientId);

        responseWriter.endElement("div");
        encodeFooter(context, responseWriter, sheet);
        responseWriter.endElement("div");
    }

    /**
     * Encodes an optional attribute to the widget builder specified.
     *
     * @param wb       the WidgetBuilder to append to
     * @param attrName the attribute name
     * @param value    the value
     * @throws IOException
     */
    protected void encodeOptionalAttr(WidgetBuilder wb, String attrName, String value) throws IOException {
        if (value != null)
            wb.attr(attrName, value);
    }

    /**
     * Encodes an optional native attribute (unquoted).
     *
     * @param wb       the WidgetBuilder to append to
     * @param attrName the attribute name
     * @param value    the value
     * @throws IOException
     */
    protected void encodeOptionalNativeAttr(WidgetBuilder wb, String attrName, Object value) throws IOException {
        if (value != null)
            wb.nativeAttr(attrName, value.toString());
    }

    /**
     * Encodes the Javascript for the sheet.
     *
     * @param context
     * @param sheet
     * @throws IOException
     */
    protected void encodeJavascript(FacesContext context, Sheet sheet, ResponseWriter responseWriter)
            throws IOException {

        String widgetVar = sheet.resolveWidgetVar();
        String clientId = sheet.getClientId(context);

        WidgetBuilder wb = new WidgetBuilder(context);
        wb.initWithDomReady("Sheet", widgetVar, clientId);

        // errors
        encodeBadData(context, sheet, wb);
        // data
        encodeData(context, sheet, wb);

        // the delta var that will be used to track changes client side
        // stringified and placed in hidden input for submission
        wb.nativeAttr("delta", "{}");

        // filters
        encodeFilterVar(context, sheet, wb);
        // sortable
        encodeSortVar(context, sheet, wb);
        // behaviors
        encodeBehaviors(context, sheet, wb);

        encodeOptionalNativeAttr(wb, "fixedColumnsLeft", sheet.getFixedCols());
        encodeOptionalNativeAttr(wb, "fixedRowsTop", sheet.getFixedRows());
        encodeOptionalNativeAttr(wb, "width", sheet.getWidth());
        encodeOptionalNativeAttr(wb, "height", sheet.getHeight());
        String emptyMessage = sheet.getEmptyMessage();
        if (StringUtils.isEmpty(emptyMessage)) {
            emptyMessage = "No Records Found";
        }
        encodeOptionalAttr(wb, "emptyMessage", emptyMessage);
        encodeOptionalAttr(wb, "stretchH", sheet.getStretchH());
        encodeOptionalAttr(wb, "currentRowClassName", sheet.getCurrentRowClass());
        encodeOptionalAttr(wb, "currentColClassName", sheet.getCurrentColClass());

        wb.nativeAttr("rowHeaders", sheet.isShowRowHeaders().toString());

        encodeColHeaders(context, sheet, wb);
        encodeColOptions(context, sheet, wb);
        wb.finish();
    }

    /**
     * Encodes the necessary JS to render bad data
     *
     * @throws IOException
     */
    protected void encodeBadData(FacesContext context, Sheet sheet, WidgetBuilder wb) throws IOException {
        wb.attr("errors", sheet.getBadDataValue());
    }

    /**
     * Encode the column headers
     *
     * @param context
     * @param sheet
     * @param wb
     * @throws IOException
     */
    protected void encodeColHeaders(FacesContext context, Sheet sheet, WidgetBuilder wb) throws IOException {
        VarBuilder vb = new VarBuilder(null, false);
        for (Column column : sheet.getColumns()) {
            if (!column.isRendered())
                continue;
            vb.appendArrayValue(column.getHeaderText(), true);
        }
        wb.nativeAttr("colHeaders", vb.closeVar().toString());
    }

    /**
     * Encode the column options
     *
     * @param context
     * @param sheet
     * @param wb
     * @throws IOException
     */
    protected void encodeColOptions(FacesContext context, Sheet sheet, WidgetBuilder wb) throws IOException {
        VarBuilder vb = new VarBuilder(null, false);
        for (Column column : sheet.getColumns()) {
            if (!column.isRendered())
                continue;

            VarBuilder options = new VarBuilder(null, true);
            options.appendProperty("type", column.getColType(), true);
            Integer width = column.getColWidth();
            if (width != null)
                options.appendProperty("width", width.toString(), false);
            if (column.isReadonly())
                options.appendProperty("readOnly", "true", false);
            vb.appendArrayValue(options.closeVar().toString(), false);
        }
        wb.nativeAttr("columns", vb.closeVar().toString());
    }

    /**
     * Encode the row data. Builds row data, style data and read only object.
     *
     * @param context
     * @param sheet
     * @param wb
     * @throws IOException
     */
    protected void encodeData(FacesContext context, Sheet sheet, WidgetBuilder wb) throws IOException {

        VarBuilder vbData = new VarBuilder(null, false);
        VarBuilder vbStyle = new VarBuilder(null, true);
        VarBuilder vbRowStyle = new VarBuilder(null, false);
        VarBuilder vbReadOnly = new VarBuilder(null, true);

        List<Object> values = sheet.getSortedValues();
        int row = 0;
        for (Object value : values) {
            sheet.setRowIndex(context, row);
            encodeRow(context, vbData, vbRowStyle, vbStyle, vbReadOnly, sheet, value, row);
            row++;
        }
        sheet.setRowIndex(context, -1);
        wb.nativeAttr("data", vbData.closeVar().toString());
        wb.nativeAttr("styles", vbStyle.closeVar().toString());
        wb.nativeAttr("rowStyles", vbRowStyle.closeVar().toString());
        wb.nativeAttr("readOnly", vbReadOnly.closeVar().toString());
    }

    /**
     * Encode a single row.
     *
     * @param context
     * @param vbData
     * @param vbRowStyle
     * @param vbStyle
     * @param vbReadOnly
     * @param sheet
     * @param data
     * @param rowIndex
     * @throws IOException
     */
    protected void encodeRow(FacesContext context, VarBuilder vbData, VarBuilder vbRowStyle, VarBuilder vbStyle,
                             VarBuilder vbReadOnly, Sheet sheet, Object data, int rowIndex) throws IOException {

        // encode rowStyle (if any)
        String rowStyleClass = sheet.getRowStyleClass();
        if (rowStyleClass == null)
            vbRowStyle.appendArrayValue("null", false);
        else
            vbRowStyle.appendArrayValue(rowStyleClass, true);

        // data is array of array of data
        VarBuilder vbRow = new VarBuilder(null, false);
        int renderCol = 0;
        for (int col = 0; col < sheet.getColumns().size(); col++) {
            final Column column = sheet.getColumns().get(col);
            if (!column.isRendered())
                continue;

            // render data value
            String value = sheet.getRenderValueForCell(context, sheet.getRowKeyValue(context), col);
            vbRow.appendArrayValue(value, true);

            // custom style
            String styleClass = column.getStyleClass();
            if (styleClass != null) {
                vbStyle.appendRowColProperty(rowIndex, renderCol, styleClass, true);
            }

            // read only per cell
            boolean readOnly = column.isReadonlyCell();
            if (readOnly)
                vbReadOnly.appendRowColProperty(rowIndex, renderCol, "true", true);
            renderCol++;
        }
        // close row and append to vbData
        vbData.appendArrayValue(vbRow.closeVar().toString(), false);
    }

    /**
     * Encode hidden input fields
     *
     * @param responseWriter
     * @param sheet
     * @param clientId
     * @throws IOException
     */
    private void encodeHiddenInputs(ResponseWriter responseWriter, final Sheet sheet, String clientId)
            throws IOException {
        responseWriter.startElement("input", null);
        responseWriter.writeAttribute("id", clientId + "_input", "id");
        responseWriter.writeAttribute("name", clientId + "_input", "name");
        responseWriter.writeAttribute("type", "hidden", null);
        responseWriter.writeAttribute("value", "", null);
        responseWriter.endElement("input");

        responseWriter.startElement("input", null);
        responseWriter.writeAttribute("id", clientId + "_focus", "id");
        responseWriter.writeAttribute("name", clientId + "_focus", "name");
        responseWriter.writeAttribute("type", "hidden", null);
        if (sheet.getFocusId() == null)
            responseWriter.writeAttribute("value", "", null);
        else
            responseWriter.writeAttribute("value", sheet.getFocusId(), null);
        responseWriter.endElement("input");

        responseWriter.startElement("input", null);
        responseWriter.writeAttribute("id", clientId + "_selection", "id");
        responseWriter.writeAttribute("name", clientId + "_selection", "name");
        responseWriter.writeAttribute("type", "hidden", null);
        if (sheet.getSelection() == null)
            responseWriter.writeAttribute("value", "", null);
        else
            responseWriter.writeAttribute("value", sheet.getSelection(), null);
        responseWriter.endElement("input");

        // sort col and order if specified and supported
        int sortCol = sheet.getSortColRenderIndex();
        responseWriter.startElement("input", null);
        responseWriter.writeAttribute("id", clientId + "_sortby", "id");
        responseWriter.writeAttribute("name", clientId + "_sortby", "name");
        responseWriter.writeAttribute("type", "hidden", null);
        responseWriter.writeAttribute("value", sortCol, null);
        responseWriter.endElement("input");

        responseWriter.startElement("input", null);
        responseWriter.writeAttribute("id", clientId + "_sortorder", "id");
        responseWriter.writeAttribute("name", clientId + "_sortorder", "name");
        responseWriter.writeAttribute("type", "hidden", null);
        responseWriter.writeAttribute("value", sheet.getSortOrder().toLowerCase(), null);
        responseWriter.endElement("input");
    }

    /**
     * Encode client behaviors to widget config
     * <p>
     *
     * @param context
     * @param sheet
     * @param wb
     * @throws IOException
     */
    private void encodeBehaviors(FacesContext context, Sheet sheet, WidgetBuilder wb) throws IOException {
        // note we write out the onchange event here so we have the selected
        // cell too
        Map<String, List<ClientBehavior>> behaviors = sheet.getClientBehaviors();

        wb.append(",behaviors:{");
        String clientId = sheet.getClientId();

        // sort event (manual since callBack prepends leading comma)
        wb.append("sort").append(":").append("function(s, event)").append("{").append("PrimeFaces.ab({source: '")
                .append(clientId).append("',event: 'sort', process: '").append(clientId).append("', update: '")
                .append(clientId).append("'}, arguments[1]);}");

        // filter
        wb.callback("filter", "function(s, event)", "PrimeFaces.ab({source: '" + clientId
                + "', event: 'filter', process: '" + clientId + "', update: '" + clientId + "'}, arguments[1]);");

        if (behaviors.containsKey("change")) {
            ClientBehaviorContext behaviorContext = ClientBehaviorContext.createClientBehaviorContext(context, sheet,
                    "change", sheet.getClientId(context), null);
            wb.callback("change", "function(source, event)", behaviors.get("change").get(0).getScript(behaviorContext));
        }

        if (behaviors.containsKey("cellSelect")) {
            ClientBehaviorContext behaviorContext = ClientBehaviorContext.createClientBehaviorContext(context, sheet,
                    "cellSelect", sheet.getClientId(context), null);
            wb.callback("cellSelect", "function(source, event)",
                    behaviors.get("cellSelect").get(0).getScript(behaviorContext));
        }

        wb.append("}");
    }

    /**
     * Encode the sheet footer
     *
     * @param context
     * @param responseWriter
     * @param sheet
     * @throws IOException
     */
    private void encodeFooter(FacesContext context, ResponseWriter responseWriter, final Sheet sheet)
            throws IOException {
        // footer
        UIComponent footer = sheet.getFacet("footer");
        if (footer != null) {
            responseWriter.startElement("div", null);
            responseWriter.writeAttribute("class", "ui-datatable-footer ui-widget-header ui-corner-bottom", null);
            footer.encodeAll(context);
            responseWriter.endElement("div");
        }
    }

    /**
     * Encode the Sheet header
     *
     * @param context
     * @param responseWriter
     * @param sheet
     * @throws IOException
     */
    private void encodeHeader(FacesContext context, ResponseWriter responseWriter, final Sheet sheet)
            throws IOException {
        // header
        UIComponent header = sheet.getFacet("header");
        if (header != null) {
            responseWriter.startElement("div", null);
            responseWriter.writeAttribute("class", "ui-datatable-header ui-widget-header ui-corner-top", null);
            header.encodeAll(context);
            responseWriter.endElement("div");
        }
    }

    /**
     * Encodes the filter values.
     *
     * @param context
     * @param responseWriter
     * @param sheet
     * @throws IOException
     */
    protected void encodeFilterValues(FacesContext context, ResponseWriter responseWriter, Sheet sheet, String clientId)
            throws IOException {
        int renderIdx = 0;
        for (Column column : sheet.getColumns()) {
            if (!column.isRendered())
                continue;

            if (column.getValueExpression("filterBy") != null) {
                responseWriter.startElement("input", null);
                responseWriter.writeAttribute("id", clientId + "_filter_" + renderIdx, "id");
                responseWriter.writeAttribute("name", clientId + "_filter_" + renderIdx, "name");
                responseWriter.writeAttribute("type", "hidden", null);
                responseWriter.writeAttribute("value", column.getFilterValue(), null);
                responseWriter.endElement("input");
            }

            renderIdx++;
        }

    }

    /**
     * Encodes a javascript filter var that informs the col header event of the
     * column's filtering options. The var is an array in the form:
     * <pre>
     * ["false","true",["option 1", "option 2"]]
     * </pre>
     * False indicates no filtering for the column.
     * <p>
     * True indicates simple input text filter.
     * <p>
     * Array of values indicates a drop down filter with the listed options.
     *
     * @param context
     * @param sheet
     * @param wb
     * @throws IOException
     */
    protected void encodeFilterVar(FacesContext context, Sheet sheet, WidgetBuilder wb) throws IOException {
        VarBuilder vb = new VarBuilder(null, false);

        for (Column column : sheet.getColumns()) {
            if (!column.isRendered())
                continue;

            if (column.getValueExpression("filterBy") == null) {
                vb.appendArrayValue("false", true);
                continue;
            }

            Collection<SelectItem> options = column.getFilterOptions();
            if (options == null)
                vb.appendArrayValue("true", true);
            else {
                VarBuilder vbOptions = new VarBuilder(null, false);
                for (SelectItem item : options) {
                    vbOptions.appendArrayValue("{ label: \"" + item.getLabel() + "\", value: \"" + item.getValue()
                                    + "\"}",
                            false);
                }
                vb.appendArrayValue(vbOptions.closeVar().toString(), false);
            }

        }
        wb.nativeAttr("filters", vb.closeVar().toString());
    }

    /**
     * Encodes a javascript sort var that informs the col header event of the
     * column's sorting options. The var is an array of boolean indicating
     * whether or not the column is sortable.
     *
     * @param context
     * @param sheet
     * @param wb
     * @throws IOException
     */
    protected void encodeSortVar(FacesContext context, Sheet sheet, WidgetBuilder wb) throws IOException {
        VarBuilder vb = new VarBuilder(null, false);

        for (Column column : sheet.getColumns()) {
            if (!column.isRendered())
                continue;

            if (column.getValueExpression("sortBy") == null)
                vb.appendArrayValue("false", false);
            else
                vb.appendArrayValue("true", false);
        }
        wb.nativeAttr("sortable", vb.closeVar().toString());
    }

    /**
     * Overrides decode and to parse the request parameters for the two hidden
     * input fields:
     * <ul>
     * <li>clientid_input: any new changes provided by the user</li>
     * <li>clientid_selection: the user's cell selections</li>
     * </ul>
     * These are JSON values and are parsed into our submitted values data on
     * the Sheet component.
     *
     * @param context
     * @parma component
     */
    @Override
    public void decode(FacesContext context, UIComponent component) {
        final Sheet sheet = (Sheet) component;

        // clear updates from previous decode
        sheet.getUpdates().clear();

        // get parameters
        // we'll need the request parameters
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String clientId = sheet.getClientId(context);

        // get our input fields
        String jsonUpdates = params.get(clientId + "_input");
        String jsonSelection = params.get(clientId + "_selection");

        // decode into submitted values on the Sheet
        decodeSubmittedValues(context, sheet, jsonUpdates);

        // decode the selected range so we can puke it back
        decodeSelection(context, sheet, jsonSelection);

        // decode client behaviors
        decodeBehaviors(context, sheet);

        // decode filters
        decodeFilters(context, sheet, params, clientId);

        String sortBy = params.get(clientId + "_sortby");
        String sortOrder = params.get(clientId + "_sortorder");
        if (sortBy != null) {
            int col = Integer.valueOf(sortBy);
            if (col >= 0) {
                col = sheet.getMappedColumn(col);
                sheet.setSortByValueExpression(sheet.getColumns().get(col).getValueExpression("sortBy"));
            }
        }

        if (sortOrder != null)
            sheet.setSortOrder(sortOrder);

        String focus = params.get(clientId + "_focus");
        sheet.setFocusId(focus);
    }

    /**
     * Decodes the filter values
     *
     * @param context
     * @param sheet
     * @param params
     * @param clientId
     */
    protected void decodeFilters(FacesContext context, Sheet sheet, Map<String, String> params, String clientId) {
        int renderIdx = 0;
        for (Column column : sheet.getColumns()) {
            if (!column.isRendered())
                continue;

            if (column.getValueExpression("filterBy") != null) {
                String value = params.get(clientId + "_filter_" + renderIdx);
                column.setFilterValue(value);
            }

            renderIdx++;
        }
    }

    /**
     * Decodes client behaviors (ajax events).
     *
     * @param context   the FacesContext
     * @param component the Component being decodes
     */
    protected void decodeBehaviors(FacesContext context, UIComponent component) {

        // get current behaviors
        Map<String, List<ClientBehavior>> behaviors = ((ClientBehaviorHolder) component).getClientBehaviors();

        // if empty, done
        if (behaviors.isEmpty())
            return;

        // get the parameter map and the behaviorEvent fired
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String behaviorEvent = params.get("javax.faces.behavior.event");

        // if no event, done
        if (behaviorEvent == null)
            return;

        // get behaviors for the event
        List<ClientBehavior> behaviorsForEvent = behaviors.get(behaviorEvent);
        if (behaviorsForEvent == null || behaviorsForEvent.isEmpty())
            return;

        // decode event if we are the source
        String behaviorSource = params.get("javax.faces.source");
        String clientId = component.getClientId();
        if (behaviorSource != null && clientId.equals(behaviorSource)) {
            for (ClientBehavior behavior : behaviorsForEvent) {
                behavior.decode(context, component);
            }
        }
    }

    /**
     * Decodes the user Selection JSON data
     *
     * @param context
     * @param sheet
     * @param jsonSelection
     */
    private void decodeSelection(FacesContext context, Sheet sheet, String jsonSelection) {
        if (StringUtils.isEmpty(jsonSelection))
            return;

        try {
            // data comes in: [ [row, col, oldValue, newValue] ... ]
            JSONArray array = new JSONArray(jsonSelection);
            sheet.setSelectedRow(array.getInt(0));
            sheet.setSelectedColumn(sheet.getMappedColumn(array.getInt(1)));
            sheet.setSelectedLastRow(array.getInt(2));
            sheet.setSelectedLastColumn(array.getInt(3));
            sheet.setSelection(jsonSelection);
        } catch (JSONException e) {
            LOG.error("Failed parsing Ajax JSON message for cell selection: {}", e.getMessage(), e);
        }
    }

    /**
     * Converts the JSON data received from the in the request params into our
     * sumitted values map. The map is cleared first.
     *
     * @param jsonData the submitted JSON data
     * @param sheet
     * @param jsonData
     */
    private void decodeSubmittedValues(FacesContext context, Sheet sheet, String jsonData) {
        if (StringUtils.isEmpty(jsonData))
            return;

        try {
            // data comes in as a JSON Object with named properties for the row
            // and columns updated
            // this is so that multiple updates to the same cell overwrite
            // previous deltas prior to submission
            // we don't care about the property names, just the values, which
            // we'll process in turn
            JSONObject obj = new JSONObject(jsonData);
            @SuppressWarnings("unchecked")
            Iterator<String> keys = obj.keys();
            while (keys.hasNext()) {
                final String key = keys.next();
                // data comes in: [row, col, oldValue, newValue]
                JSONArray update = obj.getJSONArray(key);
                final int row = update.getInt(0);
                final int col = sheet.getMappedColumn(update.getInt(1));
                final String newValue = update.getString(3);
                sheet.setSubmittedValue(context, row, col, newValue);
            }
        } catch (JSONException e) {
            LOG.error("Failed parsing Ajax JSON message for cell change event: {}", e.getMessage(), e);
        }
    }

    /**
     * We render the columns (the children).
     */
    @Override
    public boolean getRendersChildren() {
        return true;
    }

}
