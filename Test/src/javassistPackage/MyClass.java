package javassistPackage;

import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.FieldAccess;

public class MyClass {

	private CtClass cc;

	private HashMap<Field, FieldAccess> fieldMap;
	private HashMap<LocalVar, Integer> locVarMap;

	public MyClass(CtClass cc) {
		this.cc = cc;
		this.fieldMap = new HashMap<Field, FieldAccess>();
		this.locVarMap = new HashMap<LocalVar, Integer>();
	}

	public static void printOriginalClassTime(CtClass cc)
			throws CannotCompileException {

	}

	public void storeField(String fName, int fLineNumber, CtMethod ctMethod,
			FieldAccess arg) {
		this.fieldMap.put(new Field(fName, fLineNumber, ctMethod), arg);
	}

	public void storeLocVar(String varName, int varLineNumber, CtMethod m,
			int locVarTableIndex) {
		locVarMap
				.put(new LocalVar(varName, varLineNumber, m), locVarTableIndex);
	}

	public HashMap<Field, FieldAccess> getFieldMap() {
		return fieldMap;
	}

	public HashMap<LocalVar, Integer> getLocalVarMap() {
		return locVarMap;
	}

	public void setLocalVarMap(HashMap<LocalVar, Integer> localVarMap) {
		this.locVarMap = localVarMap;
	}

	public HashMap<LocalVar, Integer> getLocVarMap() {
		return locVarMap;
	}

	public void setLocVarMap(HashMap<LocalVar, Integer> locVarMap) {
		this.locVarMap = locVarMap;
	}

	public void setFieldMap(HashMap<Field, FieldAccess> fieldMap) {
		this.fieldMap = fieldMap;
	}

}

// public static void showFields() {
// for (Field f : fieldMap.keySet()) {
// System.out.println("FieldName: " + f.getFieldName()
// + "\tFieldLineNumber: " + f.getFieldLineNumber()
// + "\tMethodLineNumber: "
// + (f.getMethod().getMethodInfo().getLineNumber(0) - 1));
// }
// }

// public static void test(String className, Object objValue, int lineNr,
// String objName) {
// if (isNull(objValue)) {
//
// // System.out.print(isField(lineNr) ? "Field " : "Local variable ");
// System.out.print(objName + " at line " + lineNr + " is null: ");
// System.out.println(getNullLink(className, lineNr));
//
// }
// }
//
// private boolean isField(int lineNr) {
// for (Field f : fieldMap.keySet()) {
// if (f.getFieldLineNumber() == lineNr)
// return true;
// }
//
// return false;
//
// }
//
// public static boolean isNull(Object o) {
// return o == null;
// }
//
// private static String getNullLink(String className, int lineNumber) {
// String nullLink;
// nullLink = "(" + className + ".java:" + lineNumber + ")";
// return nullLink;
// }