package ch.unibe.scg.nullSpy.instrumentator.controller;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
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

		// get if statement
		CtBehavior behavior = var.getBehavior();
		int varLineNr = var.getVarLineNr();
		int insertedLineNumber = behavior.insertAt(varLineNr + 1, false, null);
		boolean insertInExpectedLineNr = insertedLineNumber == varLineNr + 1;

		CodeAttribute codeAttribute = behavior.getMethodInfo()
				.getCodeAttribute();
		CodeIterator iter = codeAttribute.iterator();

		byte[] byteCode = getInsertCodeByteArray(var);

		// Printer p = new Printer();
		// System.out.println("\nBefore:");
		// p.printMethod(behavior, var.getStartPos());

		if (insertInExpectedLineNr) {
			iter.move(var.getStorePos());
			iter.next();

			iter.insert(byteCode);

		} else {
			iter.insertEx(var.getAfterPos(), byteCode);
		}

		// Printer p = new Printer();
		// System.out.println("\nBefore:");
		// p.printMethod(behavior, var.getStartPos());

		codeAttribute.computeMaxStack();

		behavior.getMethodInfo().doPreverify = true;
		// behavior.getMethodInfo().rebuildStackMap(
		// behavior.getDeclaringClass().getClassPool());
		behavior.getMethodInfo().rebuildStackMapIf6(
				behavior.getDeclaringClass().getClassPool(),
				behavior.getDeclaringClass().getClassFile2());

		// System.out.println("\n\nAfter:");
		// p.printMethod(behavior, var.getStorePos());
		//
		// System.out.println();

	}

	private byte[] getInsertCodeByteArray(Variable var)
			throws NotFoundException {

		CtBehavior behavior = var.getBehavior();

		String varName = var.getVarName();
		String varType = var.getVarType();
		String varID = var.getVarID();

		ConstPool cp = behavior.getMethodInfo2().getConstPool();
		Bytecode testMethodByteCode = new Bytecode(cp);

		// testMethod params
		testMethodByteCode.addLdc(behavior.getDeclaringClass().getName()); // class
																			// where
																			// var
																			// is
																			// used
		testMethodByteCode.addLdc(behavior.getName());
		testMethodByteCode.addLdc(behavior.getSignature());
		testMethodByteCode.addLdc(varID);
		testMethodByteCode.addLdc(varName);
		testMethodByteCode.addLdc(varType);

		if (varID.equals("field")) {
			Field field = (Field) var;

			testMethodByteCode.addLdc(field
					.getNameOfClassInWhichFieldIsInstantiated());
			// int 1 -> static, 0 -> nonStatic
			testMethodByteCode.addOpcode(Opcode.BIPUSH);
			if (field.isStatic()) {
				testMethodByteCode.add(1);
			} else {
				testMethodByteCode.add(0);
			}
			// FIELD

			if (!field.isStatic()) {

				// not static
				if (field.getIndirectFieldObject() == null) {

					// direct field non-static: this.f
					// aload_0, getfield
					if (behavior.getModifiers() != AccessFlag.STATIC) {
						testMethodByteCode.addAload(0);
					}

				} else if (field.getIndirectFieldObject().getOpCode_field()
						.matches("a{1,2}load.*")) {

					// indirect field non-static: localVar.f
					// aload_X, getfield
					String localVarOpCode = field.getIndirectFieldObject()
							.getOpCode_field();
					int localVarSlot = field.getIndirectFieldObject()
							.getLocalVarSlot(localVarOpCode);
					testMethodByteCode.addAload(localVarSlot);

				} else {

					// indirect field non-static:
					// staticObject.f: getstatic, getfield
					// this.nonStaticObject.f: aload_0, getfield, getfield
					IndirectFieldObject OBJECT_field = field
							.getIndirectFieldObject();

					// non-static_field.field: aload_0
					if (behavior.getModifiers() != AccessFlag.STATIC
							&& !OBJECT_field.isObjectStatic_field()) {
						testMethodByteCode.addAload(0);
					}

					if (OBJECT_field.isObjectStatic_field()) {
						// staticObject_field
						testMethodByteCode
								.addGetstatic(OBJECT_field
										.getObjectBelongedClassName_field(),
										OBJECT_field.getObjectName_field(),
										OBJECT_field.getObjectType_field());
					} else {
						// nonStaticObject_field
						testMethodByteCode
								.addGetfield(OBJECT_field
										.getObjectBelongedClassName_field(),
										OBJECT_field.getObjectName_field(),
										OBJECT_field.getObjectType_field());
					}
				}

				// field itself
				String nameOfClassInWhichFieldIsInstantiated = field
						.getNameOfClassInWhichFieldIsInstantiated();
				testMethodByteCode
						.addGetfield(nameOfClassInWhichFieldIsInstantiated,
								varName, varType);

				// TODO: how to add Object...

			} else {

				// static field
				// getstatic
				testMethodByteCode.addGetstatic(
						field.getNameOfClassInWhichFieldIsInstantiated(),
						var.getVarName(), var.getVarType());
			}

		} else {

			// LOCAL VAR
			// aload_X
			String slotAsString = varID.substring(varID.indexOf("_") + 1,
					varID.length());
			int slot = Integer.parseInt(slotAsString);

			testMethodByteCode.addAload(slot);

			if (slot <= 127) {
				testMethodByteCode.addOpcode(Opcode.BIPUSH);
				testMethodByteCode.add(slot);
			} else {
				testMethodByteCode.addOpcode(Opcode.SIPUSH);
				testMethodByteCode.add(slot >> 8);
				testMethodByteCode.add(slot);
			}
		}

		// more testMethod params
		// else if (n <= 127 && -128 <= n) {
		// 429 addOpcode(16); // bipush
		// 430 add(n);
		// 431 } else if (n <= 32767 && -32768 <= n) {
		// 432 addOpcode(17); // sipush
		// 433 add(n >> 8);
		// 434 add(n);
		int lineNr = var.getVarLineNr();
		if (lineNr <= 127) {
			testMethodByteCode.addOpcode(Opcode.BIPUSH);
			testMethodByteCode.add(lineNr);
		} else {
			testMethodByteCode.addOpcode(Opcode.SIPUSH);
			testMethodByteCode.add(lineNr >> 8);
			testMethodByteCode.add(lineNr);
		}

		if (var.getStartPos() <= 127) {
			testMethodByteCode.addOpcode(Opcode.BIPUSH);
			testMethodByteCode.add(var.getStartPos());
		} else {
			testMethodByteCode.addOpcode(Opcode.SIPUSH);
			testMethodByteCode.add(var.getStartPos() >> 8);
			testMethodByteCode.add(var.getStartPos());
		}

		if (var.getStorePos() <= 127) {
			testMethodByteCode.addOpcode(Opcode.BIPUSH);
			testMethodByteCode.add(var.getStorePos());
		} else {
			testMethodByteCode.addOpcode(Opcode.SIPUSH);
			testMethodByteCode.add(var.getStorePos() >> 8);
			testMethodByteCode.add(var.getStorePos());
		}

		if (var.getAfterPos() <= 127) {
			testMethodByteCode.addOpcode(Opcode.BIPUSH);
			testMethodByteCode.add(var.getAfterPos());
		} else {
			testMethodByteCode.addOpcode(Opcode.SIPUSH);
			testMethodByteCode.add(var.getAfterPos() >> 8);
			testMethodByteCode.add(var.getAfterPos());
		}

		// testMethod needs
		CtClass nullDisplayer = ClassPool.getDefault().get(
				"ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer");
		CtClass str = ClassPool.getDefault().get("java.lang.String");
		CtClass object = ClassPool.getDefault().get("java.lang.Object");

		// testMethod
		if (var.getVarID().equals("field")) {
			testMethodByteCode
					.addInvokestatic(nullDisplayer, "test", CtClass.voidType,
							new CtClass[] { str, str, str, str, str, str, str,
									CtClass.intType, object, CtClass.intType,
									CtClass.intType, CtClass.intType,
									CtClass.intType });
		} else {
			testMethodByteCode
					.addInvokestatic(nullDisplayer, "testLocalVar",
							CtClass.voidType, new CtClass[] { str, str, str,
									str, str, str, object, CtClass.intType,
									CtClass.intType, CtClass.intType,
									CtClass.intType, CtClass.intType });
		}

		byte[] byteCode = testMethodByteCode.get();

		return byteCode;

	}

	public void insertTestLineAfterFieldInstantiatedOutSideMethod(
			CtBehavior constructor, Variable var)
			throws CannotCompileException, BadBytecode {
		// insert before or after ????
		constructor.insertBefore(getTestMethodAsString(constructor, var));
		// Printer p = new Printer();
		// p.printMethod(var.getBehavior(), 0);
		// System.out.println();
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
