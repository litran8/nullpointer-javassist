package ch.unibe.scg.nullSpy.runtimeSupporter;

/**
 * Stores information of a locVar which can be written for instrumentation after
 * their collection.
 * 
 * @author Lina Tran
 *
 */
public class LocVar {
	public String fieldName;
	public int fieldSourceLineNr;
	public String methodName;
	public String className;

	public LocVar(String fieldName, int fieldSourceLineNr, String methodName,
			String className) {
		this.fieldName = fieldName;
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