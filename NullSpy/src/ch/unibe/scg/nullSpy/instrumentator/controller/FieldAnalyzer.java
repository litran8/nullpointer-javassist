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

	private CtClass cc;
	// private FieldAndLocVarContainerOfOneClass container;
	private ArrayList<Field> fieldIsWritterInfoList = new ArrayList<Field>();

	// public FieldLogic(CtClass cc, FieldAndLocVarContainerOfOneClass
	// container) {
	// this.cc = cc;
	// this.container = container;
	// }

	public FieldAnalyzer(CtClass cc) {
		super(cc);
		this.cc = super.cc;
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
					// only store field in methods, which are instantiated
					// inside method
					try {
						String fieldName = field.getFieldName();
						int fieldSourceLineNr = field.getLineNumber();
						if (fieldIsNotPrimitive(field)) {
							// if (!isFieldInstantiatedInMethod(field)) {
							// storeFieldInitiatedOutsideMethod(field);
							// } else {
							if (isFieldInstantiatedInMethod(field)) {
								CtMethod method = cc.getDeclaredMethod(field
										.where().getMethodInfo().getName());

								if (isFieldFromCurrentCtClass(field)) {
									storeFieldOfCurrentClass(field, method);
								} else {
									storeFieldOfAnotherClass(field, method);
								}
							}
						}
						// if (!isFieldInstantiatedInMethod(field)) {
						// storeFieldInitiatedOutsideMethod(field);
						// }
						//
						// if (isFieldInstantiatedInMethod(field)
						// && fieldIsNotPrimitive(field)) {
						//
						// CtMethod method = cc.getDeclaredMethod(field
						// .where().getMethodInfo().getName());
						//
						// if (isFieldFromCurrentCtClass(field)) {
						// storeFieldOfCurrentClass(field, method);
						// } else {
						// storeFieldOfAnotherClass(field, method);
						// }
						// }
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
					"field", f.isStatic);
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
		// if (isFieldFromCurrentCtClass(field)) {
		// storeFieldOfCurrentClass(field, null);
		// }
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

		// CtMethod method = cc.getDeclaredMethod(field.where().getMethodInfo()
		// .getName());
		CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
		CodeIterator codeIterator = codeAttribute.iterator();
		int fieldLineNumber = field.getLineNumber();
		HashMap<Integer, Integer> lineNumberMap = getLineNumberTable(method);
		LocalVariableAttribute localVariableTable = (LocalVariableAttribute) codeAttribute
				.getAttribute(LocalVariableAttribute.tag);
		int pos = getPc(lineNumberMap, fieldLineNumber);
		// ConstPool pool = method.getMethodInfo2().getConstPool();
		String fieldType = field.getSignature();
		// String fieldType = pool.getFieldrefType(codeIterator.u16bitAt(field
		// .indexOfBytecode() + 1));

		String belongedClassNameOfField;
		String nameOfBelongedClassObjectOfField;
		String fieldName;

		if (!field.isStatic()) {
			// store locVar
			int locVarIndexInLocVarTable = getLocVarIndexInLocVarTable(
					codeIterator, localVariableTable, pos, "aload.*");
			belongedClassNameOfField = field.getClassName();
			nameOfBelongedClassObjectOfField = localVariableTable
					.variableName(locVarIndexInLocVarTable);

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

		// CtMethod method = cc.getDeclaredMethod(field.where().getMethodInfo()
		// .getName());

		String fieldName = field.getFieldName();
		String fieldType = field.getSignature();

		// ConstPool pool = method.getMethodInfo2().getConstPool();
		// CodeIterator iter = method.getMethodInfo().getCodeAttribute()
		// .iterator();
		// String fieldType = pool.getFieldrefType(iter.u16bitAt(field
		// .indexOfBytecode() + 1));

		int fieldSourceLineNr = field.getLineNumber();
		String belongedClassNameOfField = cc.getName();

		// stores field (inner class), because
		// instrument
		// directly doesn't work here...
		fieldIsWritterInfoList.add(new Field(fieldName, fieldType,
				belongedClassNameOfField, "", fieldSourceLineNr, method, field
						.isStatic()));
		// container.storeFieldIsWriterInfo(fieldName,
		// fieldSourceLineNr, method, field);

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
		public boolean isStatic;

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
			this.isStatic = isStatic;
		}

		public boolean isStatic() {
			return isStatic;
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
