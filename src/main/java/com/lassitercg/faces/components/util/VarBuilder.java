package com.lassitercg.faces.components.util;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Builds a JavaScript var object or array string. A simple way to generalized a
 * lot of code used in renderers.
 * <p>
 * @author <a href="mailto:mlassiter@lassitercg.com">Mark Lassiter</a>
 * @version $Id: $
 */
public class VarBuilder {

	private final StringBuilder sb = new StringBuilder();

	private final boolean isObject;

	private boolean firstValue = true;

	private final boolean isVar;

	/**
	 * Constructs an instance of the builder.
	 * <p>
	 * @param varName
	 *            the variable name
	 * @param isObject
	 *            true if build an Object, false if an array.
	 */
	public VarBuilder(String varName, boolean isObject) {
		this.isObject = isObject;
		this.isVar = varName != null;
		if (isVar) {
			sb.append("var ");
			sb.append(varName);
			sb.append("=");
		}
		if (isObject)
			sb.append("{");
		else
			sb.append("[");
	}

	/**
	 * Called internally to prepare for next value
	 */
	private void next() {
		if (firstValue)
			firstValue = false;
		else
			sb.append(",");
	}

	/**
	 * Appends an Object name/value pair to the object.
	 * <p>
	 * @param propertyName
	 *            the property name
	 * @param propertyValue
	 *            the property value
	 * @param quoted
	 *            if true, the value is quoted and escaped.
	 * @return this builder
	 */
	public VarBuilder appendProperty(String propertyName, String propertyValue, boolean quoted) {
		next();
		sb.append(propertyName);
		sb.append(":");
		appendText(propertyValue, quoted);
		return this;
	}

	/**
	 * appends a property with the name "rYY_cXX" where YY is the row and XX is
	 * he column.
	 * <p>
	 * @param row
	 * @param col
	 * @param propertyValue
	 * @param quoted
	 * @return
	 */
	public VarBuilder appendRowColProperty(int row, int col, String propertyValue, boolean quoted) {
		return appendProperty("r" + row + "_c" + col, propertyValue, quoted);
	}

	/**
	 * Appends text to the var string
	 * <p>
	 * @param value
	 *            the value to append
	 * @param quoted
	 *            if true, the value is quoted and escaped.
	 * @return this builder
	 */
	public VarBuilder appendText(String value, boolean quoted) {
		if (quoted) {
			sb.append("\"");
			if (value != null)
				sb.append(StringEscapeUtils.escapeEcmaScript(value));
			sb.append("\"");
		} else if (value != null)
			sb.append(value);
		return this;
	}

	/**
	 * Appends an array value.
	 * <p>
	 * @param value
	 * @param quoted
	 * @return
	 */
	public VarBuilder appendArrayValue(String value, boolean quoted) {
		next();
		return appendText(value, quoted);
	}

	/**
	 * Closes the array or object.
	 * @return
	 */
	public VarBuilder closeVar() {
		if (this.isObject)
			sb.append("}");
		else
			sb.append("]");

		if (isVar)
			sb.append(";");
		return this;
	}

	/**
	 * Returns the string for the var.
	 */
	@Override
	public String toString() {
		return sb.toString();
	}
}
