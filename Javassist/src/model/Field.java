package model;

import javassist.CtMethod;

public class Field {

	private String fieldName;
	private int fieldLineNumber;
	private CtMethod method;

	public Field(String fName, int fLineNumber, CtMethod m) {
		this.fieldName = fName;
		this.fieldLineNumber = fLineNumber;
		this.method = m;
	}

	public int getFieldLineNumber() {
		return this.fieldLineNumber;
	}

	public void setFieldLineNumber(int fieldLineNumber) {
		this.fieldLineNumber = fieldLineNumber;
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public CtMethod getMethod() {
		return this.method;
	}

	public void setMethod(CtMethod method) {
		this.method = method;
	}

}