package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LocalVariableAttribute;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

/**
 * Instruments test-code after fields.
 */
public class FieldAnalyzer extends VariableAnalyzer {

	private ArrayList<Field> fieldIsWritterInfoList = new ArrayList<Field>();

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
	 */
	public void instrumentAfterFieldAssignment() throws CannotCompileException {

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

		for (Field f : fieldIsWritterInfoList) {

			// insertAt( int lineNr + 1, String sourceCodeAsString);
			// sourceCodeasString in code: test(String className, Object
			// varValue, int lineNr, String varName) );
			CtBehavior method = f.getCtBehavior();
			String fieldName = f.getFieldName();
			int fieldLineNumber = f.getFieldLineNumber();
			String fieldType = f.getFieldType();

			adaptByteCode(method, fieldName, fieldLineNumber, fieldType,
					"field");
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
	 * Show fields which are instantiated outside method.
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
		CtMethod[] m = cc.getMethods();
		for (CtBehavior behavior : ctBehaviorList) {
			CodeAttribute codeAttribute = behavior.getMethodInfo()
					.getCodeAttribute();

			if (codeAttribute == null) {
				continue;
			}

			int methodStart = behavior.getMethodInfo().getLineNumber(0);
			int methodEnd = getLastLineNumberOfMethodOrConstructorBody(behavior);

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
			throws NotFoundException {
		storeFieldOfCurrentClass(field, null);
	}

	/**
	 * If the field is of another class than the current analyzed one.
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

		int fieldLineNumber = field.getLineNumber();
		int pos = getPc(lineNumberMap, fieldLineNumber);
		String innerClassFieldName = "";

		String fieldType = field.getSignature();
		String belongedClassNameOfField;
		String nameOfBelongedClassObjectOfField;
		String fieldName;
		System.out.println();

		if (!field.isStatic()) {

			// store locVar
			int locVarIndexInLocVarTable = getLocVarIndexInLocVarTable(
					codeIterator, localVariableTableList, pos, "aload.*");
			belongedClassNameOfField = field.getClassName();
			nameOfBelongedClassObjectOfField = localVariableTableList
					.get(locVarIndexInLocVarTable).varName;

			if (isAnotherClassAnInnerClass
					&& nameOfBelongedClassObjectOfField.equals("this")) {
				codeIterator.move(pos);
				codeIterator.next();
				int innerClassGetFieldPos = codeIterator.next();
				int index = codeIterator.u16bitAt(innerClassGetFieldPos + 1);
				innerClassFieldName = behavior.getMethodInfo2().getConstPool()
						.getFieldrefName(index);
				fieldName = nameOfBelongedClassObjectOfField + "."
						+ innerClassFieldName + "." + field.getFieldName();
			} else {
				fieldName = nameOfBelongedClassObjectOfField + "."
						+ field.getFieldName();
			}

		} else {
			belongedClassNameOfField = field.getClassName();
			nameOfBelongedClassObjectOfField = "";
			fieldName = field.getClassName() + "." + field.getFieldName();
		}

		fieldIsWritterInfoList.add(new Field(fieldName, fieldType,
				belongedClassNameOfField, nameOfBelongedClassObjectOfField,
				fieldLineNumber, behavior, field.isStatic()));

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
	 */
	private void storeFieldOfCurrentClass(FieldAccess field, CtBehavior behavior)
			throws NotFoundException {
		int fieldLineNumber = field.getLineNumber();
		String nameOfBelongedClassObjectOfField = "";
		if (behavior != null) {
			CodeAttribute codeAttribute = behavior.getMethodInfo()
					.getCodeAttribute();
			CodeIterator codeIterator = codeAttribute.iterator();

			HashMap<Integer, Integer> lineNumberMap = getLineNumberTable(behavior);
			LocalVariableAttribute localVariableTable = (LocalVariableAttribute) codeAttribute
					.getAttribute(LocalVariableAttribute.tag);
			ArrayList<LocalVariableTableEntry> localVariableTableList = getStableLocalVariableTableAsList(localVariableTable);

			int pos = getPc(lineNumberMap, fieldLineNumber);
			int locVarIndexInLocVarTable = getLocVarIndexInLocVarTable(
					codeIterator, localVariableTableList, pos, "aload.*");
			nameOfBelongedClassObjectOfField = localVariableTableList
					.get(locVarIndexInLocVarTable).varName + ".";
		}

		String fieldName = (field.isStatic() ? ""
				: nameOfBelongedClassObjectOfField) + field.getFieldName();
		String fieldType = field.getSignature();
		// int fieldLineNumber = field.getLineNumber();
		String belongedClassNameOfField = cc.getName();

		// stores field (inner class), because
		// instrument
		// directly doesn't work here...
		// if field is initiated outside a method -> method is null
		fieldIsWritterInfoList.add(new Field(fieldName, fieldType,
				belongedClassNameOfField, "", fieldLineNumber, behavior, field
						.isStatic()));
	}

	private boolean isFieldFromCurrentCtClass(FieldAccess field)
			throws NotFoundException {
		if (field.getClassName().equals(cc.getName()))
			return true;
		else
			return false;
	}

	private boolean fieldIsNotPrimitive(FieldAccess field) {
		String sign = field.getSignature();
		if (field.getSignature().matches("L.*"))
			return true;
		else
			return false;
	}

	/**
	 * Stores information of a field which can be written for instrumentation
	 * after their collection.
	 * 
	 * @author Lina Tran
	 *
	 */
	private class Field {

		public String fieldName;
		public String fieldType;
		public String belongedClassNameOfField; // package.Person
		public String nameOfBelongedClassObjectOfField; // Person p; p.a : p

		public int fieldSourceLineNr;
		public CtBehavior behavior;

		public Field(String fieldName, String fieldType,
				String belongedClassNameOfField,
				String nameOfBelongedClassObjectOfField, int fieldSourceLineNr,
				CtBehavior behavior, boolean isStatic) {
			this.fieldName = fieldName;
			this.fieldType = fieldType;
			this.belongedClassNameOfField = belongedClassNameOfField;
			this.nameOfBelongedClassObjectOfField = nameOfBelongedClassObjectOfField;
			this.fieldSourceLineNr = fieldSourceLineNr;
			this.behavior = behavior;
		}

		public String getFieldName() {
			return fieldName;
		}

		public String getFieldType() {
			return fieldType;
		}

		public int getFieldLineNumber() {
			return fieldSourceLineNr;
		}

		public CtBehavior getCtBehavior() {
			return behavior;
		}

		public String getBelongedClassNameOfField() {
			return belongedClassNameOfField;
		}

		public String getNameOfBelongedClassObjectOfField() {
			return nameOfBelongedClassObjectOfField;
		}
	}

}
