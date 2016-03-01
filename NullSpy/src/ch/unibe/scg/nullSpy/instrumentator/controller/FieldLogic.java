package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

/**
 * Instruments test-code after fields.
 */
public class FieldLogic {

	private CtClass cc;
	private ArrayList<Field> fieldIsWritterInfoList = new ArrayList<Field>();

	public FieldLogic(CtClass cc) {
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
						try {
							CtMethod method = cc.getDeclaredMethod(field
									.where().getMethodInfo().getName());
							String fieldName = field.getFieldName();
							int fieldSourceLineNr = field.getLineNumber();

							// stores field (inner class), because instrument
							// directly doesn't work here...
							fieldIsWritterInfoList.add(new Field(fieldName,
									fieldSourceLineNr, method));
						} catch (NotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}

		});

		for (Field f : fieldIsWritterInfoList) {

			// insertAt( int lineNr + 1, String sourceCodeAsString);
			// sourceCodeasString in code: test(String className, Object
			// varValue, int lineNr, String varName) );
			f.getCtMethod().insertAt(
					f.getFieldLineNr() + 1,
					"ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test( \""
							+ cc.getName() + "\"," + f.getFieldName() + ","
							+ f.getFieldLineNr() + ",\"" + f.getFieldName()
							+ "\");");
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
		public int fieldSourceLineNr;
		public CtMethod method;

		public Field(String fieldName, int fieldSourceLineNr, CtMethod method) {
			this.fieldName = fieldName;
			this.fieldSourceLineNr = fieldSourceLineNr;
			this.method = method;
		}

		public String getFieldName() {
			return fieldName;
		}

		public int getFieldLineNr() {
			return fieldSourceLineNr;
		}

		public CtMethod getCtMethod() {
			return method;
		}
	}

}
