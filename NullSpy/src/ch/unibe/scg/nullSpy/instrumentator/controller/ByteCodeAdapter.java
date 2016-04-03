package ch.unibe.scg.nullSpy.instrumentator.controller;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LineNumberAttribute;
import ch.unibe.scg.nullSpy.model.Field;
import ch.unibe.scg.nullSpy.model.Variable;

public class ByteCodeAdapter {

	public void insertTestLineAfterVariableAssignment(Variable var,
			String variableID) throws CannotCompileException,
			NotFoundException, BadBytecode {

		CtBehavior behavior = var.getBehavior();
		int varLineNr = var.getVarLineNr();

		int insertedLineNumber = behavior.insertAt(varLineNr + 1, false, null);

		int lineNumberToModify = varLineNr + 1;
		CodeAttribute codeAttribute = behavior.getMethodInfo()
				.getCodeAttribute();
		LineNumberAttribute lineNumberAttribute = (LineNumberAttribute) codeAttribute
				.getAttribute(LineNumberAttribute.tag);
		int startPc = lineNumberAttribute.toStartPc(lineNumberToModify);
		int endPc = lineNumberAttribute.toStartPc(lineNumberToModify + 1);
		boolean b = (startPc == -1 && endPc == -1);
		if (insertedLineNumber == varLineNr + 1) {
			behavior.insertAt(varLineNr + 1,
					getTestMethodAsString(behavior, var, variableID));
		} else {

			byte[] byteCode = getInsertCodeByteArray(var, variableID);

			LineNumberAttribute.Pc pc = lineNumberAttribute
					.toNearPc(insertedLineNumber);
			CodeIterator iter = codeAttribute.iterator();

			iter.insertEx(var.getPosAfterAssignment(), byteCode);

			// method.insertAt(
			// variableLineNumber + 1,
			// getTestMethodAsString(method, variableName,
			// variableLineNumber, variableType, variableID));
		}

	}

	private byte[] getInsertCodeByteArray(Variable var, String variableID)
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

		CtBehavior behavior = var.getBehavior();
		String varName = var.getVarName();
		String varType = var.getVarType();
		String varBelongedClassName = "";
		if (variableID.equals("field")) {
			varBelongedClassName = ((Field) var).getFieldBelongedClassName();
		}

		ConstPool cp = behavior.getMethodInfo2().getConstPool();
		Bytecode testMethodByteCode = new Bytecode(cp);

		testMethodByteCode.addLdc(behavior.getDeclaringClass().getName());
		testMethodByteCode.addLdc(behavior.getName());
		if (variableID.equals("field")) {
			if (!var.isStatic()) {
				testMethodByteCode.addAload(0);
			}
			testMethodByteCode.addGetfield(varBelongedClassName, varName,
					varType);
		} else {
			String indexAsString = variableID.substring(
					variableID.indexOf("_") + 1, variableID.length());
			int index = Integer.parseInt(indexAsString);
			testMethodByteCode.addAload(index);
		}

		testMethodByteCode.add32bit(var.getVarLineNr());
		testMethodByteCode.addLdc(varName);
		testMethodByteCode.addLdc(varType);
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

	// private byte[] getInsertCodeByteArray(CtBehavior method,
	// String variableName, String belongedClassNameOfVariable,
	// int variableLineNumber, String variableType, String variableID,
	// boolean isStatic) throws NotFoundException {
	//
	// // LOCAL VARIABLE
	// // 27 ldc <String "Main2"> [17]
	// // 29 ldc <String "testStackMapTable"> [17]
	// // 31 aload_1 [o]
	// // 32 bipush 16
	// // 34 ldc <String "o"> [18]
	// // 36 ldc <String "localVariable"> [20]
	// // 38 ldc <String "localVariable"> [20]
	// // 40 invokestatic
	// //
	// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test(java.lang.String,
	// // java.lang.String, java.lang.Object, int, java.lang.String,
	// // java.lang.String, java.lang.String) : void [22]
	//
	// // FIELD
	// // 54 ldc <String "Main2"> [17]
	// // 56 ldc <String "testStackMapTable"> [19]
	// // 58 aload_0 [this]
	// // 59 getfield isFieldOrLocalVariableNullExample.Main2.obj :
	// // java.lang.Object [41]
	// // 62 bipush 21
	// // 64 ldc <String "obj"> [43]
	// // 66 ldc <String "object"> [44]
	// // 68 ldc <String "field"> [46]
	// // 70 invokestatic
	// //
	// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test(java.lang.String,
	// // java.lang.String, java.lang.Object, int, java.lang.String,
	// // java.lang.String, java.lang.String) : void [24]
	//
	// ConstPool cp = method.getMethodInfo2().getConstPool();
	// Bytecode testMethodByteCode = new Bytecode(cp);
	// testMethodByteCode.addLdc(method.getDeclaringClass().getName());
	// testMethodByteCode.addLdc(method.getName());
	// if (variableID.equals("field")) {
	// if (!isStatic)
	// testMethodByteCode.addAload(0);
	// testMethodByteCode.addGetfield(belongedClassNameOfVariable,
	// variableName, variableType);
	// } else {
	// String indexAsString = variableID.substring(
	// variableID.indexOf("_") + 1, variableID.length());
	// int index = Integer.parseInt(indexAsString);
	// testMethodByteCode.addAload(index);
	// }
	//
	// testMethodByteCode.add32bit(variableLineNumber);
	// testMethodByteCode.addLdc(variableName);
	// testMethodByteCode.addLdc(variableType);
	// testMethodByteCode.addLdc(variableID);
	// CtClass nullDisplayer = ClassPool.getDefault().get(
	// "ch.unibe.scg.nullspy.runtimeSupporter.NullDisplayer");
	// CtClass str = ClassPool.getDefault().get("java.lang.String");
	// CtClass object = ClassPool.getDefault().get("java.lang.Object");
	// testMethodByteCode.addInvokestatic(nullDisplayer, "test",
	// CtClass.voidType, new CtClass[] { str, str, object,
	// CtClass.intType, str, str, str });
	// byte[] byteCode = testMethodByteCode.get();
	// return byteCode;
	// }

	public void insertTestLineAfterFieldInstantiatedOutSideMethod(
			CtBehavior constructor, Variable var, String variableID)
			throws CannotCompileException {
		constructor.insertAfter(getTestMethodAsString(constructor, var,
				variableID));
	}

	private String getTestMethodAsString(CtBehavior behavior, Variable var,
			String variableID) {
		return "ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test( \""
				+ behavior.getDeclaringClass().getName() + "\",\""
				+ behavior.getName() + "\"," + var.getVarName() + ","
				+ var.getVarLineNr() + ",\"" + var.getVarName() + "\", \""
				+ var.getVarType() + "\", \"" + variableID + "\");";
	}

	// public void insertTestLineAfterVariableAssignment(CtBehavior method,
	// String variableName, String belongedClassNameOfVariable,
	// int variableLineNumber, String variableType, String variableID,
	// boolean isStatic, int posAfterAssignment)
	// throws CannotCompileException, NotFoundException, BadBytecode {
	//
	// int insertedLineNumber = method.insertAt(variableLineNumber + 1, false,
	// null);
	//
	// int lineNumberToModify = variableLineNumber + 1;
	// CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
	// LineNumberAttribute lineNumberAttribute = (LineNumberAttribute)
	// codeAttribute
	// .getAttribute(LineNumberAttribute.tag);
	// int startPc = lineNumberAttribute.toStartPc(lineNumberToModify);
	// int endPc = lineNumberAttribute.toStartPc(lineNumberToModify + 1);
	// boolean b = (startPc == -1 && endPc == -1);
	// if (insertedLineNumber == variableLineNumber + 1) {
	// method.insertAt(
	// variableLineNumber + 1,
	// getTestMethodAsString(method, variableName,
	// variableLineNumber, variableType, variableID));
	// } else {
	//
	// byte[] byteCode = getInsertCodeByteArray(method, variableName,
	// belongedClassNameOfVariable, variableLineNumber,
	// variableType, variableID, isStatic);
	//
	// LineNumberAttribute.Pc pc = lineNumberAttribute
	// .toNearPc(insertedLineNumber);
	// CodeIterator iter = codeAttribute.iterator();
	//
	// iter.insertEx(posAfterAssignment, byteCode);
	//
	// // method.insertAt(
	// // variableLineNumber + 1,
	// // getTestMethodAsString(method, variableName,
	// // variableLineNumber, variableType, variableID));
	// }
	//
	// }

	// public void insertTestLineAfterFieldInstantiatedOutSideMethod(
	// CtBehavior constructor, String variableName,
	// int variableLineNumber, String variableType, String variableID)
	// throws CannotCompileException {
	// constructor.insertAfter(getTestMethodAsString(constructor,
	// variableName, variableLineNumber, variableType, variableID));
	// }

	// private String getTestMethodAsString(CtBehavior behavior,
	// String variableName, int variableLineNumber, String variableType,
	// String variableID) {
	// return "ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test( \""
	// + behavior.getDeclaringClass().getName() + "\",\""
	// + behavior.getName() + "\"," + variableName + ","
	// + variableLineNumber + ",\"" + variableName + "\", \""
	// + variableType + "\", \"" + variableID + "\");";
	// }
}
