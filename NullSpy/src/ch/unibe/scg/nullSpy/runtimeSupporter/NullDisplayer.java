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
			NullDisplayer nullDisplayer = new NullDisplayer();
			boolean isVarStatic = (isStatic == 1 ? true : false);
			boolean isIndirectVarStatic = false;
			IndirectFieldObject indirectFieldObject = null;

			String linkVarName = varName;

			// this.f
			FieldKey fieldKey = nullDisplayer.new FieldKey(varName,
					classNameInWhichVarIsInstantiated);

			if (isVarStatic) {
				// class.f
				fieldKey = nullDisplayer.new FieldKey(varName, varType,
						isVarStatic);
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
					fieldKey = nullDisplayer.new FieldKey(varName,
							classNameInWhichVarIsInstantiated, indirectVarName,
							indirectClassNameInWhichVarIsInstantiated);

				} else if (!indirectClassNameInWhichVarIsInstantiated
						.equals("") && !indirectVarType.equals("")) {
					// indirestStaticVar.f
					isIndirectVarStatic = true;
					fieldKey = nullDisplayer.new FieldKey(varName,
							classNameInWhichVarIsInstantiated, indirectVarName,
							indirectVarType, isIndirectVarStatic);
					linkVarName = indirectClassNameInWhichVarIsInstantiated
							+ "." + indirectVarName + "." + varName;

				} else {
					// localVar.f
					fieldKey = nullDisplayer.new FieldKey(varName,
							indirectClassNameInWhichVarIsInstantiated,
							indirectVarName, classNameInWhichVarIsUsed,
							behaviorName, behaviorSignature);
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

			NullDisplayer nullDisplayer = new NullDisplayer();
			localVarMap.put(
					nullDisplayer.new LocalVarKey(varName,
							classNameInWhichVarIsUsed, behaviorName,
							behaviorSignature), localVar);

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

	private class FieldKey {

		public int keySize;

		public String varName;
		public String varType;
		public String classNameInWhichVarIsInstantiated;
		public boolean isVarStatic;

		public String indirectVarName;

		public String behaviorDeclaredInClassName;
		public String behaviorName;
		public String behaviorSignature;

		public String indirectClassNameInWhichVarIsInstantiated;

		public String indirectVarType;
		public boolean isIndirectVarStatic;

		public FieldKey(String varName, String classNameInWhichVarIsInstantiated) {
			this.keySize = 2;
			this.varName = varName;
			this.classNameInWhichVarIsInstantiated = classNameInWhichVarIsInstantiated;
		}

		public FieldKey(String varName, String varType, boolean isVarStatic) {
			this.keySize = 3;
			this.varName = varName;
			this.varType = varType;
			this.isVarStatic = isVarStatic;
		}

		public FieldKey(String varName,
				String classNameInWhichVarIsInstantiated,
				String indirectVarName, String behaviorDeclaredInClassName,
				String behaviorName, String behaviorSignature) {
			this.keySize = 6;
			this.varName = varName;
			this.classNameInWhichVarIsInstantiated = classNameInWhichVarIsInstantiated;
			this.indirectVarName = indirectVarName;
			this.behaviorDeclaredInClassName = behaviorDeclaredInClassName;
			this.behaviorName = behaviorName;
			this.behaviorSignature = behaviorSignature;
		}

		public FieldKey(String varName,
				String classNameInWhichVarIsInstantiated,
				String indirectVarName,
				String indirectClassNameInWhichVarIsInstantiated) {
			this.keySize = 4;
			this.varName = varName;
			this.classNameInWhichVarIsInstantiated = classNameInWhichVarIsInstantiated;
			this.indirectVarName = indirectVarName;
			this.indirectClassNameInWhichVarIsInstantiated = indirectClassNameInWhichVarIsInstantiated;
		}

		public FieldKey(String varName,
				String classNameInWhichVarIsInstantiated,
				String indirectVarName, String indirectVarType,
				boolean isIndirectVarStatic) {
			this.keySize = 5;
			this.varName = varName;
			this.classNameInWhichVarIsInstantiated = classNameInWhichVarIsInstantiated;
			this.indirectVarName = indirectVarName;
			this.indirectVarType = indirectVarType;
			this.isIndirectVarStatic = isIndirectVarStatic;
		}

		public int getKeySize() {
			return keySize;
		}

		public String getVarName() {
			return varName;
		}

		public String getVarType() {
			return varType;
		}

		public String getClassNameInWhichVarIsInstantiated() {
			return classNameInWhichVarIsInstantiated;
		}

		public boolean isVarStatic() {
			return isVarStatic;
		}

		public String getIndirectVarName() {
			return indirectVarName;
		}

		public String getBehaviorDeclaredInClassName() {
			return behaviorDeclaredInClassName;
		}

		public String getBehaviorName() {
			return behaviorName;
		}

		public String getBehaviorSignature() {
			return behaviorSignature;
		}

		public String getIndirectClassNameInWhichVarIsInstantiated() {
			return indirectClassNameInWhichVarIsInstantiated;
		}

		public String getIndirectVarType() {
			return indirectVarType;
		}

		public boolean isIndirectVarStatic() {
			return isIndirectVarStatic;
		}

	}

	private class LocalVarKey {

		public String varName;

		public String classNameInWhichVarIsUsed;
		public String behaviorName;
		public String behaviorSignature;

		public LocalVarKey(String varName, String classNameInWhichVarIsUsed,
				String behaviorName, String behaviorSignature) {
			this.varName = varName;
			this.classNameInWhichVarIsUsed = classNameInWhichVarIsUsed;
			this.behaviorName = behaviorName;
			this.behaviorSignature = behaviorSignature;
		}

		public String getVarName() {
			return varName;
		}

		public String getClassNameInWhichVarIsUsed() {
			return classNameInWhichVarIsUsed;
		}

		public String getBehaviorName() {
			return behaviorName;
		}

		public String getBehaviorSignature() {
			return behaviorSignature;
		}

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
