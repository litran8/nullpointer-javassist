package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
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
							if (!isFieldInstantiatedInMethod(field)) {
								storeFieldInitiatedOutsideMethod(field);
							} else {
								CtMethod method = cc.getDeclaredMethod(field
										.where().getMethodInfo().getName());

								if (isFieldFromCurrentCtClass(field)) {
									storeFieldOfCurrentClass(field, method);
								} else {
									storeFieldOfAnotherClass(field, method);
								}
							}
						}
					} catch (NotFoundException e) {
						e.printStackTrace();
					}
				}
			}

		});

		for (Field f : fieldIsWritterInfoList) {

			// insertAt( int lineNr + 1, String sourceCodeAsString);
			// sourceCodeasString in code: test(String className, Object
			// varValue, int lineNr, String varName) );
			CtMethod method = f.getCtMethod();
			String fieldName = f.getFieldName();
			int fieldLineNumber = f.getFieldLineNumber();
			String fieldType = f.getFieldType();

			adaptByteCode(method, fieldName, fieldLineNumber, fieldType,
					"field");
		}
	}

	/**
	 * Show fields which are instantiated outside method.
	 * 
	 * @param field
	 * @return
	 * @throws NotFoundException
	 */
	private boolean isFieldInstantiatedInMethod(FieldAccess field)
			throws NotFoundException {
		return field.getLineNumber() >= cc.getDeclaredMethods()[0]
				.getMethodInfo().getLineNumber(0);
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
	 */
	private void storeFieldOfAnotherClass(FieldAccess field, CtMethod method)
			throws NotFoundException {

		CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
		CodeIterator codeIterator = codeAttribute.iterator();

		HashMap<Integer, Integer> lineNumberMap = getLineNumberTable(method);
		LocalVariableAttribute localVariableTable = (LocalVariableAttribute) codeAttribute
				.getAttribute(LocalVariableAttribute.tag);
		ArrayList<LocalVariableTableEntry> localVariableTableList = getStableLocalVariableTableAsList(localVariableTable);

		int fieldLineNumber = field.getLineNumber();
		int pos = getPc(lineNumberMap, fieldLineNumber);

		String fieldType = field.getSignature();
		String belongedClassNameOfField;
		String nameOfBelongedClassObjectOfField;
		String fieldName;

		if (!field.isStatic()) {
			// store locVar
			int locVarIndexInLocVarTable = getLocVarIndexInLocVarTable(
					codeIterator, localVariableTableList, pos, "aload.*");
			belongedClassNameOfField = field.getClassName();
			nameOfBelongedClassObjectOfField = localVariableTableList
					.get(locVarIndexInLocVarTable).varName;

			fieldName = nameOfBelongedClassObjectOfField + "."
					+ field.getFieldName();
		} else {
			belongedClassNameOfField = field.getClassName();
			nameOfBelongedClassObjectOfField = "";
			fieldName = field.getClassName() + "." + field.getFieldName();
		}

		fieldIsWritterInfoList.add(new Field(fieldName, fieldType,
				belongedClassNameOfField, nameOfBelongedClassObjectOfField,
				fieldLineNumber, method, field.isStatic()));

	}

	/**
	 * If the field is of the current analyzed class.
	 * 
	 * @param field
	 * @throws NotFoundException
	 */
	private void storeFieldOfCurrentClass(FieldAccess field, CtMethod method)
			throws NotFoundException {

		String fieldName = field.getFieldName();
		String fieldType = field.getSignature();
		int fieldSourceLineNr = field.getLineNumber();
		String belongedClassNameOfField = cc.getName();

		// stores field (inner class), because
		// instrument
		// directly doesn't work here...
		fieldIsWritterInfoList.add(new Field(fieldName, fieldType,
				belongedClassNameOfField, "", fieldSourceLineNr, method, field
						.isStatic()));
	}

	private boolean isFieldFromCurrentCtClass(FieldAccess field) {
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
		public CtMethod method;

		public Field(String fieldName, String fieldType,
				String belongedClassNameOfField,
				String nameOfBelongedClassObjectOfField, int fieldSourceLineNr,
				CtMethod method, boolean isStatic) {
			this.fieldName = fieldName;
			this.fieldType = fieldType;
			this.belongedClassNameOfField = belongedClassNameOfField;
			this.nameOfBelongedClassObjectOfField = nameOfBelongedClassObjectOfField;
			this.fieldSourceLineNr = fieldSourceLineNr;
			this.method = method;
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

		public CtMethod getCtMethod() {
			return method;
		}

		public String getBelongedClassNameOfField() {
			return belongedClassNameOfField;
		}

		public String getNameOfBelongedClassObjectOfField() {
			return nameOfBelongedClassObjectOfField;
		}
	}

}
