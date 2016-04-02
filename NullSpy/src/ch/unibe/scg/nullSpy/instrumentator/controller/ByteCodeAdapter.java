package ch.unibe.scg.nullSpy.instrumentator.controller;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LineNumberAttribute;

public class ByteCodeAdapter {

	public void insertTestLineAfterVariableAssignment(CtBehavior method,
			String variableName, String belongedClassNameOfVariable,
			int variableLineNumber, String variableType, String variableID)
			throws CannotCompileException, NotFoundException, BadBytecode {

		int insertedLineNumber = method.insertAt(variableLineNumber + 1, false,
				null);

		int lineNumberToModify = variableLineNumber + 1;
		CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
		LineNumberAttribute lineNumberAttribute = (LineNumberAttribute) codeAttribute
				.getAttribute(LineNumberAttribute.tag);
		int startPc = lineNumberAttribute.toStartPc(lineNumberToModify);
		int endPc = lineNumberAttribute.toStartPc(lineNumberToModify + 1);
		boolean b = (startPc != -1 && endPc != -1);
		// if (!b) {
		method.insertAt(
				variableLineNumber + 1,
				getTestMethodAsString(method, variableName, variableLineNumber,
						variableType, variableID));
		// } else {
		//
		// byte[] byteCode = getInsertCodeByteArray(method, variableName,
		// belongedClassNameOfVariable, variableLineNumber,
		// variableType, variableID);
		//
		// LineNumberAttribute.Pc pc = lineNumberAttribute
		// .toNearPc(insertedLineNumber);
		// CodeIterator iter = codeAttribute.iterator();
		//
		// iter.insertEx(pc.index, byteCode);
		//
		// // method.insertAt(
		// // variableLineNumber + 1,
		// // getTestMethodAsString(method, variableName,
		// // variableLineNumber, variableType, variableID));
		// }

	}

	private byte[] getInsertCodeByteArray(CtBehavior method,
			String variableName, String belongedClassNameOfVariable,
			int variableLineNumber, String variableType, String variableID)
			throws NotFoundException {

		// LOCAL VARIABLE
		// 27 ldc <String "Main2"> [17]
		// 29 ldc <String "testStackMapTable"> [17]
		// 31 aload_1 [o]
		// 32 bipush 16
		// 34 ldc <String "o"> [18]
		// 36 ldc <String "localVariable"> [20]
		// 38 ldc <String "localVariable"> [20]
		// 40 invokestatic
		// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test(java.lang.String,
		// java.lang.String, java.lang.Object, int, java.lang.String,
		// java.lang.String, java.lang.String) : void [22]

		// FIELD
		// 54 ldc <String "Main2"> [17]
		// 56 ldc <String "testStackMapTable"> [19]
		// 58 aload_0 [this]
		// 59 getfield isFieldOrLocalVariableNullExample.Main2.obj :
		// java.lang.Object [41]
		// 62 bipush 21
		// 64 ldc <String "obj"> [43]
		// 66 ldc <String "object"> [44]
		// 68 ldc <String "field"> [46]
		// 70 invokestatic
		// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test(java.lang.String,
		// java.lang.String, java.lang.Object, int, java.lang.String,
		// java.lang.String, java.lang.String) : void [24]

		ConstPool cp = method.getMethodInfo2().getConstPool();
		Bytecode testMethodByteCode = new Bytecode(cp);
		testMethodByteCode.addLdc(method.getDeclaringClass().getName());
		testMethodByteCode.addLdc(method.getName());
		if (variableID.equals("field")) {
			testMethodByteCode.addAload(0);
			testMethodByteCode.addGetfield(belongedClassNameOfVariable,
					variableName, variableType);
		} else {
			String indexAsString = variableID.substring(
					variableID.indexOf("_") + 1, variableID.length());
			int index = Integer.parseInt(indexAsString);
			testMethodByteCode.addAload(index);
		}

		testMethodByteCode.add32bit(variableLineNumber);
		testMethodByteCode.addLdc(variableName);
		testMethodByteCode.addLdc(variableType);
		testMethodByteCode.addLdc(variableID);
		CtClass nullDisplayer = ClassPool.getDefault().get(
				"ch.unibe.scg.nullspy.runtimeSupporter.NullDisplayer");
		CtClass str = ClassPool.getDefault().get("java.lang.String");
		CtClass object = ClassPool.getDefault().get("java.lang.Object");
		testMethodByteCode.addInvokestatic(nullDisplayer, "test",
				CtClass.voidType, new CtClass[] { str, str, object,
						CtClass.intType, str, str, str });
		byte[] byteCode = testMethodByteCode.get();
		return byteCode;
	}

	public void insertTestLineAfterFieldInstantiatedOutSideMethod(
			CtBehavior constructor, String variableName,
			int variableLineNumber, String variableType, String variableID)
			throws CannotCompileException {
		constructor.insertAfter(getTestMethodAsString(constructor,
				variableName, variableLineNumber, variableType, variableID));
	}

	private String getTestMethodAsString(CtBehavior method,
			String variableName, int variableLineNumber, String variableType,
			String variableID) {
		return "ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test( \""
				+ method.getDeclaringClass().getName() + "\",\""
				+ method.getName() + "\"," + variableName + ","
				+ variableLineNumber + ",\"" + variableName + "\", \""
				+ variableType + "\", \"" + variableID + "\");";
	}
}
