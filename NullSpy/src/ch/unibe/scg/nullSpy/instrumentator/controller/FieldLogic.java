package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LocalVariableAttribute;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

/**
 * Instruments test-code after fields.
 */
public class FieldLogic extends VariableAnalyzer {

	private CtClass cc;
	// private FieldAndLocVarContainerOfOneClass container;
	private ArrayList<Field> fieldIsWritterInfoList = new ArrayList<Field>();

	// public FieldLogic(CtClass cc, FieldAndLocVarContainerOfOneClass
	// container) {
	// this.cc = cc;
	// this.container = container;
	// }

	public FieldLogic(CtClass cc) {
		super(cc);
		this.cc = cc;
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
					if (field.getLineNumber() >= cc.getDeclaredMethods()[0]
							.getMethodInfo().getLineNumber(0)
							&& fieldIsNotPrimitive(field)) {
						if (isFieldFromCurrentCtClass(field)) {
							try {
								storeFieldOfCurrentClass(field);
							} catch (NotFoundException e) {
								e.printStackTrace();
							}
						} else {
							try {
								storeFieldOfAnotherClass(field);
							} catch (NotFoundException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

			private void storeFieldOfAnotherClass(FieldAccess field)
					throws NotFoundException {

				CtMethod method = cc.getDeclaredMethod(field.where()
						.getMethodInfo().getName());
				CodeAttribute codeAttribute = method.getMethodInfo()
						.getCodeAttribute();
				CodeIterator codeIterator = codeAttribute.iterator();
				int fieldLineNumber = field.getLineNumber();
				HashMap<Integer, Integer> lineNumberMap = getLineNumberTable(method);
				LocalVariableAttribute localVariableTable = (LocalVariableAttribute) codeAttribute
						.getAttribute(LocalVariableAttribute.tag);
				int pos = getPc(lineNumberMap, fieldLineNumber);
				ConstPool pool = method.getMethodInfo2().getConstPool();
				String fieldType = pool.getFieldrefType(codeIterator
						.u16bitAt(field.indexOfBytecode() + 1));

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

					// insertTestLineForLocalVariableAssignment(method,
					// localVariableName, locVarSourceLineNr);
				} else {
					belongedClassNameOfField = field.getClassName();
					nameOfBelongedClassObjectOfField = "";
					fieldName = field.getClassName() + "."
							+ field.getFieldName();
				}

				fieldIsWritterInfoList.add(new Field(fieldName, fieldType,
						belongedClassNameOfField,
						nameOfBelongedClassObjectOfField, fieldLineNumber,
						method));

			}

			private void storeFieldOfCurrentClass(FieldAccess field)
					throws NotFoundException {

				CtMethod method = cc.getDeclaredMethod(field.where()
						.getMethodInfo().getName());

				String fieldName = field.getFieldName();

				ConstPool pool = method.getMethodInfo2().getConstPool();
				CodeIterator iter = method.getMethodInfo().getCodeAttribute()
						.iterator();
				String fieldType = pool.getFieldrefType(iter.u16bitAt(field
						.indexOfBytecode() + 1));

				int fieldSourceLineNr = field.getLineNumber();
				String belongedClassNameOfField = cc.getName();

				// stores field (inner class), because
				// instrument
				// directly doesn't work here...
				fieldIsWritterInfoList
						.add(new Field(fieldName, fieldType,
								belongedClassNameOfField, "",
								fieldSourceLineNr, method));
				// container.storeFieldIsWriterInfo(fieldName,
				// fieldSourceLineNr, method, field);

			}

			private boolean isFieldFromCurrentCtClass(FieldAccess field) {
				if (field.getClassName().equals(cc.getName()))
					return true;
				else
					return false;
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
			// insertTestLineAfterFieldAssignment(method, fieldName,
			// fieldLineNumber, fieldType);
		}
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
		public String belongedClassNameOfField;
		public String nameOfBelongedClassObjectOfField;

		public int fieldSourceLineNr;
		public CtMethod method;

		public Field(String fieldName, String fieldType,
				String belongedClassNameOfField,
				String nameOfBelongedClassObjectOfField, int fieldSourceLineNr,
				CtMethod method) {
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
	}

}
