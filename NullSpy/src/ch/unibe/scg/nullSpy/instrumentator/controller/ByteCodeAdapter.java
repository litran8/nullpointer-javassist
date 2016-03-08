package ch.unibe.scg.nullSpy.instrumentator.controller;

import javassist.CannotCompileException;
import javassist.CtMethod;

public class ByteCodeAdapter {

	public void insertTestLineAfterVariableAssignment(CtMethod method,
			String variableName, int variableLineNumber, String variableType,
			String variableID) throws CannotCompileException {
		method.insertAt(variableLineNumber + 1,
				"ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test( \""
						+ method.getDeclaringClass().getName() + "\",\""
						+ method.getName() + "\"," + variableName + ","
						+ variableLineNumber + ",\"" + variableName + "\", \""
						+ variableType + "\", \"" + variableID + "\");");

	}

	public void insertTestLineAfterFieldInstantiatedOutSideMethod(
			CtMethod method, String variableName, int variableLineNumber,
			String variableType, String variableID)
			throws CannotCompileException {
		method.insertBefore("ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test( \""
				+ method.getDeclaringClass().getName()
				+ "\",\""
				+ method.getName()
				+ "\","
				+ variableName
				+ ","
				+ variableLineNumber
				+ ",\""
				+ variableName
				+ "\", \""
				+ variableType + "\", \"" + variableID + "\");");
	}
}
