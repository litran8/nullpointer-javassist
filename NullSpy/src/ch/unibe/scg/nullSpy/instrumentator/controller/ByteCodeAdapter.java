package ch.unibe.scg.nullSpy.instrumentator.controller;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtConstructor;

public class ByteCodeAdapter {

	public void insertTestLineAfterVariableAssignment(CtBehavior method,
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
			CtConstructor constructor, String variableName,
			int variableLineNumber, String variableType, String variableID)
			throws CannotCompileException {
		constructor
				.insertAfter("ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test( \""
						+ constructor.getDeclaringClass().getName()
						+ "\",\""
						+ constructor.getName()
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
