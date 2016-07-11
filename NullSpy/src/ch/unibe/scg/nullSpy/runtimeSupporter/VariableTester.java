package ch.unibe.scg.nullSpy.runtimeSupporter;

import java.util.HashMap;

import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.Field;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.FieldKey;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.IndirectFieldObject;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.LocalVarKey;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.LocalVariable;

public class VariableTester {

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

		FieldKey fieldKey = new FieldKey(classNameInWhichVarIsUsed, varName,
				varType, varDeclaringClassName, isVarStatic, indirectVarName,
				indirectVarType, indirectVarDeclaringClassName,
				isIndirectVarStatic, behaviorName, behaviorSignature);

		IndirectFieldObject indirectFieldObject = new IndirectFieldObject(
				indirectVar, indirectVarName, indirectVarType,
				indirectVarDeclaringClassName, isIndirectVarStatic,
				indirectVarOpcode);

		if (varValue == null) {

			Field field = new Field(classNameInWhichVarIsUsed, behaviorName,
					behaviorSignature, varID, varName, varType,
					varDeclaringClassName, isVarStatic, varLineNr, startPos,
					storePos, afterPos, indirectFieldObject);

			fieldMap.put(fieldKey, field);

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

		if (varValue == null) {

			IndirectFieldObject indirectFieldObject = null;
			Field field = new Field(classNameInWhichVarIsUsed, behaviorName,
					behaviorSignature, varID, varName, varType,
					varDeclaringClassName, isVarStatic, varLineNr, startPos,
					storePos, afterPos, indirectFieldObject);

			fieldMap.put(fieldKey, field);

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

			localVarMap.put(localVarKey, localVar);

		} else if (localVarMap.containsKey(localVarKey)) {
			localVarMap.remove(localVarKey);
		}
	}

	// ---- For MethodReceiver-Match -------

	public static HashMap<LocalVarKey, LocalVariable> getLocalVarMap() {
		return VariableTester.localVarMap;
	}

	public static HashMap<FieldKey, Field> getFieldMap() {
		return VariableTester.fieldMap;
	}

}
