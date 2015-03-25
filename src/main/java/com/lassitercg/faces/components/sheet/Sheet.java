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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.primefaces.component.api.Widget;
import org.primefaces.context.RequestContext;
import org.primefaces.model.BeanPropertyComparator;
import org.primefaces.model.SortOrder;
import org.primefaces.util.ComponentUtils;

import com.lassitercg.faces.components.event.SheetUpdate;
import com.lassitercg.faces.components.util.VarBuilder;

/**
 * Spreadsheet component wrappering the Handsontable jQuery UI component.
 * <p>
 * @author <a href="mailto:mlassiter@lassitercg.com">Mark Lassiter</a>
 * @version $Id:$
 */
@FacesComponent(value = Sheet.COMPONENTTYPE)
@ResourceDependencies({ @ResourceDependency(name = "handsontable.js", target = "head", library = "handsontable"),
		@ResourceDependency(name = "sheet.js", target = "head", library = "handsontable"),
		@ResourceDependency(name = "handsontable.css", target = "head", library = "handsontable"), })
public class Sheet extends UIInput implements ClientBehaviorHolder, EditableValueHolder, Widget {

	public static final String EVENT_CELL_SELECT = "cellSelect";
	public static final String EVENT_CHANGE = "change";
	public static final String FAMILY = "com.lassitercg.faces.components";
	public static final String RENDERERTYPE = "com.lassitercg.faces.components.sheet";
	public static final String COMPONENTTYPE = "com.lassitercg.faces.components.sheet";
	public static final String PARTIAL_SOURCE_PARAM = "javax.faces.source";
	public static final String PARTIAL_BEHAVIOR_EVENT_PARAM = "javax.faces.behavior.event";

	/**
	 * Properties that are tracked by state saving.
	 */
	enum PropertyKeys {
		/**
		 * <p>
		 * The local value of this {@link UIComponent}.
		 * </p>
		 */
		value,

		/**
		 * <p>
		 * Flag indicating whether or not this component is valid.
		 * </p>
		 */
		valid,

		/**
		 * <p>
		 * The request scope attribute under which the data object for the
		 * current row will be exposed when iterating.
		 * </p>
		 */
		var,

		/**
		 * The selected row
		 */
		selectedRow,

		/**
		 * The last selected row
		 */
		selectedLastRow,

		/**
		 * The selected column
		 */
		selectedColumn,

		/**
		 * The last selected column
		 */
		selectedLastColumn,
		/**
		 * flag indication whether or not to show column headers
		 */
		showColumnHeaders,

		/**
		 * flag indication whether or not to show row headers
		 */
		showRowHeaders,

		/**
		 * Fixed rows when scrolling
		 */
		fixedRows,

		/**
		 * Fixed columns when scrolling
		 */
		fixedCols,

		/**
		 * The width of the component in pixels
		 */
		width,

		/**
		 * The height of the component in pixels
		 */
		height,

		/**
		 * The global error message to be displayed when the sheet is in error
		 */
		errorMessage,

		/**
		 * User style class for sheet
		 */
		styleClass,

		/**
		 * The style class to apply to the currently selected row
		 */
		currentRowClass,

		/**
		 * The style class to apply to the currently selected column
		 */
		currentColClass,

		/**
		 * The row key, used to unqiuely identify each row for update operations
		 */
		rowKey,

		/**
		 * The current sortBy value expression
		 */
		sortBy,

		/**
		 * The current direction of the sort
		 */
		sortOrder,

		/**
		 * The original sortBy value expression saved off for reset
		 */
		origSortBy,

		/**
		 * The original sort direction saved off for reset
		 */
		origSortOrder,

		/**
		 * The Handsontable stretchH value
		 */
		stretchH,

		/**
		 * The style class to apply to each row in the sheet (EL expression)
		 */
		rowStyleClass,

		/**
		 * The message displayed when no records are found
		 */
		emptyMessage
	}

	/**
	 * The list of UI Columns
	 */
	private List<Column> columns;

	/**
	 * List of bad updates
	 */
	private List<BadUpdate> badUpdates;

	/**
	 * The sorted list of data
	 */
	private List<Object> sortedList;

	/**
	 * Map of submitted values by row index and column index
	 */
	private Map<RowColIndex, String> submittedValues = new HashMap<RowColIndex, String>();

	/**
	 * Map of local values by row index and column index
	 */
	private Map<RowColIndex, Object> localValues = new HashMap<RowColIndex, Object>();

	/**
	 * Current row Index for iteration operations
	 */
	private int rowIndex = -1;

	/**
	 * The selection data
	 */
	private String selection;

	/**
	 * The id of the focused filter input if any
	 */
	private String focusId;

	/**
	 * Transient list of sheet updates that can be accessed after a successful
	 * model update.
	 */
	private final List<SheetUpdate> updates = new ArrayList<SheetUpdate>();

	/**
	 * Maps a visible, rendered column index to the actual column based on
	 * whether or not the column is rendered. Updated on encode, and used on
	 * decode. Saved in the component state.
	 */
	private Map<Integer, Integer> columnMapping;

	/**
	 * Map by row keys for values found in list
	 */
	private Map<Object, RowMap> rowMap;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.component.UIComponent#getFamily()
	 */
	@Override
	public String getFamily() {
		return FAMILY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.component.UIComponentBase#getRendererType()
	 */
	@Override
	public String getRendererType() {
		return RENDERERTYPE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.component.UIComponentBase#getEventNames()
	 */
	@Override
	public Collection<String> getEventNames() {
		return Arrays.asList(EVENT_CHANGE, EVENT_CELL_SELECT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.component.UIComponentBase#getDefaultEventName()
	 */
	@Override
	public String getDefaultEventName() {
		return EVENT_CHANGE;
	}

	/**
	 * Update's the user's custom style class to be added to the div container
	 * for the sheet.
	 * <p>
	 * @param styleClass
	 */
	public void setStyleClass(String styleClass) {
		getStateHelper().put(PropertyKeys.styleClass, styleClass);
	}

	/**
	 * The user's custom style class to be added to the div container for the
	 * sheet.
	 * <p>
	 * @param styleClass
	 */
	public String getStyleClass() {
		Object result = getStateHelper().eval(PropertyKeys.styleClass, null);
		if (result == null)
			return null;
		return result.toString();
	}

	/**
	 * Update the stretcH value for the component
	 * <p>
	 * @param value
	 */
	public void setStretchH(String value) {
		getStateHelper().put(PropertyKeys.stretchH, value);
	}

	/**
	 * The handsontable stretchH value.
	 * <p>
	 * @return the stretchH value
	 */
	public String getStretchH() {
		Object result = getStateHelper().eval(PropertyKeys.stretchH, null);
		if (result == null)
			return null;
		return result.toString();
	}

	/**
	 * Update the emptyMessage value for the component
	 * <p>
	 * @param value
	 */
	public void setEmptyMessage(String value) {
		getStateHelper().put(PropertyKeys.emptyMessage, value);
	}

	/**
	 * The emptyMessage value.
	 * <p>
	 * @return the emptyMessage value
	 */
	public String getEmptyMessage() {
		Object result = getStateHelper().eval(PropertyKeys.emptyMessage, null);
		if (result == null)
			return null;
		return result.toString();
	}

	/**
	 * Update the current row style class
	 * <p>
	 * @param styleClass
	 */
	public void setCurrentColClass(String styleClass) {
		getStateHelper().put(PropertyKeys.currentColClass, styleClass);
	}

	/**
	 * The col style class to use for the selected col
	 * <p>
	 * @param styleClass
	 */
	public String getCurrentColClass() {
		Object result = getStateHelper().eval(PropertyKeys.currentColClass, null);
		if (result == null)
			return null;
		return result.toString();
	}

	/**
	 * Update the current row style class
	 * <p>
	 * @param styleClass
	 */
	public void setCurrentRowClass(String styleClass) {
		getStateHelper().put(PropertyKeys.currentRowClass, styleClass);
	}

	/**
	 * The row style class to use for the selected row
	 * <p>
	 * @param styleClass
	 */
	public String getCurrentRowClass() {
		Object result = getStateHelper().eval(PropertyKeys.currentRowClass, null);
		if (result == null)
			return null;
		return result.toString();
	}

	/**
	 * Update the current row style class to apply to the row
	 * <p>
	 * @param styleClass
	 */
	public void setRowStyleClass(String styleClass) {
		getStateHelper().put(PropertyKeys.rowStyleClass, styleClass);
	}

	/**
	 * The row style class to apply to each row
	 * <p>
	 * @param styleClass
	 */
	public String getRowStyleClass() {
		Object result = getStateHelper().eval(PropertyKeys.rowStyleClass, null);
		if (result == null)
			return null;
		return result.toString();
	}

	/**
	 * Update the ShowColumnheaders
	 * <p>
	 * @param value
	 */
	public void setShowColumnHeaders(Boolean value) {
		getStateHelper().put(PropertyKeys.showColumnHeaders, value);
	}

	/**
	 * Flag indicating whether or not column headers are visible
	 * <p>
	 * @return
	 */
	public Boolean isShowColumnHeaders() {
		return Boolean.valueOf(getStateHelper().eval(PropertyKeys.showColumnHeaders, true).toString());
	}

	/**
	 * Update the ShowRowHeaders value.
	 * <p>
	 * @param value
	 */
	public void setShowRowHeaders(Boolean value) {
		getStateHelper().put(PropertyKeys.showRowHeaders, value);
	}

	/**
	 * The ShowRowHeaders flag
	 * <p>
	 * @return
	 */
	public Boolean isShowRowHeaders() {
		return Boolean.valueOf(getStateHelper().eval(PropertyKeys.showRowHeaders, true).toString());
	}

	/**
	 * The list of child columns.
	 * <p>
	 * @return
	 */
	public List<Column> getColumns() {
		if (columns == null) {
			columns = new ArrayList<Column>();
			getColumns(this);
		}
		return columns;
	}

	/**
	 * Grabs the UIColumn children for the parent specified.
	 * @param parent
	 */
	private void getColumns(UIComponent parent) {
		for (UIComponent child : parent.getChildren())
			if (child instanceof Column)
				columns.add((Column) child);
	}

	/**
	 * Updates the list of child columns.
	 * <p>
	 * @param columns
	 */
	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	/**
	 * Updates the fixed row count.
	 * <p>
	 * @param value
	 */
	public void setFixedRows(Integer value) {
		getStateHelper().put(PropertyKeys.fixedRows, value);
	}

	/**
	 * The fixed row count
	 * @return
	 */
	public Integer getFixedRows() {
		Object result = getStateHelper().eval(PropertyKeys.fixedRows, null);
		if (result == null)
			return null;
		return Integer.valueOf(result.toString());
	}

	/**
	 * Updates the fixed columns count.
	 * <p>
	 * @param value
	 */
	public void setFixedCols(Integer value) {
		getStateHelper().put(PropertyKeys.fixedCols, value);
	}

	/**
	 * The fixed column count.
	 * <p>
	 * @return
	 */
	public Integer getFixedCols() {
		Object result = getStateHelper().eval(PropertyKeys.fixedCols, null);
		if (result == null)
			return null;
		return Integer.valueOf(result.toString());
	}

	/**
	 * The list of bad updates
	 * @return
	 */
	public List<BadUpdate> getBadUpdates() {
		if (badUpdates == null)
			badUpdates = new ArrayList<BadUpdate>();
		return badUpdates;
	}

	/**
	 * Resets the submitted values
	 */
	public void resetSubmitted() {
		this.submittedValues.clear();
	}

	/**
	 * Resets the sorting to the originally specified values (if any)
	 */
	public void resetSort() {
		ValueExpression origSortBy = (ValueExpression) getStateHelper().get(PropertyKeys.origSortBy);
		if (origSortBy != null)
			this.setSortByValueExpression(origSortBy);

		String origSortOrder = (String) getStateHelper().get(PropertyKeys.origSortOrder);
		if (origSortOrder != null)
			setSortOrder(origSortOrder);
	}

	/**
	 * Resets all filters, sorting and submitted values.
	 */
	public void reset() {
		resetSubmitted();
		resetSort();
		localValues.clear();
		getBadUpdates().clear();
		for (Column c : getColumns())
			c.setFilterValue(null);
	}

	/**
	 * Updates a submitted value.
	 * <p>
	 * @param row
	 * @param col
	 * @param value
	 */
	public void setSubmittedValue(FacesContext context, int row, int col, String value) {
		// need to find row key
		this.setRowIndex(context, row);
		submittedValues.put(new RowColIndex(this.getRowKeyValue(context), col), value);
	}

	/**
	 * Retrieves the submitted value for the row and col.
	 * <p>
	 * @param row
	 * @param col
	 * @return
	 */
	public String getSubmittedValue(Object rowKey, int col) {
		return submittedValues.get(new RowColIndex(rowKey, col));
	}

	/**
	 * Updates a local value.
	 * <p>
	 * @param rowKey
	 * @param col
	 * @param value
	 */
	public void setLocalValue(Object rowKey, int col, Object value) {
		localValues.put(new RowColIndex(rowKey, col), value);
	}

	/**
	 * Retrieves the submitted value for the rowKey and col.
	 * <p>
	 * @param row
	 * @param col
	 * @return
	 */
	public Object getLocalValue(Object rowKey, int col) {
		return localValues.get(new RowColIndex(rowKey, col));
	}

	/**
	 * The current row index for iterations over the List
	 * @return
	 */
	public int getRowIndex() {
		return rowIndex;
	}

	/**
	 * Updates the row index for iterations over the list. The var value will be
	 * update
	 * <p>
	 * @param context
	 *            the FacesContext against which to the row var is set. Passed
	 *            for performance
	 * @param rowIndex
	 */
	public void setRowIndex(FacesContext context, int rowIndex) {
		if (this.rowIndex != rowIndex) {
			this.rowIndex = rowIndex;

			if (context == null)
				return;

			if (rowIndex < 0) {
				context.getExternalContext().getRequestMap().remove(getVar());
			} else {
				final List<Object> values = this.getSortedValues();
				if (values == null)
					return;

				Object value = null;
				if (rowIndex < values.size())
					value = values.get(rowIndex);
				context.getExternalContext().getRequestMap().put(getVar(), value);
			}
		}
	}

	/**
	 * Gets the object value of the row and col specified. If a local value
	 * exists, that is returned, otherwise the actual value is return.
	 * <p>
	 * @param context
	 * @param rowKey
	 * @param col
	 * @return
	 */
	public Object getValueForCell(FacesContext context, Object rowKey, int col) {
		// if we have a local value, use it
		// note: can't check for null, as null may be the submitted value
		RowColIndex index = new RowColIndex(rowKey, col);
		if (localValues.containsKey(index))
			return localValues.get(index);

		RowMap map = rowMap.get(rowKey);
		setRowIndex(context, map.sortedIndex);
		final Column column = getColumns().get(col);
		return column.getValueExpression("value").getValue(context.getELContext());
	}

	/**
	 * Gets the render string for the value the given cell. Applys the available
	 * converters to convert the value.
	 * <p>
	 * @param context
	 * @param rowKey
	 * @param col
	 * @return
	 */
	public String getRenderValueForCell(FacesContext context, Object rowKey, int col) {

		// if we have a submitted value still, use it
		// note: can't check for null, as null may be the submitted value
		RowColIndex index = new RowColIndex(rowKey, col);
		if (submittedValues.containsKey(index))
			return submittedValues.get(index);

		Object value = getValueForCell(context, rowKey, col);
		if (value == null)
			return null;

		final Column column = getColumns().get(col);
		Converter converter = ComponentUtils.getConverter(context, column);
		if (converter == null)
			return value.toString();
		else
			return converter.getAsString(context, this, value);
	}

	/**
	 * The currently selected column.
	 * <p>
	 * @return
	 */
	public Integer getSelectedColumn() {
		Object result = getStateHelper().eval(PropertyKeys.selectedColumn);
		if (result == null)
			return null;
		return Integer.valueOf(result.toString());
	}

	/**
	 * Updates the selected column.
	 * <p>
	 * @param col
	 */
	public void setSelectedColumn(Integer col) {
		getStateHelper().put(PropertyKeys.selectedColumn, col);
	}

	/**
	 * The currently selected column.
	 * <p>
	 * @return
	 */
	public Integer getSelectedLastColumn() {
		Object result = getStateHelper().eval(PropertyKeys.selectedLastColumn);
		if (result == null)
			return null;
		return Integer.valueOf(result.toString());
	}

	/**
	 * Updates the selected column.
	 * <p>
	 * @param col
	 */
	public void setSelectedLastColumn(Integer col) {
		getStateHelper().put(PropertyKeys.selectedLastColumn, col);
	}

	/**
	 * The currently selected row.
	 * <p>
	 * @return
	 */
	public Integer getSelectedRow() {
		Object result = getStateHelper().eval(PropertyKeys.selectedRow);
		if (result == null)
			return null;
		return Integer.valueOf(result.toString());
	}

	/**
	 * The currently selected row.
	 * <p>
	 * @return
	 */
	public Integer getSelectedLastRow() {
		Object result = getStateHelper().eval(PropertyKeys.selectedLastRow);
		if (result == null)
			return null;
		return Integer.valueOf(result.toString());
	}

	/**
	 * Updates the selected row.
	 * <p>
	 * @param row
	 */
	public void setSelectedRow(Integer row) {
		getStateHelper().put(PropertyKeys.selectedRow, row);
	}

	/**
	 * Updates the selected row.
	 * <p>
	 * @param row
	 */
	public void setSelectedLastRow(Integer row) {
		getStateHelper().put(PropertyKeys.selectedLastRow, row);
	}

	/**
	 * The width of the sheet in pixels
	 * <p>
	 * @return
	 */
	public Integer getWidth() {
		Object result = getStateHelper().eval(PropertyKeys.width);
		if (result == null)
			return null;
		// this will handle any type so long as its convertable to integer
		return Integer.valueOf(result.toString());
	}

	/**
	 * Updates the width
	 * <p>
	 * @param row
	 */
	public void setWidth(Integer value) {
		getStateHelper().put(PropertyKeys.width, value);
	}

	/**
	 * The height of the sheet. Note this is applied to the inner div which is
	 * why it is recommend you use this property instead of a style class.
	 * <p>
	 * @return
	 */
	public Integer getHeight() {
		Object result = getStateHelper().eval(PropertyKeys.height);
		if (result == null)
			return null;
		// this will handle any type so long as its convertable to integer
		return Integer.valueOf(result.toString());
	}

	/**
	 * Updates the height
	 * <p>
	 * @param row
	 */
	public void setHeight(Integer value) {
		getStateHelper().put(PropertyKeys.height, value);
	}

	/**
	 * <p>
	 * Return the value of the Sheet. This value must be a java.util.List value
	 * at this time.
	 * </p>
	 */
	@Override
	public Object getValue() {
		return getStateHelper().eval(PropertyKeys.value);
	}

	/**
	 * The sorted list of values.
	 * <p>
	 * @return
	 */
	public List<Object> getSortedValues() {
		if (sortedList == null)
			sortAndFilter();
		return sortedList;
	}

	/**
	 * Gets the rendered col index of the column corresponding to the current
	 * sortBy. This is used to keep track of the current sort column in the
	 * page.
	 * <p>
	 * @return
	 */
	public int getSortColRenderIndex() {
		ValueExpression veSortBy = getValueExpression(PropertyKeys.sortBy.name());
		if (veSortBy == null)
			return -1;

		final String sortByExp = veSortBy.getExpressionString();
		int colIdx = 0;
		for (Column column : getColumns()) {
			if (!column.isRendered())
				continue;

			ValueExpression veCol = column.getValueExpression(PropertyKeys.sortBy.name());
			if (veCol != null) {
				if (veCol.getExpressionString().equals(sortByExp))
					return colIdx;
			}
			colIdx++;
		}
		return -1;
	}

	/**
	 * Evaluates the specified item value against the column filters and if they
	 * match, returns true, otherwise false.
	 * <p>
	 * @param obj
	 * @return
	 */
	protected boolean matchesFilter(Object obj) {
		for (Column col : getColumns()) {
			String filterValue = col.getFilterValue();
			if (StringUtils.isEmpty(filterValue))
				continue;

			Object filterBy = col.getFilterBy();
			// if we have a filter, but no value in the row, no match
			if (filterBy == null)
				return false;

			// case-insensitive
			String compareA = filterBy.toString().toLowerCase();
			String compareB = filterValue.toLowerCase();

			// TODO need to support match modes
			if (!compareA.contains(compareB))
				return false;
		}
		return true;
	}

	/**
	 * Sorts and filters the data
	 */
	@SuppressWarnings("unchecked")
	public void sortAndFilter() {
		sortedList = new ArrayList<Object>();
		rowMap = new HashMap<Object, RowMap>();

		Collection<?> values = (Collection<?>) getValue();
		if (values == null || values.isEmpty())
			return;

		boolean filters = false;
		for (Column col : getColumns())
			if (StringUtils.isNotEmpty(col.getFilterValue())) {
				filters = true;
				break;
			}

		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, Object> requestMap = context.getExternalContext().getRequestMap();

		if (filters) {
			// iterate and add those matching the filters
			String var = getVar();
			for (Object obj : values) {
				requestMap.put(var, obj);
				try {
					if (matchesFilter(obj))
						sortedList.add(obj);
				} finally {
					requestMap.remove(var);
				}
			}
		} else
			sortedList.addAll(values);

		ValueExpression veSortBy = this.getValueExpression(PropertyKeys.sortBy.name());
		if (veSortBy != null)
			Collections.sort(sortedList, new BeanPropertyComparator(veSortBy, getVar(), convertSortOrder(), null));

		reMapRows();
	}

	/**
	 * Remaps the row keys to the sorted and filtered list.
	 */
	protected void reMapRows() {
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, Object> requestMap = context.getExternalContext().getRequestMap();

		for (int i = 0; i < sortedList.size(); i++) {
			Object obj = sortedList.get(i);
			String var = getVar();
			requestMap.put(var, obj);
			try {
				RowMap map = new RowMap();
				map.sortedIndex = i;
				map.value = obj;
				rowMap.put(getRowKeyValue(context), map);
			} finally {
				requestMap.remove(var);
			}
		}
	}

	/**
	 * Gets the rowKey for the current row
	 * <p>
	 * @param context
	 *            the faces context
	 * @return a row key value or null if the expression is not set
	 */
	protected Object getRowKeyValue(FacesContext context) {
		ValueExpression veRowKey = getValueExpression(PropertyKeys.rowKey.name());
		if (veRowKey == null)
			throw new RuntimeException("RowKey required on sheet!");
		Object value = veRowKey.getValue(context.getELContext());
		if (value == null)
			throw new RuntimeException("RowKey must resolve to non-null valkue for updates to work properly");
		return value;
	}

	/**
	 * Convert to PF SortOrder enum since we are leveraging PF sorting code.
	 * <p>
	 * @return
	 */
	protected SortOrder convertSortOrder() {
		String sortOrder = getSortOrder();
		if (sortOrder == null)
			return SortOrder.UNSORTED;
		else {
			SortOrder result = SortOrder.valueOf(sortOrder.toUpperCase(Locale.ENGLISH));
			return result;
		}
	}

	/**
	 * <p>
	 * Set the value of the <code>Sheet</code>. This value must be a
	 * java.util.List at this time.
	 * </p>
	 * @param value
	 *            the new value
	 */
	@Override
	public void setValue(Object value) {
		getStateHelper().put(PropertyKeys.value, value);
	}

	/**
	 * <p>
	 * Return the request-scope attribute under which the data object for the
	 * current row will be exposed when iterating. This property is
	 * <strong>not</strong> enabled for value binding expressions.
	 * </p>
	 */
	public String getVar() {
		// must be a string literal (no eval)
		return (String) getStateHelper().get(PropertyKeys.var);
	}

	/**
	 * <p>
	 * Set the request-scope attribute under which the data object for the
	 * current row wil be exposed when iterating.
	 * </p>
	 * @param var
	 *            The new request-scope attribute name
	 */
	public void setVar(String var) {
		getStateHelper().put(PropertyKeys.var, var);
	}

	/**
	 * The current sortBy value expression in use.
	 * @return
	 */
	public ValueExpression getSortByValueExpression() {
		ValueExpression veSortBy = getValueExpression(PropertyKeys.sortBy.name());
		return veSortBy;
	}

	/**
	 * Update the sort field
	 * @param sortBy
	 */
	public void setSortByValueExpression(ValueExpression sortBy) {
		// when updating, make sure we store off the original so it may be
		// restored
		ValueExpression orig = (ValueExpression) getStateHelper().get(PropertyKeys.origSortBy);
		if (orig == null) {
			getStateHelper().put(PropertyKeys.origSortBy, getSortByValueExpression());
		}
		setValueExpression(PropertyKeys.sortBy.name(), sortBy);
	}

	/**
	 * The sort direction
	 * @return
	 */
	public String getSortOrder() {
		// if we have a toggled sort in our state, use it
		String result = (String) getStateHelper().eval(PropertyKeys.sortOrder, SortOrder.ASCENDING.toString());
		return result;
	}

	/**
	 * Update the sort direction
	 * @param sortOrder
	 */
	public void setSortOrder(java.lang.String sortOrder) {
		// when updating, make sure we store off the original so it may be
		// restored
		String orig = (String) getStateHelper().get(PropertyKeys.origSortOrder);
		if (orig == null)
			// do not call getSortOrder as it defaults to ascending, we want
			// null
			// if this is the first call and there is no previous value.
			getStateHelper().put(PropertyKeys.origSortOrder, getStateHelper().eval(PropertyKeys.sortOrder));
		getStateHelper().put(PropertyKeys.sortOrder, sortOrder);
	}

	/**
	 * The error message to display when the sheet is in error.
	 * <p>
	 * @return
	 */
	public String getErrorMessage() {
		Object result = getStateHelper().eval(PropertyKeys.errorMessage);
		if (result == null)
			return null;
		return result.toString();
	}

	/**
	 * Updates the errorMessage value.
	 * @param value
	 */
	public void setErrorMessage(String value) {
		getStateHelper().put(PropertyKeys.errorMessage, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.component.UIInput#processValidators(javax.faces.context.
	 * FacesContext)
	 */
	@Override
	public void processValidators(FacesContext context) {
		super.processValidators(context);
	}

	/**
	 * Converts each submitted value into a local value and stores it back in
	 * the hash. If all values convert without error, then the component is
	 * valid, and we can proceed to the processUpdates.
	 */
	@Override
	public void validate(FacesContext context) {
		// iterate over submitted values and attempt to convert to the proper
		// data type. For successful values, remove from submitted and add to
		// local values map. for failures, add a conversion message and leave in
		// the submitted state
		Iterator<Entry<RowColIndex, String>> entries = submittedValues.entrySet().iterator();
		boolean hadBadUpdates = !getBadUpdates().isEmpty();
		getBadUpdates().clear();
		while (entries.hasNext()) {
			final Entry<RowColIndex, String> entry = entries.next();
			final Column column = getColumns().get(entry.getKey().colIndex);
			final String newValue = entry.getValue();
			final Object rowKey = entry.getKey().getRowKey();
			final int col = entry.getKey().getColIndex();
			final RowMap map = rowMap.get(rowKey);
			this.setRowIndex(context, map.sortedIndex);

			// attempt to convert new value from string to correct object type
			// based on column converter. Use PF util as helper
			Converter converter = ComponentUtils.getConverter(context, column);

			// assume string value if converter not found
			Object newValueObj = newValue;
			if (converter != null)
				try {
					newValueObj = converter.getAsObject(context, this, newValue);
				} catch (ConverterException e) {
					// add offending cell to list of bad updates
					// and to a stringbuffer for error messages (so we have one
					// message for the component)
					setValid(false);
					FacesMessage message = e.getFacesMessage();
					if (message == null) {
						message = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage());
					}
					context.addMessage(this.getClientId(context), message);

					String messageText = message.getDetail();
					this.getBadUpdates()
							.add(new BadUpdate(getRowKeyValue(context), col, column, newValue, messageText));
					continue;
				}
			// value is fine, no further validations (again, not to be confused
			// with validators. until we have a "required" or something like
			// that, nothing else to do).
			setLocalValue(rowKey, col, newValueObj);

			// process validators on column
			column.setValue(newValueObj);
			try {
				column.validate(context);
			} finally {
				column.resetValue();
			}

			entries.remove();
		}
		this.setRowIndex(context, -1);

		final boolean newBadUpdates = !getBadUpdates().isEmpty();
		String errorMessage = this.getErrorMessage();

		if (hadBadUpdates || newBadUpdates) {
			// update the bad data var if partial request
			if (context.getPartialViewContext().isPartialRequest()) {
				this.sortAndFilter();
				this.renderBadUpdateScript(context);
			}
		}

		if (newBadUpdates && errorMessage != null) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage, errorMessage);
			context.addMessage(null, message);
		}
	}

	/**
	 * Override to update model with local values. Note that this is where
	 * things can be fragile in that we can successfully update some values and
	 * fail on others. There is no clean way to roll back the updates, but we
	 * also need to fail processing.
	 * <p>
	 * TODO consider keeping old values as we update (need for event anyhow) and
	 * if there is a failure attempt to roll back by updating successful model
	 * updates with the old value. This may not all be necessary.
	 */
	@Override
	public void updateModel(FacesContext context) {
		Iterator<Entry<RowColIndex, Object>> entries = localValues.entrySet().iterator();
		// Keep track of the dirtied rows for ajax callbacks so we can send
		// updates on what was touched
		HashSet<Object> dirtyRows = new HashSet<Object>();
		while (entries.hasNext()) {
			final Entry<RowColIndex, Object> entry = entries.next();

			final Object newValue = entry.getValue();
			final Object rowKey = entry.getKey().getRowKey();
			final int col = entry.getKey().getColIndex();
			final Column column = getColumns().get(col);
			final RowMap map = rowMap.get(rowKey);
			this.setRowIndex(context, map.sortedIndex);

			System.out.println("Local key=" + rowKey + " and sortedRow is " + map.sortedIndex);

			ValueExpression ve = column.getValueExpression(PropertyKeys.value.name());
			ELContext elContext = context.getELContext();
			Object oldValue = ve.getValue(elContext);
			ve.setValue(elContext, newValue);
			entries.remove();
			appendUpdateEvent(map.sortedIndex, col, map.value, oldValue, newValue);
			dirtyRows.add(rowKey);
		}
		setLocalValueSet(false);
		setRowIndex(context, -1);

		this.sortAndFilter();

		if (context.getPartialViewContext().isPartialRequest())
			this.renderRowUpdateScript(context, dirtyRows);
	}

	/**
	 * Saves the state of the submitted and local values and the bad updates.
	 */
	@Override
	public Object saveState(FacesContext context) {
		Object values[] = new Object[7];
		values[0] = super.saveState(context);
		values[1] = submittedValues;
		values[2] = localValues;
		values[3] = badUpdates;
		values[4] = columnMapping;
		values[5] = sortedList;
		values[6] = rowMap;

		return values;
	}

	/**
	 * Restores the state for the submitted, local and bad values.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void restoreState(FacesContext context, Object state) {
		if (state == null)
			return;

		Object values[] = (Object[]) state;
		super.restoreState(context, values[0]);
		Object restoredSubmittedValues = values[1];
		Object restoredLocalValues = values[2];
		Object restoredBadUpdates = values[3];
		Object restoredColMappings = values[4];
		Object restoredSortedList = values[5];
		Object restoredRowMap = values[6];

		if (restoredSubmittedValues == null)
			submittedValues.clear();
		else
			submittedValues = (Map<RowColIndex, String>) restoredSubmittedValues;

		if (restoredLocalValues == null)
			localValues.clear();
		else
			localValues = (Map<RowColIndex, Object>) restoredLocalValues;

		if (restoredBadUpdates == null)
			badUpdates.clear();
		else
			badUpdates = (List<BadUpdate>) restoredBadUpdates;

		if (restoredColMappings == null)
			columnMapping = null;
		else
			columnMapping = (Map<Integer, Integer>) restoredColMappings;

		if (restoredSortedList == null)
			sortedList = null;
		else
			sortedList = (List<Object>) restoredSortedList;

		if (restoredRowMap == null)
			rowMap = null;
		else
			rowMap = (Map<Object, RowMap>) restoredRowMap;
	}

	/**
	 * The selection value.
	 * <p>
	 * @return the selection
	 */
	public String getSelection() {
		return selection;
	}

	/**
	 * Updates the selection value.
	 * <p>
	 * @param selection
	 *            the selection to set
	 */
	public void setSelection(String selection) {
		this.selection = selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.component.EditableValueHolder#getSubmittedValue()
	 */
	@Override
	public Object getSubmittedValue() {
		if (this.submittedValues.isEmpty())
			return null;
		else
			return (this.submittedValues);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.faces.component.EditableValueHolder#setSubmittedValue(java.lang
	 * .Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setSubmittedValue(Object submittedValue) {
		if (submittedValue == null)
			submittedValues.clear();
		else
			submittedValues = (Map<RowColIndex, String>) submittedValue;

	}

	/**
	 * A list of updates from the last submission or ajax event.
	 * <p>
	 * @return the editEvent
	 */
	public List<SheetUpdate> getUpdates() {
		return updates;
	}

	/**
	 * Appends an update event
	 * <p>
	 * @param rowIndex
	 * @param colIndex
	 * @param rowData
	 * @param oldValue
	 * @param newValue
	 */
	protected void appendUpdateEvent(int rowIndex, int colIndex, Object rowData, Object oldValue, Object newValue) {
		updates.add(new SheetUpdate(rowIndex, colIndex, rowData, oldValue, newValue));
	}

	/**
	 * Returns true if any of the columns contain conditional styling.
	 * <p>
	 * @return
	 */
	public boolean isHasStyledCells() {
		for (Column column : getColumns())
			if (column.getStyleClass() != null)
				return true;
		return false;
	}

	/**
	 * Maps the rendered column index to the real column index.
	 * <p>
	 * @param renderIdx
	 *            the rendered index
	 * @return the mapped index
	 */
	public int getMappedColumn(int renderIdx) {
		if (columnMapping == null) {
			return renderIdx;
		} else {
			Integer result = columnMapping.get(renderIdx);
			if (result == null)
				throw new IllegalArgumentException("Invalid index " + renderIdx);
			return result;
		}
	}

	/**
	 * Provides the render column index based on the real index
	 * @param realIdx
	 * @return
	 */
	public int getRenderIndexFromRealIdx(int realIdx) {
		if (columnMapping == null) {
			return realIdx;
		}

		for (Entry<Integer, Integer> entry : columnMapping.entrySet())
			if (entry.getValue().equals(realIdx))
				return entry.getKey();

		return realIdx;
	}

	/**
	 * Updates the column mappings based on the rendered attribute
	 */
	public void updateColumnMappings() {
		columnMapping = new HashMap<Integer, Integer>();
		int realIdx = 0;
		int renderIdx = 0;
		for (Column column : getColumns()) {
			if (column.isRendered()) {
				columnMapping.put(renderIdx, realIdx);
				renderIdx++;
			}
			realIdx++;
		}
	}

	/**
	 * The number of rows in the value list.
	 * <p>
	 * @return
	 */
	public int getRowCount() {
		List<Object> values = getSortedValues();
		if (values == null)
			return 0;
		return values.size();
	}

	/**
	 * The focusId value.
	 * <p>
	 * @return the focusId
	 */
	public String getFocusId() {
		return focusId;
	}

	/**
	 * Updates the focusId value.
	 * <p>
	 * @param focusId
	 *            the focusId to set
	 */
	public void setFocusId(String focusId) {
		this.focusId = focusId;
	}

	/**
	 * Invoke this method to commit the most recent set of ajax updates and
	 * restart the tracking of changes. Use this when you have processes the
	 * updates to the model and are confident that any changes made to this
	 * point can be cleared (likely because you have persisted those changes).
	 */
	public void commitUpdates() {
		resetSubmitted();
		FacesContext context = FacesContext.getCurrentInstance();
		if (context.getPartialViewContext().isPartialRequest()) {
			StringBuffer eval = new StringBuffer();
			String jQueryId = this.getClientId().replace(":", "\\\\:");
			String jsDeltaVar = this.getClientId().replace(":", "_") + "_delta";

			eval.append("$('#");
			eval.append(jQueryId);
			eval.append("_input').val('');");
			eval.append(jsDeltaVar);
			eval.append("={};");
			RequestContext.getCurrentInstance().getScriptsToExecute().add(eval.toString());
		}

	}

	/**
	 * Generates the bad data var value for this sheet.
	 * <p>
	 * @param sheet
	 * @param badDataVar
	 * @return
	 */
	public String getBadDataValue() {
		VarBuilder vb = new VarBuilder(null, true);
		for (BadUpdate badUpdate : getBadUpdates()) {
			final Object rowKey = badUpdate.getBadRowKey();
			final int col = getRenderIndexFromRealIdx(badUpdate.getBadColIndex());
			RowMap map = rowMap.get(rowKey);
			System.out.println("RowMap is " + map.sortedIndex + " for key " + rowKey);
			vb.appendRowColProperty(map.sortedIndex, col, badUpdate.getBadMessage().replace("'", "&apos;"), true);
		}
		return vb.closeVar().toString();
	}

	/**
	 * Adds eval scripts to the ajax response to update the rows dirtied by the
	 * most recent successful update request.
	 * <p>
	 * @param context
	 *            the FacesContext
	 * @param dirtyRows
	 *            the set of dirty rows
	 */
	protected void renderRowUpdateScript(FacesContext context, Set<Object> dirtyRows) {
		String jsVar = this.resolveWidgetVar();
		StringBuilder eval = new StringBuilder();

		for (Object rowKey : dirtyRows) {
			RowMap map = this.rowMap.get(rowKey);
			setRowIndex(context, map.sortedIndex);
			// data is array of array of data
			VarBuilder vbRow = new VarBuilder(null, false);
			for (int col = 0; col < getColumns().size(); col++) {
				final Column column = getColumns().get(col);
				if (!column.isRendered())
					continue;

				// render data value
				String value = getRenderValueForCell(context, rowKey, col);
				vbRow.appendArrayValue(value, true);
			}
			eval.append(jsVar);
			eval.append(".cfg.data[");
			eval.append(Integer.toString(map.sortedIndex));
			eval.append("]=");
			eval.append(vbRow.closeVar().toString());
			eval.append(";");
		}
		eval.append(jsVar);
		eval.append(".ht.render();");
		RequestContext.getCurrentInstance().getScriptsToExecute().add(eval.toString());
	}

	/**
	 * Adds eval scripts to update the bad data array in the sheet to render
	 * valdiation failures produced by the most recent ajax update attempt.
	 * <p>
	 * @param context
	 *            the FacesContext
	 */
	protected void renderBadUpdateScript(FacesContext context) {
		String widgetVar = this.resolveWidgetVar();
		String badDataVar = this.getBadDataValue();
		StringBuffer sb = new StringBuffer(widgetVar);
		sb.append(".cfg.errors=");
		sb.append(badDataVar);
		sb.append(";");
		sb.append(widgetVar);
		sb.append(".ht.render();");
		RequestContext.getCurrentInstance().getScriptsToExecute().add(sb.toString());

		sb = new StringBuffer();
		sb.append(widgetVar);
		sb.append(".sheetDiv.removeClass('ui-state-error')");
		if (!getBadUpdates().isEmpty())
			sb.append(".addClass('ui-state-error')");
		RequestContext.getCurrentInstance().getScriptsToExecute().add(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.primefaces.component.api.Widget#resolveWidgetVar()
	 */
	@Override
	public String resolveWidgetVar() {
		FacesContext context = FacesContext.getCurrentInstance();
		String userWidgetVar = (String) getAttributes().get("widgetVar");
		if (userWidgetVar != null)
			return userWidgetVar;
		else
			return "widget_" + getClientId(context).replaceAll("-|" + UINamingContainer.getSeparatorChar(context), "_");
	}

	/*
	 * Private class used as a key for row,col maps.
	 */
	private class RowColIndex implements Serializable {

		private static final long serialVersionUID = 1L;

		private final Object rowKey;
		private final Integer colIndex;

		/**
		 * Constructs an instance of RowColIndex for the row and column
		 * specified.
		 * <p>
		 * @param row
		 *            the row represented by this index
		 * @param col
		 *            the column respresented by this index
		 */
		public RowColIndex(Object rowKey, Integer col) {
			this.rowKey = rowKey;
			this.colIndex = col;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof RowColIndex))
				return false;
			RowColIndex castOther = (RowColIndex) other;
			return new EqualsBuilder().append(rowKey, castOther.rowKey).append(colIndex, castOther.colIndex)
					.isEquals();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(rowKey).append(colIndex).toHashCode();
		}

		/**
		 * The rowIndex value.
		 * <p>
		 * @return the rowIndex
		 */
		public Object getRowKey() {
			return rowKey;
		}

		/**
		 * The colIndex value.
		 * <p>
		 * @return the colIndex
		 */
		public Integer getColIndex() {
			return colIndex;
		}

	}

	/*
	 * Private class used to map a row key to its object and sorted row index
	 */
	private class RowMap implements Serializable {
		private static final long serialVersionUID = 1L;
		Object value;
		int sortedIndex;
	}
}
