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
import javassist.bytecode.Opcode;
import ch.unibe.scg.nullSpy.model.Field;
import ch.unibe.scg.nullSpy.model.IndirectFieldObject;
import ch.unibe.scg.nullSpy.model.Variable;

public class ByteCodeAdapter {

	public void insertTestLineAfterVariableAssignment(Variable var)
			throws CannotCompileException, NotFoundException, BadBytecode {

		CtBehavior behavior = var.getBehavior();
		int varLineNr = var.getVarLineNr();

		int insertedLineNumber = behavior.insertAt(varLineNr + 1, false, null);

		CodeAttribute codeAttribute = behavior.getMethodInfo()
				.getCodeAttribute();

		// if (insertedLineNumber == varLineNr + 1) {
		// Printer p = new Printer();
		// p.printMethod(behavior, 0);
		// behavior.insertAt(varLineNr + 1,
		// getTestMethodAsString(behavior, var));
		// } else {
		// Printer p = new Printer();
		// p.printMethod(behavior, 0);
		byte[] byteCode = getInsertCodeByteArray(var);

		CodeIterator iter = codeAttribute.iterator();
		iter.move(var.getPos());
		iter.next();

		iter.insert(byteCode);

		// iter.insertEx(var.getPosAfterAssignment(), byteCode);

		// behavior.getMethodInfo().rebuildStackMap(
		// behavior.getDeclaringClass().getClassPool());
		behavior.getMethodInfo().rebuildStackMapIf6(
				behavior.getDeclaringClass().getClassPool(),
				behavior.getDeclaringClass().getClassFile2());
		// }

	}

	private byte[] getInsertCodeByteArray(Variable var)
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
		String varID = var.getVarID();
		if (varID.equals("field")) {
			varBelongedClassName = ((Field) var).getFieldBelongedClassName();
		}

		ConstPool cp = behavior.getMethodInfo2().getConstPool();
		Bytecode testMethodByteCode = new Bytecode(cp);

		testMethodByteCode.addLdc(behavior.getDeclaringClass().getName());
		testMethodByteCode.addLdc(behavior.getName());
		if (varID.equals("field")) {
			if (!var.isStatic()) {
				if (((Field) var).getIndirectFieldObject() == null) {
					testMethodByteCode.addAload(0);
				} else if (((Field) var).getIndirectFieldObject()
						.getOpCode_field().matches("a{1,2}load.*")) {
					String localVarOpCode = ((Field) var)
							.getIndirectFieldObject().getOpCode_field();
					int localVarSlot = ((Field) var).getIndirectFieldObject()
							.getLocalVarSlot(localVarOpCode);
					testMethodByteCode.addAload(localVarSlot);
				} else {
					testMethodByteCode.addAload(0);
					IndirectFieldObject OBJECT_field = ((Field) var)
							.getIndirectFieldObject();
					testMethodByteCode.addGetfield(
							OBJECT_field.getObjectBelongedClassName_field(),
							OBJECT_field.getObjectName_field(),
							OBJECT_field.getObjectType_field());
				}
				testMethodByteCode.addGetfield(varBelongedClassName, varName,
						varType);
			} else {
				testMethodByteCode.addGetstatic(
						((Field) var).getFieldBelongedClassName(),
						var.getVarName(), var.getVarType());
			}

		} else {
			String indexAsString = varID.substring(varID.indexOf("_") + 1,
					varID.length());
			int index = Integer.parseInt(indexAsString);
			testMethodByteCode.addAload(index);
		}

		testMethodByteCode.addOpcode(Opcode.BIPUSH);
		testMethodByteCode.addOpcode(var.getVarLineNr());
		testMethodByteCode.addLdc(varName);
		testMethodByteCode.addLdc(varType);
		testMethodByteCode.addLdc(varID);

		CtClass nullDisplayer = ClassPool.getDefault().get(
				"ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer");
		CtClass str = ClassPool.getDefault().get("java.lang.String");
		CtClass object = ClassPool.getDefault().get("java.lang.Object");

		testMethodByteCode.addInvokestatic(nullDisplayer, "test",
				CtClass.voidType, new CtClass[] { str, str, object,
						CtClass.intType, str, str, str });

		byte[] byteCode = testMethodByteCode.get();
		return byteCode;
	}

	public void insertTestLineAfterFieldInstantiatedOutSideMethod(
			CtBehavior constructor, Variable var) throws CannotCompileException {
		constructor.insertAfter(getTestMethodAsString(constructor, var));
	}

	private String getTestMethodAsString(CtBehavior behavior, Variable var) {
		String s = "ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test(\""
				+ behavior.getDeclaringClass().getName()
				+ "\",\""
				+ behavior.getName()
				+ "\","
				+ var.getVarName()
				+ ","
				+ var.getVarLineNr()
				+ ",\""
				+ var.getVarName()
				+ "\",\""
				+ var.getVarType() + "\",\"" + var.getVarID() + "\");";
		return s;
	}

}
