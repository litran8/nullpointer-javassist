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
	public int fieldSourceLineNr;
	public String methodName;
	public String className;

	public Field(String fieldName, String fieldType, int fieldSourceLineNr,
			String methodName, String className) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.fieldSourceLineNr = fieldSourceLineNr;
		this.methodName = methodName;
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public String getFieldName() {
		return fieldName;
	}

	public int getFieldLineNr() {
		return fieldSourceLineNr;
	}

	public String getMethodName() {
		return methodName;
	}

}
