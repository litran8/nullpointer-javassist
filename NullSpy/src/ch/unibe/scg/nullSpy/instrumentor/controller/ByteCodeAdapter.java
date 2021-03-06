package ch.unibe.scg.nullSpy.instrumentor.controller;

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
import ch.unibe.scg.nullSpy.instrumentor.model.Field;
import ch.unibe.scg.nullSpy.instrumentor.model.IndirectVar;
import ch.unibe.scg.nullSpy.instrumentor.model.Variable;

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

		if (insertInExpectedLineNr) {
			iter.move(var.getStorePos());
			iter.next();

			iter.insert(byteCode);

		} else {
			iter.insertEx(var.getAfterPos(), byteCode);
		}

		codeAttribute.computeMaxStack();

		behavior.getMethodInfo().doPreverify = true;
		behavior.getMethodInfo().rebuildStackMapIf6(
				behavior.getDeclaringClass().getClassPool(),
				behavior.getDeclaringClass().getClassFile2());
	}

	private byte[] getInsertCodeByteArray(Variable var)
			throws NotFoundException {

		String varName = var.getVarName();
		String varType = var.getVarType();
		String varID = var.getVarID();

		CtBehavior behavior = var.getBehavior();
		ConstPool cp = behavior.getMethodInfo2().getConstPool();
		Bytecode testMethodByteCode = new Bytecode(cp);

		// testMethod params; param = class where var is used
		testMethodByteCode.addLdc(behavior.getDeclaringClass().getName());
		testMethodByteCode.addLdc(behavior.getName());
		testMethodByteCode.addLdc(behavior.getSignature());
		testMethodByteCode.addLdc(varID);
		testMethodByteCode.addLdc(varName);
		testMethodByteCode.addLdc(varType);

		if (varID.equals("field")) {

			// FIELD

			Field field = (Field) var;
			String classNameInWhichFieldIsInstantiated = field
					.getFieldDeclaringClassName();

			testMethodByteCode.addLdc(classNameInWhichFieldIsInstantiated);

			// int 1 -> static, 0 -> nonStatic
			// testMethodByteCode.addOpcode(Opcode.BIPUSH);
			if (field.isStatic()) {
				addIntegerToBytecode(testMethodByteCode, 1);
			} else {
				addIntegerToBytecode(testMethodByteCode, 0);
			}

			IndirectVar indirectVar = field.getIndirectVar();

			if (indirectVar == null) {

				// direct fields

				if (field.isStatic()) {
					// static field: getstatic
					testMethodByteCode.addGetstatic(
							classNameInWhichFieldIsInstantiated,
							var.getVarName(), var.getVarType());
				} else {
					// this.f: aload_0, getfield
					if (behavior.getModifiers() != AccessFlag.STATIC) {
						testMethodByteCode.addAload(0);
						// field itself: _._.field
						testMethodByteCode.addGetfield(
								classNameInWhichFieldIsInstantiated, varName,
								varType);
					}
				}
			} else {

				// indirect fields

				if (indirectVar.getIndirectVarOpCode().matches("a{1,2}load.*")) {

					// infos about indirectVar.field
					testMethodByteCode.addLdc(indirectVar.getIndirectVarName());
					testMethodByteCode.addLdc(indirectVar.getIndirectVarType());
					testMethodByteCode.addLdc("");
					testMethodByteCode.addLdc(indirectVar
							.getIndirectVarOpCode());

					// localVar.f: aload_X, getfield
					String localVarOpCode = indirectVar.getIndirectVarOpCode();
					int localVarSlot = indirectVar
							.getLocalVarSlot(localVarOpCode);

					// aload_x. , just indirectVar
					testMethodByteCode.addAload(localVarSlot);

					testMethodByteCode.addAload(localVarSlot);
				} else {

					if (indirectVar.isIndirectVarStatic()) {
						// staticVar.field: getstatic, getfield

						testMethodByteCode.addLdc(indirectVar
								.getIndirectVarName());
						testMethodByteCode.addLdc(indirectVar
								.getIndirectVarType());
						testMethodByteCode.addLdc(indirectVar
								.getIndirectVarDeclaringClassName());
						testMethodByteCode.addLdc("");

						// indirectStaticVar, just the indirect object
						testMethodByteCode.addGetstatic(
								indirectVar.getIndirectVarDeclaringClassName(),
								indirectVar.getIndirectVarName(),
								indirectVar.getIndirectVarType());

						testMethodByteCode.addGetstatic(
								indirectVar.getIndirectVarDeclaringClassName(),
								indirectVar.getIndirectVarName(),
								indirectVar.getIndirectVarType());
					} else {
						// this.field.field: aload_0, getfield, getfield

						testMethodByteCode.addLdc(indirectVar
								.getIndirectVarName());
						testMethodByteCode.addLdc("");
						testMethodByteCode.addLdc(indirectVar
								.getIndirectVarDeclaringClassName());
						testMethodByteCode.addLdc("");

						// this.field, just the indirect object
						if (behavior.getModifiers() != AccessFlag.STATIC) {
							// this.
							testMethodByteCode.addAload(0);
						}
						testMethodByteCode.addGetfield(
								indirectVar.getIndirectVarDeclaringClassName(),
								indirectVar.getIndirectVarName(),
								indirectVar.getIndirectVarType());

						if (behavior.getModifiers() != AccessFlag.STATIC) {
							// this.
							testMethodByteCode.addAload(0);
						}

						// _.field.
						testMethodByteCode.addGetfield(
								indirectVar.getIndirectVarDeclaringClassName(),
								indirectVar.getIndirectVarName(),
								indirectVar.getIndirectVarType());

					}
				}
				// field itself: _._.field
				testMethodByteCode.addGetfield(
						classNameInWhichFieldIsInstantiated, varName, varType);
			}

		} else {

			// LOCAL VAR
			// aload_X
			String slotAsString = varID.substring(varID.indexOf("_") + 1,
					varID.length());
			int slot = Integer.parseInt(slotAsString);

			testMethodByteCode.addAload(slot);

			addIntegerToBytecode(testMethodByteCode, slot);
		}

		addIntegerToBytecode(testMethodByteCode, var.getVarLineNr());
		addIntegerToBytecode(testMethodByteCode, var.getStartPos());
		addIntegerToBytecode(testMethodByteCode, var.getStorePos());
		addIntegerToBytecode(testMethodByteCode, var.getAfterPos());

		// testMethod needs
		CtClass variableTester = ClassPool.getDefault().get(
				"ch.unibe.scg.nullSpy.runtimeSupporter.VariableTester");
		CtClass str = ClassPool.getDefault().get("java.lang.String");
		CtClass object = ClassPool.getDefault().get("java.lang.Object");

		// testMethod
		if (var.getVarID().equals("field")) {

			IndirectVar indirectVar = ((Field) var).getIndirectVar();

			if (indirectVar == null) {
				testMethodByteCode.addInvokestatic(variableTester,
						"testDirectField", CtClass.voidType, new CtClass[] {
								str, str, str, str, str, str, str,
								CtClass.intType, object, CtClass.intType,
								CtClass.intType, CtClass.intType,
								CtClass.intType });
			} else {
				testMethodByteCode.addInvokestatic(variableTester,
						"testIndirectField", CtClass.voidType, new CtClass[] {
								str, str, str, str, str, str, str,
								CtClass.intType, str, str, str, str, object,
								object, CtClass.intType, CtClass.intType,
								CtClass.intType, CtClass.intType });
			}

		} else {
			testMethodByteCode
					.addInvokestatic(variableTester, "testLocalVar",
							CtClass.voidType, new CtClass[] { str, str, str,
									str, str, str, object, CtClass.intType,
									CtClass.intType, CtClass.intType,
									CtClass.intType, CtClass.intType });
		}

		byte[] byteCode = testMethodByteCode.get();

		return byteCode;

	}

	private void addIntegerToBytecode(Bytecode testMethodByteCode, int i) {
		if (i <= 127) {
			testMethodByteCode.addOpcode(Opcode.BIPUSH);
			testMethodByteCode.add(i);
		} else {
			testMethodByteCode.addOpcode(Opcode.SIPUSH);
			testMethodByteCode.add(i >> 8);
			testMethodByteCode.add(i);
		}
	}

	public void insertTestLineAfterFieldInstantiatedOutSideMethod(
			CtBehavior constructor, Variable var)
			throws CannotCompileException, BadBytecode {
		constructor.insertBefore(getTestMethodAsString(constructor, var));
	}

	private String getTestMethodAsString(CtBehavior behavior, Variable var) {
		String s = "ch.unibe.scg.nullSpy.runtimeSupporter.VariableTester.test(\""
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
