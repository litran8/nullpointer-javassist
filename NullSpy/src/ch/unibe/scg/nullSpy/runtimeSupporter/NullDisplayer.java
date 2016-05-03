package ch.unibe.scg.nullSpy.runtimeSupporter;

import java.util.ArrayList;
import java.util.List;

public class NullDisplayer {

	// private static HashMap<Field, Object> fieldMap = new HashMap<Field,
	// Object>();
	// private static HashMap<LocVar, Object> locVarMap = new HashMap<LocVar,
	// Object>();

	private static List<Variable> fieldList = new ArrayList<>();
	private static List<Variable> localVariableList = new ArrayList<>();

	// public static void test(String classNameInWhichVarIsUsed,
	// String behaviorName, String behaviorSignature, String varID,
	// String varName, String varType,
	// String classNameInWhichVarIsInstantiated, Object varValue,
	// int isStatic, IndirectFieldObject indirectFieldObject,
	// int varLineNr, int startPos, int storePos, int afterPos) {
	// if (varValue == null) {
	//
	// Field field = new Field(classNameInWhichVarIsUsed, behaviorName,
	// behaviorSignature, varID, varName, varType,
	// classNameInWhichVarIsInstantiated, (isStatic == 1 ? true
	// : false), indirectFieldObject, varLineNr, startPos,
	// storePos, afterPos);
	// fieldList.add(field);
	//
	// System.out.print("Field ");
	// printNullLink(classNameInWhichVarIsUsed, varLineNr, varName);
	// }
	// }

	public static void test(String classNameInWhichVarIsUsed,
			String behaviorName, String behaviorSignature, String varID,
			String varName, String varType,
			String classNameInWhichVarIsInstantiated, int isStatic,
			Object varValue, int varLineNr, int startPos, int storePos,
			int afterPos) {
		if (varValue == null) {

			Field field = new Field(classNameInWhichVarIsUsed, behaviorName,
					behaviorSignature, varID, varName, varType,
					classNameInWhichVarIsInstantiated, (isStatic == 1 ? true
							: false), varLineNr, startPos, storePos, afterPos);
			fieldList.add(field);

			System.out.print("Field ");
			printNullLink(classNameInWhichVarIsUsed, varLineNr, varName);
		}
	}

	public static void testLocalVar(String classNameInWhichVarIsUsed,
			String behaviorName, String behaviorSignature, String varID,
			String varName, String varType, Object varValue, int varSlot,
			int varLineNr, int startPos, int storePos, int afterPos) {
		if (varValue == null) {
			LocalVariable localVar = new LocalVariable(
					classNameInWhichVarIsUsed, behaviorName, behaviorSignature,
					varID, varName, varType, varLineNr, varSlot, startPos,
					storePos, afterPos);
			localVariableList.add(localVar);

			System.out.print("Local variable ");
			printNullLink(classNameInWhichVarIsUsed, varLineNr, varName);
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

	// public static void isAlreadyNull(String className, String methodName,
	// String varName, String fieldOrLocVarID) {
	//
	// if (fieldOrLocVarID.equals("field")) {
	// for (Field f : fieldList) {
	// if (f.getFieldName().equals(varName)) {
	// printNullLink(f.getClassName(), f.getFieldLineNr(),
	// f.getFieldName());
	// }
	// }
	// } else {
	// for (LocalVariable lv : localVariableList) {
	// if (lv.getFieldName().equals(varName)) {
	// printNullLink(lv.getClassName(), lv.getFieldLineNr(),
	// lv.getFieldName());
	// }
	// }
	// }
	// }

}
