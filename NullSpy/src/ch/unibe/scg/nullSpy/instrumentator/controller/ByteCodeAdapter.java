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

		Printer p = new Printer();
		// System.out.println("\nBefore:");
		// p.printMethod(behavior, var.getStartPos());

		if (insertInExpectedLineNr) {
			iter.move(var.getStorePos());
			iter.next();

			iter.insert(byteCode);

		} else {
			iter.insertEx(var.getAfterPos(), byteCode);
		}

		codeAttribute.computeMaxStack();

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
		String varBelongedClassName = "";
		String varID = var.getVarID();

		if (varID.equals("field")) {
			varBelongedClassName = ((Field) var).getFieldBelongedClassName();
		}

		ConstPool cp = behavior.getMethodInfo2().getConstPool();
		Bytecode testMethodByteCode = new Bytecode(cp);

		// testMethod params
		testMethodByteCode.addLdc(behavior.getDeclaringClass().getName());
		testMethodByteCode.addLdc(behavior.getName());

		if (varID.equals("field")) {

			// FIELD

			if (!var.isStatic()) {

				// not static
				if (((Field) var).getIndirectFieldObject() == null) {

					// direct field non-static: this.f
					// aload_0, getfield
					if (behavior.getModifiers() != AccessFlag.STATIC) {
						testMethodByteCode.addAload(0);
					}

				} else if (((Field) var).getIndirectFieldObject()
						.getOpCode_field().matches("a{1,2}load.*")) {

					// indirect field non-static: localVar.f
					// aload_X, getfield
					String localVarOpCode = ((Field) var)
							.getIndirectFieldObject().getOpCode_field();
					int localVarSlot = ((Field) var).getIndirectFieldObject()
							.getLocalVarSlot(localVarOpCode);
					testMethodByteCode.addAload(localVarSlot);

				} else {

					// indirect field non-static:
					// staticObject.f: getstatic, getfield
					// this.nonStaticObject.f: aload_0, getfield, getfield
					IndirectFieldObject OBJECT_field = ((Field) var)
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
				testMethodByteCode.addGetfield(varBelongedClassName, varName,
						varType);

			} else {

				// static field
				// getstatic
				testMethodByteCode.addGetstatic(
						((Field) var).getFieldBelongedClassName(),
						var.getVarName(), var.getVarType());
			}

		} else {

			// LOCAL VAR
			// aload_X
			String indexAsString = varID.substring(varID.indexOf("_") + 1,
					varID.length());
			int index = Integer.parseInt(indexAsString);
			testMethodByteCode.addAload(index);
		}

		// more testMethod params
		testMethodByteCode.addOpcode(Opcode.BIPUSH);
		testMethodByteCode.add(var.getVarLineNr());
		testMethodByteCode.addLdc(varName);
		testMethodByteCode.addLdc(varType);
		testMethodByteCode.addLdc(varID);

		// testMethod needs
		CtClass nullDisplayer = ClassPool.getDefault().get(
				"ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer");
		CtClass str = ClassPool.getDefault().get("java.lang.String");
		CtClass object = ClassPool.getDefault().get("java.lang.Object");

		// testMethod
		testMethodByteCode.addInvokestatic(nullDisplayer, "test",
				CtClass.voidType, new CtClass[] { str, str, object,
						CtClass.intType, str, str, str });

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
