package com.lassitercg.faces.components.event;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Represents the data associated with the update of a single cell.
 *
 * @author <a href="mailto:mlassiter@lassitercg.com">Mark Lassiter</a>
 */
public class SheetUpdate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Object rowData;

    private final Object oldValue;

    private final Object newValue;

    private final Object rowKey;

    private final int colIndex;

    private transient String toString;

    @Override
    public String toString() {
        if (toString == null) {
            toString = new ToStringBuilder(this).appendSuper(super.toString()).append("rowData", rowData)
                    .append("oldValue", oldValue).append("newValue", newValue).append("rowIndex", rowKey)
                    .append("colIndex", colIndex).toString();
        }
        return toString;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof SheetUpdate))
            return false;
        SheetUpdate castOther = (SheetUpdate) other;
        return new EqualsBuilder().append(rowKey, castOther.rowKey).append(colIndex, castOther.colIndex).isEquals();
    }

    private transient int hashCode;

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = new HashCodeBuilder().append(rowKey).append(colIndex).toHashCode();
        }
        return hashCode;
    }

    /**
     * Constructs an instance representing a single cell update.
     *
     * @param rowKey   the row key of the row being updated
     * @param colIndex the column index of the column being updated
     * @param rowData  the rowData associated with the row being updated
     * @param oldValue the old cell value
     * @param newValue the new cell value
     */
    public SheetUpdate(Object rowKey, int colIndex, Object rowData,
                       Object oldValue, Object newValue) {
        this.rowKey = rowKey;
        this.colIndex = colIndex;
        this.rowData = rowData;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * The rowData value.
     *
     * @return the rowData the row data for this update
     */
    public Object getRowData() {
        return rowData;
    }

    /**
     * The oldValue value.
     *
     * @return the oldValue
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * The newValue value.
     *
     * @return the newValue
     */
    public Object getNewValue() {
        return newValue;
    }

    /**
     * The rowKey value.
     *
     * @return the rowKey
     */
    public Object getRowKey() {
        return rowKey;
    }

    /**
     * The colIndex value.
     *
     * @return the colIndex
     */
    public int getColIndex() {
        return colIndex;
    }

}
