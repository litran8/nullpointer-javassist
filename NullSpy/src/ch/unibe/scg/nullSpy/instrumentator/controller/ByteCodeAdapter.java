package ch.unibe.scg.nullSpy.instrumentator.controller;

import javassist.CannotCompileException;
import javassist.CtMethod;

public class ByteCodeAdapter {

	public void insertTestLineForLocalVariableAssignment(CtMethod method,
			String locVarName, int locVarSourceLineNr)
			throws CannotCompileException {
		method.insertAt(locVarSourceLineNr + 1,
				"ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test( \""
						+ method.getDeclaringClass().getName() + "\",\""
						+ method.getName() + "\"," + locVarName + ","
						+ locVarSourceLineNr + ",\"" + locVarName
						+ "\", \"locVar\", \"locVar\");");

	}

	public void insertTestLineAfterFieldAssignment(CtMethod method,
			String fieldName, int fieldLineNumber, String fieldType)
			throws CannotCompileException {
		method.insertAt(fieldLineNumber + 1,
				"ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test( \""
						+ method.getDeclaringClass().getName() + "\", \""
						+ method.getName() + "\", " + fieldName + ","
						+ fieldLineNumber + ",\"" + fieldName + "\", \""
						+ fieldType + "\", \"field\");");
	}
}
