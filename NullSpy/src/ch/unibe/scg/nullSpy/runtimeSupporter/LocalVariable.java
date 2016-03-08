package ch.unibe.scg.nullSpy.runtimeSupporter;

/**
 * Stores information of a locVar which can be written for instrumentation after
 * their collection.
 * 
 * @author Lina Tran
 *
 */
public class LocalVariable {
	public String localVariableName;
	public int localVariableLineNumber;
	public String methodName;
	public String className;

	public LocalVariable(String localVariableName, int localVariableLineNumber, String methodName,
			String className) {
		this.localVariableName = localVariableName;
		this.localVariableLineNumber = localVariableLineNumber;
		this.methodName = methodName;
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public String getFieldName() {
		return localVariableName;
	}

	public int getFieldLineNr() {
		return localVariableLineNumber;
	}

	public String getMethodName() {
		return methodName;
	}
}