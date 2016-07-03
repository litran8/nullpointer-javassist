package ch.unibe.scg.nullSpy.runtimeSupporter;

import java.util.HashMap;

import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.Field;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.FieldKey;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.IndirectFieldObject;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.LocalVarKey;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.LocalVariable;

public class NullDisplayer {

	// private static List<Variable> fieldList = new ArrayList<>();
	// private static List<Variable> localVariableList = new ArrayList<>();
	private static HashMap<LocalVarKey, LocalVariable> localVarMap = new HashMap<>();
	private static HashMap<FieldKey, Field> fieldMap = new HashMap<>();

	public static void testIndirectField(String classNameInWhichVarIsUsed,
			String behaviorName, String behaviorSignature, String varID,
			String varName, String varType, String varDeclaringClassName,
			int isStatic, String indirectVarName, String indirectVarType,
			String indirectVarDeclaringClassName, String indirectVarOpcode,
			Object indirectVar, Object varValue, int varLineNr, int startPos,
			int storePos, int afterPos) {

		boolean isVarStatic = (isStatic == 1 ? true : false);
		boolean isIndirectVarStatic = false;

		String linkVarName = varName;

		FieldKey fieldKey = new FieldKey(classNameInWhichVarIsUsed, varName,
				varType, varDeclaringClassName, isVarStatic, indirectVarName,
				indirectVarType, indirectVarDeclaringClassName,
				isIndirectVarStatic, behaviorName, behaviorSignature);

		// FieldKey fieldKey = null;

		IndirectFieldObject indirectFieldObject = new IndirectFieldObject(
				indirectVar, indirectVarName, indirectVarType,
				indirectVarDeclaringClassName, isIndirectVarStatic,
				indirectVarOpcode);
		linkVarName = indirectVarName + "." + varName;

		if (!indirectVarDeclaringClassName.equals("")
				&& indirectVarType.equals("")) {
			// indirectNonStaticVar.f
			// fieldKey = new FieldKey(varName, varDeclaringClassName,
			// indirectVarName, indirectVarDeclaringClassName);

		} else if (!indirectVarDeclaringClassName.equals("")
				&& !indirectVarType.equals("")) {
			// indirectStaticVar.f
			// isIndirectVarStatic = true;
			// fieldKey = new FieldKey(varName, varDeclaringClassName,
			// indirectVarName, indirectVarType, isIndirectVarStatic);
			linkVarName = indirectVarDeclaringClassName + "." + indirectVarName
					+ "." + varName;

		} else {
			// localVar.f
			// fieldKey = new FieldKey(varName, varDeclaringClassName,
			// indirectVarName, classNameInWhichVarIsUsed, behaviorName,
			// behaviorSignature);
		}

		if (varValue == null) {

			Field field = new Field(classNameInWhichVarIsUsed, behaviorName,
					behaviorSignature, varID, varName, varType,
					varDeclaringClassName, isVarStatic, varLineNr, startPos,
					storePos, afterPos, indirectFieldObject);

			fieldMap.put(fieldKey, field);

			// fieldList.add(field);

			// System.out.print("Field ");
			// printNullLink(classNameInWhichVarIsUsed, varLineNr, linkVarName);
		} else if (fieldMap.containsKey(fieldKey)) {
			fieldMap.remove(fieldKey);
		}
	}

	public static void testDirectField(String classNameInWhichVarIsUsed,
			String behaviorName, String behaviorSignature, String varID,
			String varName, String varType, String varDeclaringClassName,
			int isStatic, Object varValue, int varLineNr, int startPos,
			int storePos, int afterPos) {
		boolean isVarStatic = (isStatic == 1 ? true : false);

		FieldKey fieldKey = new FieldKey(classNameInWhichVarIsUsed, varName,
				varType, varDeclaringClassName, isVarStatic, "", "", "", false,
				behaviorName, behaviorSignature);
		// FieldKey fieldKey = new FieldKey(varName,
		// classNameInWhichVarIsInstantiated);

		if (varValue == null) {

			String linkVarName = varName;

			// this.f

			if (isVarStatic) {
				// staticField
				// fieldKey = new FieldKey(varName, varType, isVarStatic);
				linkVarName = varDeclaringClassName + "." + varName;
			}

			IndirectFieldObject indirectFieldObject = null;
			Field field = new Field(classNameInWhichVarIsUsed, behaviorName,
					behaviorSignature, varID, varName, varType,
					varDeclaringClassName, isVarStatic, varLineNr, startPos,
					storePos, afterPos, indirectFieldObject);

			fieldMap.put(fieldKey, field);

			// fieldList.add(field);

			// System.out.print("Field ");
			// printNullLink(classNameInWhichVarIsUsed, varLineNr, linkVarName);
		} else if (fieldMap.containsKey(fieldKey)) {
			fieldMap.remove(fieldKey);
		}
	}

	public static void testLocalVar(String classNameInWhichVarIsUsed,
			String behaviorName, String behaviorSignature, String varID,
			String varName, String varType, Object varValue, int varSlot,
			int varLineNr, int startPos, int storePos, int afterPos) {

		LocalVarKey localVarKey = new LocalVarKey(varName,
				classNameInWhichVarIsUsed, behaviorName, behaviorSignature);

		if (varValue == null) {
			LocalVariable localVar = new LocalVariable(
					classNameInWhichVarIsUsed, behaviorName, behaviorSignature,
					varID, varName, varType, varLineNr, varSlot, startPos,
					storePos, afterPos);

			// localVariableList.add(localVar);

			localVarMap.put(localVarKey, localVar);

			// System.out.print("LocalVar ");
			// printNullLink(classNameInWhichVarIsUsed, varLineNr, varName);
		} else if (localVarMap.containsKey(localVarKey)) {
			localVarMap.remove(localVarKey);
		}
	}

	private static void printNullLink(String className, int lineNr,
			String linkVarName) {
		System.out.print(linkVarName + " at line " + lineNr + " is null: ");
		System.out.println(getNullLink(className, lineNr));
	}

	private static String getNullLink(String className, int lineNumber) {
		String nullLink;
		nullLink = "(" + className + ".java:" + lineNumber + ")";
		return nullLink;
	}

	// ---- For MethodReceiver-Match -------

	public static HashMap<LocalVarKey, LocalVariable> getLocalVarMap() {
		return NullDisplayer.localVarMap;
	}

	public static HashMap<FieldKey, Field> getFieldMap() {
		return NullDisplayer.fieldMap;
	}

}

// public static void test(String classNameInWhichVarIsUsed,
// String behaviorName, String behaviorSignature, String varID,
// String varName, String varType,
// String classNameInWhichVarIsInstantiated, int isStatic,
// String indirectVarName, String indirectVarType,
// String classNameInWhichIndirectVarIsInstantiated,
// String indirectVarOpcode, Object varValue, int varLineNr,
// int startPos, int storePos, int afterPos) {
//
// if (varValue == null) {
// boolean isVarStatic = (isStatic == 1 ? true : false);
// boolean isIndirectVarStatic = false;
// IndirectFieldObject indirectFieldObject = null;
//
// String linkVarName = varName;
//
// // this.f
// FieldKey fieldKey = new FieldKey(varName,
// classNameInWhichVarIsInstantiated);
//
// if (isVarStatic) {
// // class.f
// fieldKey = new FieldKey(varName, varType, isVarStatic);
// linkVarName = classNameInWhichVarIsInstantiated + "." + varName;
//
// } else if (!isVarStatic && !indirectVarName.equals("")) {
// // indirectVar.f
// indirectFieldObject = new IndirectFieldObject(indirectVarName,
// indirectVarType,
// classNameInWhichIndirectVarIsInstantiated,
// isIndirectVarStatic, indirectVarOpcode);
// linkVarName = indirectVarName + "." + varName;
//
// if (!classNameInWhichIndirectVarIsInstantiated.equals("")
// && indirectVarType.equals("")) {
// // indirectNonStaticVar.f
// fieldKey = new FieldKey(varName,
// classNameInWhichVarIsInstantiated, indirectVarName,
// classNameInWhichIndirectVarIsInstantiated);
//
// } else if (!classNameInWhichIndirectVarIsInstantiated
// .equals("") && !indirectVarType.equals("")) {
// // indirestStaticVar.f
// isIndirectVarStatic = true;
// fieldKey = new FieldKey(varName,
// classNameInWhichVarIsInstantiated, indirectVarName,
// indirectVarType, isIndirectVarStatic);
// linkVarName = classNameInWhichIndirectVarIsInstantiated
// + "." + indirectVarName + "." + varName;
//
// } else {
// // localVar.f
// fieldKey = new FieldKey(varName,
// classNameInWhichVarIsInstantiated, indirectVarName,
// classNameInWhichVarIsUsed, behaviorName,
// behaviorSignature);
// }
// }
//
// Field field = new Field(classNameInWhichVarIsUsed, behaviorName,
// behaviorSignature, varID, varName, varType,
// classNameInWhichVarIsInstantiated, isVarStatic, varLineNr,
// startPos, storePos, afterPos, indirectFieldObject);
//
// fieldMap.put(fieldKey, field);
//
// // fieldList.add(field);
//
// System.out.print("Field ");
// printNullLink(classNameInWhichVarIsUsed, varLineNr, linkVarName);
// }
// }
