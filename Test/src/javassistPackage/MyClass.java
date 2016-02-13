package javassistPackage;

import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.expr.FieldAccess;

public class MyClass {

	private static HashMap<Field, FieldAccess> fieldMap = new HashMap<Field, FieldAccess>();
	private static HashMap<LocalVar, Integer> locVarMap = new HashMap<LocalVar, Integer>();

	public static void printOriginalClassTime(CtClass cc)
			throws CannotCompileException {

		for (CtMethod m : cc.getDeclaredMethods()) {
			if (m.getName().equals("main")) {
				CtField f = CtField.make("public static long startTime;", cc);
				cc.addField(f);
				m.insertBefore("startTime = System.currentTimeMillis();");
				m.insertAfter("System.out.println(\"\\nOriginal class time: \" +(System.currentTimeMillis() - startTime));");
			}
		}

	}

	public static void test(String className, Object objValue, int lineNr,
			String objName) {
		if (isNull(objValue)) {

			System.out.print(isField(lineNr) ? "Field " : "Local variable ");
			System.out.print(objName + " at line " + lineNr + " is null: ");
			System.out.println(getNullLink(className, lineNr));
		}
	}

	private static boolean isField(int lineNr) {
		for (Field f : fieldMap.keySet()) {
			if (f.getFieldLineNumber() == lineNr)
				return true;
		}

		return false;

	}

	public static boolean isNull(Object o) {
		return o == null;
	}

	private static String getNullLink(String className, int lineNumber) {
		String nullLink;
		nullLink = "(" + className + ".java:" + lineNumber + ")";
		return nullLink;
	}

	public static void storeField(String fName, int fLineNumber,
			CtMethod ctMethod, FieldAccess arg) {
		fieldMap.put(new Field(fName, fLineNumber, ctMethod), arg);
	}

	public static void storeLocVar(String varName, int varLineNumber,
			CtMethod m, int locVarTableIndex) {
		locVarMap
				.put(new LocalVar(varName, varLineNumber, m), locVarTableIndex);
	}

	public static void showFields() {
		for (Field f : fieldMap.keySet()) {
			System.out.println("FieldName: " + f.getFieldName()
					+ "\tFieldLineNumber: " + f.getFieldLineNumber()
					+ "\tMethodLineNumber: "
					+ (f.getMethod().getMethodInfo().getLineNumber(0) - 1));
		}
	}

	public static HashMap<Field, FieldAccess> getFieldMap() {
		return fieldMap;
	}

	public static HashMap<LocalVar, Integer> getLocalVarMap() {
		return locVarMap;
	}

	public static void setLocalVarMap(HashMap<LocalVar, Integer> localVarMap) {
		MyClass.locVarMap = localVarMap;
	}

}
