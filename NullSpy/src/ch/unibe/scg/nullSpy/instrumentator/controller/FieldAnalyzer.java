package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import ch.unibe.scg.nullSpy.model.Field;
import ch.unibe.scg.nullSpy.model.IndirectFieldObject;
import ch.unibe.scg.nullSpy.model.Variable;

/**
 * Instruments test-code after fields.
 */
public class FieldAnalyzer extends VariableAnalyzer {

	private ArrayList<Variable> fieldIsWritterInfoList = new ArrayList<Variable>();

	public FieldAnalyzer(CtClass cc) {
		super(cc);
	}

	/**
	 * Search all fields and store them in an arrayList. The reason for storing
	 * is because directly instrument code in this method does not work... It
	 * instruments test-code after each field assignments.
	 * 
	 * @param this.cc
	 * @param myClass
	 * @throws CannotCompileException
	 * @throws BadBytecode
	 * @throws NotFoundException
	 */
	public void instrumentAfterFieldAssignment() throws CannotCompileException,
			BadBytecode, NotFoundException {

		cc.instrument(new ExprEditor() {
			public void edit(FieldAccess field) throws CannotCompileException {

				if (field.isWriter()) {
					try {
						if (fieldIsNotPrimitive(field)) {
							// check if field is instantiated outside, not in
							// method nor constructor
							if (!isFieldInstantiatedInMethod(field)) {
								// check if is instatiated in a constructor
								if (isFieldInstantiatedInConstructor(field)) {
									CtConstructor constructor = getConstructor(field);
									// check if it's field itself, or a field of
									// the field (direct or indirect)
									if (isFieldFromCurrentCtClass(field)) {
										storeFieldOfCurrentClass(field,
												constructor);
									} else {
										// field of an object/field in current
										// class
										storeFieldOfAnotherClass(field,
												constructor);
									}
								} else {
									storeFieldInitiatedOutsideMethod(field);
								}
							} else {
								CtMethod method = cc.getDeclaredMethod(field
										.where().getMethodInfo().getName());

								if (isFieldFromCurrentCtClass(field)) {
									storeFieldOfCurrentClass(field, method);
								} else {
									// field of an object in current class
									storeFieldOfAnotherClass(field, method);
								}
							}
						}
					} catch (NotFoundException | BadBytecode e) {
						e.printStackTrace();
					}
				}
			}

		});

		for (Variable var : fieldIsWritterInfoList) {
			adaptByteCode(var);
		}
	}

	private CtConstructor getConstructor(FieldAccess field) throws BadBytecode {
		int fieldLineNumber = field.getLineNumber();
		CtConstructor aimConstructor = null;
		for (CtConstructor constructor : cc.getConstructors()) {
			CodeAttribute codeAttribute = constructor.getMethodInfo()
					.getCodeAttribute();

			if (codeAttribute == null) {
				continue;
			}

			if (fieldLineNumber >= constructor.getMethodInfo().getLineNumber(0)
					&& fieldLineNumber <= getLastLineNumberOfMethodOrConstructorBody(constructor)) {
				aimConstructor = constructor;
				break;
			}
		}
		return aimConstructor;
	}

	private boolean isFieldInstantiatedInConstructor(FieldAccess field)
			throws BadBytecode {
		boolean inConstructor = isInMethodOrConstructorBody(field,
				cc.getConstructors());

		return inConstructor;
	}

	/**
	 * Check if field is instantiated in a method or a constructor. If not, it
	 * returns false.
	 * 
	 * @param field
	 * @return
	 * @throws NotFoundException
	 * @throws BadBytecode
	 */
	private boolean isFieldInstantiatedInMethod(FieldAccess field)
			throws NotFoundException, BadBytecode {
		boolean inMethod = isInMethodOrConstructorBody(field, cc.getMethods());

		return inMethod;
	}

	private boolean isInMethodOrConstructorBody(FieldAccess field,
			CtBehavior[] ctBehaviorList) throws BadBytecode {
		boolean inMethodBody = false;
		System.out.println("ClassName: " + cc.getName());
		System.out.println("FieldName: " + field.getFieldName());
		System.out.println("FieldLineNumber: " + field.getLineNumber());
		System.out.println("FieldMethod: "
				+ field.where().getMethodInfo().getName());

		for (CtBehavior behavior : ctBehaviorList) {
			CodeAttribute codeAttribute = behavior.getMethodInfo()
					.getCodeAttribute();

			if (codeAttribute == null) {
				continue;
			}

			inMethodBody = field.getLineNumber() >= behavior.getMethodInfo()
					.getLineNumber(0)
					&& field.getLineNumber() <= getLastLineNumberOfMethodOrConstructorBody(behavior);

			if (inMethodBody) {
				break;
			}

		}
		System.out.println();
		return inMethodBody;
	}

	private int getLastLineNumberOfMethodOrConstructorBody(CtBehavior behavior)
			throws BadBytecode {
		int pos = 0;
		CodeIterator codeIterator = behavior.getMethodInfo().getCodeAttribute()
				.iterator();
		// get last pc of method (end of method)
		while (codeIterator.hasNext()) {
			pos = codeIterator.next();
		}

		// int methodEnd = behavior.getMethodInfo().getLineNumber(pos);
		return behavior.getMethodInfo().getLineNumber(pos);
	}

	private void storeFieldInitiatedOutsideMethod(FieldAccess field)
			throws NotFoundException, BadBytecode {
		storeFieldOfCurrentClass(field, null);
	}

	/**
	 * If the field is of another class than the current analyzed one. E.g. p.a:
	 * p->PersonClass
	 * 
	 * @param field
	 * @throws NotFoundException
	 * @throws BadBytecode
	 */
	private void storeFieldOfAnotherClass(FieldAccess field, CtBehavior behavior)
			throws NotFoundException, BadBytecode {

		boolean isAnotherClassAnInnerClass = isAnotherClassAnInnerClass(field);

		CodeAttribute codeAttribute = behavior.getMethodInfo()
				.getCodeAttribute();
		CodeIterator codeIterator = codeAttribute.iterator();

		HashMap<Integer, Integer> lineNumberMap = getLineNumberTable(behavior);
		LocalVariableAttribute localVariableTable = (LocalVariableAttribute) codeAttribute
				.getAttribute(LocalVariableAttribute.tag);
		ArrayList<LocalVariableTableEntry> localVariableTableList = getStableLocalVariableTableAsList(localVariableTable);

		// lineNr
		int fieldLineNumber = field.getLineNumber();

		// pos and posAfterAssignment
		int pos = getPc(lineNumberMap, fieldLineNumber);
		int posAfterAssignment = getPosAfterAssignment(pos, codeIterator,
				"put.*");

		// innerclass
		String innerClassFieldName = "";

		String fieldName;

		// object_FIELD
		int op = codeIterator.byteAt(pos);
		String opCode_field = Mnemonic.OPCODE[op];

		// OBJECT_field
		IndirectFieldObject indirectFieldObject;

		String objectName_field = "";
		String objectType_field = "";
		String objectBelongedClassName_field = "";

		if (!field.isStatic()) {

			boolean isfieldStatic_field = false;

			if (Mnemonic.OPCODE[op].matches("aload.*")) {
				// store locVar e.g. p.a -> get p
				int localVarTableIndex = 0;

				localVarTableIndex = getLocVarIndexInLocVarTable(codeIterator,
						localVariableTableList, pos, "aload.*");
				int locVarSlot = getLocVarArraySlot(codeIterator, pos);
				opCode_field = "aload_" + locVarSlot;

				objectName_field = localVariableTableList
						.get(localVarTableIndex).varName;
				objectType_field = localVariableTableList
						.get(localVarTableIndex).varType;

			} else {

				String instruction = InstructionPrinter.instructionString(
						codeIterator, pos, field.where().getMethodInfo2()
								.getConstPool());
				int brace = instruction.indexOf("(");
				instruction = instruction.substring(0, brace);
				instruction = instruction.substring(instruction
						.lastIndexOf(".") + 1);
				objectName_field = instruction;

				CtField field_field = cc.getField(objectName_field);

				objectBelongedClassName_field = field_field.getDeclaringClass()
						.getName();
				objectType_field = field_field.getSignature();
				isfieldStatic_field = op == Opcode.GETSTATIC;
				// testMethodByteCode.addGetfield(belongedClassNameOfVariable,
				// variableName, variableType);

			}

			if (isAnotherClassAnInnerClass && objectName_field.equals("this")) {
				codeIterator.move(pos);
				codeIterator.next();
				int innerClassGetFieldPos = codeIterator.next();
				int index = codeIterator.u16bitAt(innerClassGetFieldPos + 1);
				innerClassFieldName = behavior.getMethodInfo2().getConstPool()
						.getFieldrefName(index);
				fieldName = objectName_field + "." + innerClassFieldName + "."
						+ field.getFieldName();
			} else {
				fieldName = objectName_field + "." + field.getFieldName();
			}

			if (opCode_field.matches("get.*") && !isfieldStatic_field) {
				fieldName = "this." + fieldName;
			}

		} else {
			fieldName = field.getClassName() + "." + field.getFieldName();
		}

		indirectFieldObject = new IndirectFieldObject(objectName_field,
				objectType_field, objectBelongedClassName_field, opCode_field);

		String fieldType = field.getSignature();
		String fieldBelongedClassName = field.getClassName();

		fieldIsWritterInfoList.add(new Field("field", fieldName, fieldType,
				fieldBelongedClassName, fieldLineNumber, pos,
				posAfterAssignment, behavior, field.isStatic(),
				indirectFieldObject));
	}

	private boolean isAnotherClassAnInnerClass(FieldAccess field)
			throws NotFoundException {
		for (CtClass c : cc.getNestedClasses()) {
			if (c.getName().equals(field.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If the field is of the current analyzed class.
	 * 
	 * @param field
	 * @throws NotFoundException
	 * @throws BadBytecode
	 */
	private void storeFieldOfCurrentClass(FieldAccess field, CtBehavior behavior)
			throws NotFoundException, BadBytecode {

		int fieldLineNr = field.getLineNumber();
		String objectName_Field = "";
		int pos = 0;
		int posAfterAssignment = 0;

		if (behavior != null) {
			CodeAttribute codeAttribute = behavior.getMethodInfo()
					.getCodeAttribute();
			CodeIterator codeIterator = codeAttribute.iterator();

			HashMap<Integer, Integer> lineNumberMap = getLineNumberTable(behavior);
			LocalVariableAttribute localVariableTable = (LocalVariableAttribute) codeAttribute
					.getAttribute(LocalVariableAttribute.tag);
			ArrayList<LocalVariableTableEntry> localVariableTableList = getStableLocalVariableTableAsList(localVariableTable);

			pos = getPc(lineNumberMap, fieldLineNr);
			posAfterAssignment = getPosAfterAssignment(pos, codeIterator,
					"put.*");
			int localVarTableIndex = getLocVarIndexInLocVarTable(codeIterator,
					localVariableTableList, pos, "aload.*");
			objectName_Field = localVariableTableList.get(localVarTableIndex).varName
					+ ".";
		}

		String fieldName = (field.isStatic() ? "" : objectName_Field)
				+ field.getFieldName();
		String fieldType = field.getSignature();
		// int fieldLineNumber = field.getLineNumber();
		String fieldBelongedClassName = cc.getName();

		// stores field (inner class), because
		// instrument
		// directly doesn't work here...
		// if field is initiated outside a method -> method is null
		fieldIsWritterInfoList.add(new Field("field", fieldName, fieldType,
				fieldBelongedClassName, fieldLineNr, pos, posAfterAssignment,
				behavior, field.isStatic(), null));
	}

	private boolean isFieldFromCurrentCtClass(FieldAccess field)
			throws NotFoundException {
		if (field.getClassName().equals(cc.getName()))
			return true;
		else
			return false;
	}

	private boolean fieldIsNotPrimitive(FieldAccess field) {
		if (field.getSignature().matches("L.*"))
			return true;
		else
			return false;
	}

}
