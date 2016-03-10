package ch.unibe.scg.nullSpy.runtimeSupporter;

/**
 * Stores information of a field which can be written for instrumentation after
 * their collection.
 * 
 * @author Lina Tran
 *
 */
public class Field {
	public String fieldName;
	public String fieldType;
	public int fieldLineNumber;
	public String methodName;
	public String className;

	public Field(String fieldName, String fieldType, int fieldLineNumber,
			String methodName, String className) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.fieldLineNumber = fieldLineNumber;
		this.methodName = methodName;
		this.className = className;
	}

	public String getFieldType() {
		return fieldType;
	}

	public int getFieldSourceLineNr() {
		return fieldLineNumber;
	}

	public String getClassName() {
		return className;
	}

	public String getFieldName() {
		return fieldName;
	}

	public int getFieldLineNr() {
		return fieldLineNumber;
	}

	public String getMethodName() {
		return methodName;
	}

}
