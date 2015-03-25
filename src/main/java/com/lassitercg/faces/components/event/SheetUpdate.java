package com.lassitercg.faces.components.event;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Represents the data associated with the update of a single cell.
 * <p>
 * @author <a href="mailto:mlassiter@lassitercg.com">Mark Lassiter</a>
 * @version $Id: $
 */
public class SheetUpdate implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Object rowData;

	private final Object oldValue;

	private final Object newValue;

	private final int rowIndex;

	private final int colIndex;

	private transient String toString;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (toString == null) {
			toString = new ToStringBuilder(this).appendSuper(super.toString()).append("rowData", rowData)
					.append("oldValue", oldValue).append("newValue", newValue).append("rowIndex", rowIndex)
					.append("colIndex", colIndex).toString();
		}
		return toString;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof SheetUpdate))
			return false;
		SheetUpdate castOther = (SheetUpdate) other;
		return new EqualsBuilder().append(rowIndex, castOther.rowIndex).append(colIndex, castOther.colIndex).isEquals();
	}

	private transient int hashCode;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (hashCode == 0) {
			hashCode = new HashCodeBuilder().append(rowIndex).append(colIndex).toHashCode();
		}
		return hashCode;
	}

	/**
	 * Constructs an instance representing a single cell update.
	 * <p>
	 * @param rowIndex
	 * @param colIndex
	 * @param rowData
	 * @param oldValue
	 * @param newValue
	 */
	public SheetUpdate(int rowIndex, int colIndex, Object rowData,
			Object oldValue, Object newValue) {
		this.rowIndex = rowIndex;
		this.colIndex = colIndex;
		this.rowData = rowData;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * The rowData value.
	 * <p>
	 * @return the rowData
	 */
	public Object getRowData() {
		return rowData;
	}

	/**
	 * The oldValue value.
	 * <p>
	 * @return the oldValue
	 */
	public Object getOldValue() {
		return oldValue;
	}

	/**
	 * The newValue value.
	 * <p>
	 * @return the newValue
	 */
	public Object getNewValue() {
		return newValue;
	}

	/**
	 * The rowIndex value.
	 * <p>
	 * @return the rowIndex
	 */
	public int getRowIndex() {
		return rowIndex;
	}

	/**
	 * The colIndex value.
	 * <p>
	 * @return the colIndex
	 */
	public int getColIndex() {
		return colIndex;
	}

}
