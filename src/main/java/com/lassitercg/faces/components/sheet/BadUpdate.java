package com.lassitercg.faces.components.sheet;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Class used to represent bad row
 * <p>
 * @author <a href="mailto:mlassiter@lassitercg.com">Mark Lassiter</a>
 * @version $Id:$
 */
public class BadUpdate implements Serializable {

	private static final long serialVersionUID = 1L;

	private Object badRowKey;

	private int badColIndex;

	private Column badColumn;

	private Object badValue;

	private String badMessage;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("badRowKey", badRowKey).append("badColIndex", badColIndex)
				.append("badColumn", badColumn).append("badValue", badValue)
				.append("badMessage", badMessage).toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof BadUpdate))
			return false;
		BadUpdate castOther = (BadUpdate) other;
		return new EqualsBuilder().append(badRowKey, castOther.badRowKey)
				.append(badColIndex, castOther.badColIndex).isEquals();
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
			hashCode = new HashCodeBuilder().append(badRowKey).append(badColIndex).toHashCode();
		}
		return hashCode;
	}

	public BadUpdate() {
		super();
	}

	public BadUpdate(Object badRowKey, int badColIndex, Column badColumn, Object badValue,
			String badMessage) {
		this.badRowKey = badRowKey;
		this.badColIndex = badColIndex;
		this.badColumn = badColumn;
		this.badValue = badValue;
		this.badMessage = badMessage;
	}

	/**
	 * The bad RowKey value.
	 * <p>
	 * @return the badRowIndex
	 */
	public Object getBadRowKey() {
		return badRowKey;
	}

	/**
	 * Updates the badRowKey value.
	 * <p>
	 * @param badRowKey
	 *            the bad RowKey to set
	 */
	public void setBadRowKey(Object badRowKey) {
		this.badRowKey = badRowKey;
	}

	/**
	 * The badColIndex value.
	 * <p>
	 * @return the badColIndex
	 */
	public int getBadColIndex() {
		return badColIndex;
	}

	/**
	 * Updates the badColIndex value.
	 * <p>
	 * @param badColIndex
	 *            the badColIndex to set
	 */
	public void setBadColIndex(int badColIndex) {
		this.badColIndex = badColIndex;
	}

	/**
	 * The badColumn value.
	 * <p>
	 * @return the badColumn
	 */
	public Column getBadColumn() {
		return badColumn;
	}

	/**
	 * Updates the badColumn value.
	 * <p>
	 * @param badColumn
	 *            the badColumn to set
	 */
	public void setBadColumn(Column badColumn) {
		this.badColumn = badColumn;
	}

	/**
	 * The badValue value.
	 * <p>
	 * @return the badValue
	 */
	public Object getBadValue() {
		return badValue;
	}

	/**
	 * Updates the badValue value.
	 * <p>
	 * @param badValue
	 *            the badValue to set
	 */
	public void setBadValue(Object badValue) {
		this.badValue = badValue;
	}

	/**
	 * The badMessage value.
	 * <p>
	 * @return the badMessage
	 */
	public String getBadMessage() {
		return badMessage;
	}

	/**
	 * Updates the badMessage value.
	 * <p>
	 * @param badMessage
	 *            the badMessage to set
	 */
	public void setBadMessage(String badMessage) {
		this.badMessage = badMessage;
	}

	/**
	 * The hashCode value.
	 * <p>
	 * @return the hashCode
	 */
	public int getHashCode() {
		return hashCode;
	}

	/**
	 * Updates the hashCode value.
	 * <p>
	 * @param hashCode
	 *            the hashCode to set
	 */
	public void setHashCode(int hashCode) {
		this.hashCode = hashCode;
	}

}
