package ch.unibe.scg.nullSpy.runtimeSupporter;

import java.util.ArrayList;
import java.util.List;

public class NullDisplayer {

	// private static HashMap<Field, Object> fieldMap = new HashMap<Field,
	// Object>();
	// private static HashMap<LocVar, Object> locVarMap = new HashMap<LocVar,
	// Object>();

	private static List<Field> fieldList = new ArrayList<>();
	private static List<LocVar> locVarList = new ArrayList<>();

	public static void test(String className, String methodName,
			Object varValue, int lineNr, String varName, String varType,
			String fieldOrLocVarID) {
		if (varValue == null) {

			if (fieldOrLocVarID.equals("field")) {
				Field field = new Field(varName, varType, lineNr, methodName,
						className);
				// isAlreadyNull(field, fieldOrLocVarID);
				fieldList.add(field);
				// fieldMap.put(new Field(varName, lineNr, methodName,
				// className),
				// varValue);
				System.out.print("Field ");
			} else {
				locVarList.add(new LocVar(varName, lineNr, methodName,
						className));
				// locVarMap.put(
				// new LocVar(varName, lineNr, methodName, className),
				// varValue);
				System.out.print("Local variable ");
			}

			printNullLink(className, lineNr, varName);
		}
	}

	private static void printNullLink(String className, int lineNr,
			String varName) {
		System.out.print(varName + " at line " + lineNr + " is null: ");
		System.out.println(getNullLink(className, lineNr));
	}

	private static String getNullLink(String className, int lineNumber) {
		String nullLink;
		nullLink = "(" + className + ".java:" + lineNumber + ")";
		return nullLink;
	}

	public static void isAlreadyNull(String className, String methodName,
			String varName, String fieldOrLocVarID) {

		if (fieldOrLocVarID.equals("field")) {
			for (Field f : fieldList) {
				if (f.getFieldName().equals(varName)) {
					printNullLink(f.getClassName(), f.getFieldLineNr(),
							f.getFieldName());
				}
			}
		} else {
			for (LocVar lv : locVarList) {
				if (lv.getFieldName().equals(varName)) {
					printNullLink(lv.getClassName(), lv.getFieldLineNr(),
							lv.getFieldName());
				}
			}
		}
	}

	/**
	 * Stores information of a field which can be written for instrumentation
	 * after their collection.
	 * 
	 * @author Lina Tran
	 *
	 */
	// private static class Field {
	// public String fieldName;
	// public int fieldSourceLineNr;
	// public String methodName;
	// public String className;
	//
	// public Field(String fieldName, int fieldSourceLineNr,
	// String methodName, String className) {
	// this.fieldName = fieldName;
	// this.fieldSourceLineNr = fieldSourceLineNr;
	// this.methodName = methodName;
	// this.className = className;
	// }
	//
	// public String getClassName() {
	// return className;
	// }
	//
	// public String getFieldName() {
	// return fieldName;
	// }
	//
	// public int getFieldLineNr() {
	// return fieldSourceLineNr;
	// }
	//
	// public String getMethodName() {
	// return methodName;
	// }
	//
	// }

	/**
	 * Stores information of a locVar which can be written for instrumentation
	 * after their collection.
	 * 
	 * @author Lina Tran
	 *
	 */
	// private static class LocVar {
	// public String fieldName;
	// public int fieldSourceLineNr;
	// public String methodName;
	// public String className;
	//
	// public LocVar(String fieldName, int fieldSourceLineNr,
	// String methodName, String className) {
	// this.fieldName = fieldName;
	// this.fieldSourceLineNr = fieldSourceLineNr;
	// this.methodName = methodName;
	// this.className = className;
	// }
	//
	// public String getClassName() {
	// return className;
	// }
	//
	// public String getFieldName() {
	// return fieldName;
	// }
	//
	// public int getFieldLineNr() {
	// return fieldSourceLineNr;
	// }
	//
	// public String getMethodName() {
	// return methodName;
	// }
	// }
}
