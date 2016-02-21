package com.lassitercg.faces.components.sheet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Class used to represent bad row
 *
 * @author <a href="mailto:mlassiter@lassitercg.com">Mark Lassiter</a>
 */
public class BadUpdate implements Serializable {

    private static final long serialVersionUID = 1L;

    private Object badRowKey;

    private int badColIndex;

    private Column badColumn;

    private Object badValue;

    private String badMessage;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("badRowKey", badRowKey).append("badColIndex", badColIndex)
                .append("badColumn", badColumn).append("badValue", badValue)
                .append("badMessage", badMessage).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof BadUpdate))
            return false;
        BadUpdate castOther = (BadUpdate) other;
        return new EqualsBuilder().append(badRowKey, castOther.badRowKey)
                .append(badColIndex, castOther.badColIndex).isEquals();
    }

    private transient int hashCode;

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
     *
     * @return the badRowIndex
     */
    public Object getBadRowKey() {
        return badRowKey;
    }

    /**
     * Updates the badRowKey value.
     *
     * @param badRowKey the bad RowKey to set
     */
    public void setBadRowKey(Object badRowKey) {
        this.badRowKey = badRowKey;
    }

    /**
     * The badColIndex value.
     *
     * @return the badColIndex
     */
    public int getBadColIndex() {
        return badColIndex;
    }

    /**
     * Updates the badColIndex value.
     *
     * @param badColIndex the badColIndex to set
     */
    public void setBadColIndex(int badColIndex) {
        this.badColIndex = badColIndex;
    }

    /**
     * The badColumn value.
     *
     * @return the badColumn
     */
    public Column getBadColumn() {
        return badColumn;
    }

    /**
     * Updates the badColumn value.
     *
     * @param badColumn the badColumn to set
     */
    public void setBadColumn(Column badColumn) {
        this.badColumn = badColumn;
    }

    /**
     * The badValue value.
     *
     * @return the badValue
     */
    public Object getBadValue() {
        return badValue;
    }

    /**
     * Updates the badValue value.
     *
     * @param badValue the badValue to set
     */
    public void setBadValue(Object badValue) {
        this.badValue = badValue;
    }

    /**
     * The badMessage value.
     *
     * @return the badMessage
     */
    public String getBadMessage() {
        return badMessage;
    }

    /**
     * Updates the badMessage value.
     *
     * @param badMessage the badMessage to set
     */
    public void setBadMessage(String badMessage) {
        this.badMessage = badMessage;
    }

    /**
     * The hashCode value.
     *
     * @return the hashCode
     */
    public int getHashCode() {
        return hashCode;
    }

    /**
     * Updates the hashCode value.
     *
     * @param hashCode the hashCode to set
     */
    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
    }

}
