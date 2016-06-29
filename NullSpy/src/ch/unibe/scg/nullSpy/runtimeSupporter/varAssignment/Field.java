package ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment;

/**
 * Stores information of a field which can be written for instrumentation after
 * their collection.
 * 
 * @author Lina Tran
 *
 */

public class Field extends Variable {

	private String varDeclaringClassName; // package.Person
	private boolean isStatic;

	// object_filed
	private IndirectFieldObject indirectFieldObject;

	public Field(String classNameInWhichVarIsUsed, String behaviorName,
			String behaviorSignature, String varID, String varName,
			String varType, String verDeclaringClassName, boolean isStatic,
			int varLineNr, int startPos, int storePos, int afterPos,
			IndirectFieldObject indirectFieldObject) {

		super(classNameInWhichVarIsUsed, behaviorName, behaviorSignature,
				varID, varName, varType, varLineNr, startPos, storePos,
				afterPos);

		this.varDeclaringClassName = verDeclaringClassName;
		this.isStatic = isStatic;

		// OBJECT_field
		this.indirectFieldObject = indirectFieldObject;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public String getVarDeclaringClassName() {
		return varDeclaringClassName;
	}

	public IndirectFieldObject getIndirectFieldObject() {
		return indirectFieldObject;
	}

	public String toString() {
		return super.toString();
	}

}
