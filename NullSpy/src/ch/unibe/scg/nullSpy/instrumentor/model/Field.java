package ch.unibe.scg.nullSpy.instrumentor.model;

/**
 * Stores information of a field which can be written for instrumentation after
 * their collection.
 * 
 * @author Lina Tran
 *
 */
import javassist.CtBehavior;
import javassist.CtClass;

public class Field extends Variable {

	private String fieldDeclaringClassName; // package.Person

	// object_filed
	private IndirectVar indirectVar;

	public Field(String varID, String fieldName, String fieldType,
			String fieldDeclaringClassName, int fieldLineNr,
			int storePos, int startPos, int afterPos,
			CtClass classWhereFieldIsUsed, CtBehavior behavior,
			boolean isStatic, IndirectVar indirectVar) {

		super(varID, fieldName, fieldLineNr, fieldType, isStatic,
				classWhereFieldIsUsed, behavior, storePos, startPos, afterPos);

		this.fieldDeclaringClassName = fieldDeclaringClassName;

		// OBJECT_field
		this.indirectVar = indirectVar;
	}

	public String getFieldDeclaringClassName() {
		return fieldDeclaringClassName;
	}

	public IndirectVar getIndirectVar() {
		return indirectVar;
	}

	public String toString() {
		return super.toString();
	}

}
