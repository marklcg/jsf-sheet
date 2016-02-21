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

import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.Collection;

/**
 * JSF Component used to represent a column in the Sheet component.
 *
 * @author <a href="mailto:mlassiter@lassitercg.com">Mark Lassiter</a>
 */
@FacesComponent(value = Column.COMPONENTTYPE)
public class Column extends UIInput implements ClientBehaviorHolder {

    private static final String MESSAGE_REQUIRED = "A valid value for this column is required.";
    public static final String FAMILY = "com.lassitercg.faces.components";
    public static final String COMPONENTTYPE = "com.lassitercg.faces.components.column";

    /**
     * Properties that are tracked by state saving.
     */
    enum PropertyKeys {
        /**
         * The header text to display for the column
         */
        headerText,

        /**
         * Flag indicating whether or not the column is read only
         */
        readonly,

        /**
         * Flag indicating whether or not a cell is read only
         */
        readonlyCell,

        /**
         * The width of the column
         */
        colWidth,

        /**
         * The type of the column (text, numeric, etc)
         */
        colType,

        /**
         * the style class for the cell
         */
        styleClass,

        /**
         * Filter by value expression
         */
        filterBy,

        /**
         * Filter options
         */
        filterOptions,

        /**
         * The submitted filtered value
         */
        filterValue
    }

    private Object localValue;

    /**
     * Sheet reference. Updated on decode.
     */
    private Sheet sheet;

    /*
     * (non-Javadoc)
     *
     * @see javax.faces.component.UIComponent#getFamily()
     */
    @Override
    public String getFamily() {
        return FAMILY;
    }

    /**
     * Updates the fixed columns count.
     *
     * @param value
     */
    public void setHeaderText(String value) {
        getStateHelper().put(PropertyKeys.headerText, value);
    }

    /**
     * The fixed column count.
     *
     * @return
     */
    public String getHeaderText() {
        Object result = getStateHelper().eval(PropertyKeys.headerText, null);
        if (result == null)
            return null;
        return result.toString();
    }

    /**
     * Updates the readOnly property
     *
     * @param value
     */
    public void setReadonly(Boolean value) {
        getStateHelper().put(PropertyKeys.readonly, value);
    }

    /**
     * Flag indicating whether this column is read only.
     *
     * @return true if read only, otherwise false
     */
    public Boolean isReadonly() {
        return Boolean.valueOf(getStateHelper().eval(PropertyKeys.readonly, Boolean.FALSE).toString());
    }

    /**
     * Updates the readOnly property of the cell
     *
     * @param value
     */
    public void setReadonlyCell(Boolean value) {
        getStateHelper().put(PropertyKeys.readonlyCell, value);
    }

    /**
     * Flag indicating whether this cell is read only. the var reference will be
     * available.
     *
     * @return true if read only, otherwise false
     */
    public Boolean isReadonlyCell() {
        return Boolean.valueOf(getStateHelper().eval(PropertyKeys.readonlyCell, Boolean.FALSE).toString());
    }

    /**
     * Updates the column width
     *
     * @param value
     */
    public void setColWidth(Integer value) {
        getStateHelper().put(PropertyKeys.colWidth, value);
    }

    /**
     * The column width
     *
     * @return
     */
    public Integer getColWidth() {
        Object result = getStateHelper().eval(PropertyKeys.colWidth, null);
        if (result == null)
            return null;
        return Integer.valueOf(result.toString());
    }

    /**
     * Updates the column type. Possible values are: text, numeric, date,
     * checkbox, autocomplete, handsontable.
     *
     * @param value
     */
    public void setColType(String value) {
        getStateHelper().put(PropertyKeys.colType, value);
    }

    /**
     * the Handsontable column type.
     * <p>
     * TODO make this an enum and limit choices
     *
     * @return
     */
    public String getColType() {
        return getStateHelper().eval(PropertyKeys.colType, "text").toString();
    }

    /**
     * Update the style class for the cell.
     *
     * @param value
     */
    public void setStyleClass(String value) {
        getStateHelper().put(PropertyKeys.styleClass, value);
    }

    /**
     * The style class for the cell.
     *
     * @return
     */
    public String getStyleClass() {
        Object result = getStateHelper().eval(PropertyKeys.styleClass, null);
        if (result == null)
            return null;
        return result.toString();
    }

    /**
     * The filterBy expression
     *
     * @return
     */
    public Object getFilterBy() {
        return getStateHelper().eval(PropertyKeys.filterBy, null);
    }

    /**
     * Update the filter by field
     *
     * @param filterBy
     */
    public void setFilterBy(Object filterBy) {
        getStateHelper().put(PropertyKeys.filterBy, filterBy);
    }

    /**
     * The filter value submitted by the user
     *
     * @return
     */
    public String getFilterValue() {
        return (String) getStateHelper().get(PropertyKeys.filterValue);
    }

    /**
     * Update the filter value for this column
     *
     * @param filterValue
     */
    public void setFilterValue(String filterValue) {
        getStateHelper().put(PropertyKeys.filterValue, filterValue);
    }

    /**
     * The filterOptions expression
     *
     * @return
     */
    public Collection<SelectItem> getFilterOptions() {
        return (Collection<SelectItem>) getStateHelper().eval(PropertyKeys.filterOptions, null);
    }

    /**
     * Update the filterOptions field
     */
    public void setFilterOptions(Collection<SelectItem> filterOptions) {
        getStateHelper().put(PropertyKeys.filterOptions, filterOptions);
    }

    /**
     * Get the parent sheet
     *
     * @return
     */
    public Sheet getSheet() {
        if (sheet != null)
            return sheet;

        UIComponent parent = this.getParent();
        while (parent != null && !(parent instanceof Sheet))
            parent = parent.getParent();
        return (Sheet) parent;
    }

    /**
     * Updates the sheet reference to work around getParent sometimes returning
     * null.
     *
     * @param sheet the owning sheet
     */
    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    /**
     * --------------------------------------------------
     * <p>
     * Override UIInput methods
     */

    /**
     * Ignore attempts to set the local value for this column, again done by
     * parent.
     */
    @Override
    public void setValue(Object value) {
        localValue = value;
        this.setLocalValueSet(true);
    }

    /**
     * When asked for the value, return the local value if available, otherwise
     * the sheet value.
     */
    @Override
    public Object getValue() {
        return localValue;
    }

    /**
     * We are valid if the sheet is valid, we do not track at the individual
     * column.
     */
    @Override
    public boolean isValid() {
        return getSheet().isValid();
    }

    /**
     * when we become valid, invalidate the whole sheet.
     */
    @Override
    public void setValid(boolean valid) {
        getSheet().setValid(valid);
    }

    /**
     * Sheet handles decoding of all submitted values
     */
    @Override
    public void processDecodes(FacesContext context) {
        // do nothing, done for us by sheet
    }

    /**
     * Sheet handles updating of model
     */
    @Override
    public void processUpdates(FacesContext context) {
        // do nothing, done for us by sheet
    }

    /**
     * Reset the local value. No submitted value tracked here. Validity not
     * tracked here.
     */
    @Override
    public void resetValue() {
        this.setValue(null);
        this.setLocalValueSet(false);
    }

    /**
     * Don't do anything when called by inherited behavior. Sheet will call
     * validate directly
     */
    @Override
    public void processValidators(FacesContext context) {
        // do nothing, sheet will call validate directly
    }

    /**
     * Process all validators (skip normal UIInput behavior)
     */
    @Override
    public void validate(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        final Validator[] validators = getValidators();
        final Object value = getValue();

        if (!validateRequired(context, value))
            return;

        if (validators == null)
            return;

        for (Validator validator : validators) {
            try {
                validator.validate(context, this, value);
            } catch (ValidatorException ve) {
                // If the validator throws an exception, we're
                // invalid, and we need to add a message
                setValid(false);
                FacesMessage message;
                String validatorMessageString = getValidatorMessage();

                if (null != validatorMessageString) {
                    message =
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    validatorMessageString,
                                    validatorMessageString);
                    message.setSeverity(FacesMessage.SEVERITY_ERROR);
                } else {
                    Collection<FacesMessage> messages = ve.getFacesMessages();
                    if (null != messages) {
                        message = null;
                        String cid = getClientId(context);
                        for (FacesMessage m : messages) {
                            context.addMessage(cid, m);
                        }
                    } else {
                        message = ve.getFacesMessage();
                    }
                }
                if (message != null) {
                    Sheet sheet = getSheet();
                    if (sheet == null)
                        return;
                    context.addMessage(getClientId(context), message);
                    sheet.getBadUpdates().add(
                            new BadUpdate(sheet.getRowKeyValue(context), sheet.getColumns().indexOf(this), this, value,
                                    message
                                            .getDetail()));

                }
            }
        }

    }

    /**
     * Validates the value against the required flags on this column.
     *
     * @param context  the FacesContext
     * @param newValue the new value for this column
     * @return true if passes validation, otherwise valse
     */
    protected boolean validateRequired(FacesContext context, Object newValue) {
        // If our value is valid, enforce the required property if present
        if (isValid() && isRequired() && isEmpty(newValue)) {
            String requiredMessageStr = getRequiredMessage();
            FacesMessage message;
            if (null != requiredMessageStr) {
                message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        requiredMessageStr,
                        requiredMessageStr);
            } else {
                // TODO can't get at package protected MessageFactory to do this
                // right.
                message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        MESSAGE_REQUIRED,
                        MESSAGE_REQUIRED);
            }
            context.addMessage(getClientId(context), message);
            Sheet sheet = getSheet();
            if (sheet != null)
                sheet.getBadUpdates().add(
                        new BadUpdate(sheet.getRowKeyValue(context), sheet.getColumns().indexOf(this), this, newValue,
                                message
                                        .getDetail()));
            setValid(false);
            return false;
        }
        return true;
    }
}
