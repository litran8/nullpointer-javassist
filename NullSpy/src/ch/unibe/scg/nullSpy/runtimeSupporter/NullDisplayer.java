package ch.unibe.scg.nullSpy.runtimeSupporter;

import java.util.HashMap;

public class NullDisplayer {

	// private static List<Variable> fieldList = new ArrayList<>();
	// private static List<Variable> localVariableList = new ArrayList<>();
	private static HashMap<LocalVarKey, LocalVariable> localVarMap = new HashMap<>();
	private static HashMap<FieldKey, Field> fieldMap = new HashMap<>();

	public static void test(String classNameInWhichVarIsUsed,
			String behaviorName, String behaviorSignature, String varID,
			String varName, String varType,
			String classNameInWhichVarIsInstantiated, int isStatic,
			String indirectVarName, String indirectVarType,
			String indirectClassNameInWhichVarIsInstantiated,
			String indirectVarOpcode, Object varValue, int varLineNr,
			int startPos, int storePos, int afterPos) {

		if (varValue == null) {
			boolean isVarStatic = (isStatic == 1 ? true : false);
			boolean isIndirectVarStatic = false;
			IndirectFieldObject indirectFieldObject = null;

			String linkVarName = varName;

			// this.f
			FieldKey fieldKey = new FieldKey(varName,
					classNameInWhichVarIsInstantiated);

			if (isVarStatic) {
				// class.f
				fieldKey = new FieldKey(varName, varType, isVarStatic);
				linkVarName = classNameInWhichVarIsInstantiated + "." + varName;

			} else if (!isVarStatic && !indirectVarName.equals("")) {
				// indirectVar.f
				indirectFieldObject = new IndirectFieldObject(indirectVarName,
						indirectVarType,
						indirectClassNameInWhichVarIsInstantiated,
						isIndirectVarStatic, indirectVarOpcode);
				linkVarName = indirectVarName + "." + varName;

				if (!indirectClassNameInWhichVarIsInstantiated.equals("")
						&& indirectVarType.equals("")) {
					// indirectNonStaticVar.f
					fieldKey = new FieldKey(varName,
							classNameInWhichVarIsInstantiated, indirectVarName,
							indirectClassNameInWhichVarIsInstantiated);

				} else if (!indirectClassNameInWhichVarIsInstantiated
						.equals("") && !indirectVarType.equals("")) {
					// indirestStaticVar.f
					isIndirectVarStatic = true;
					fieldKey = new FieldKey(varName,
							classNameInWhichVarIsInstantiated, indirectVarName,
							indirectVarType, isIndirectVarStatic);
					linkVarName = indirectClassNameInWhichVarIsInstantiated
							+ "." + indirectVarName + "." + varName;

				} else {
					// localVar.f
					fieldKey = new FieldKey(varName,
							classNameInWhichVarIsInstantiated, indirectVarName,
							classNameInWhichVarIsUsed, behaviorName,
							behaviorSignature);
				}
			}

			Field field = new Field(classNameInWhichVarIsUsed, behaviorName,
					behaviorSignature, varID, varName, varType,
					classNameInWhichVarIsInstantiated, isVarStatic, varLineNr,
					startPos, storePos, afterPos, indirectFieldObject);

			fieldMap.put(fieldKey, field);

			// fieldList.add(field);

			System.out.print("Field ");
			printNullLink(classNameInWhichVarIsUsed, varLineNr, linkVarName);
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

			// localVariableList.add(localVar);

			localVarMap.put(new LocalVarKey(varName, classNameInWhichVarIsUsed,
					behaviorName, behaviorSignature), localVar);

			System.out.print("LocalVar ");
			printNullLink(classNameInWhichVarIsUsed, varLineNr, varName);
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

}
