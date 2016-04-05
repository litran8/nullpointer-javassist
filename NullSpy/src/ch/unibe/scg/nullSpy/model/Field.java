package ch.unibe.scg.nullSpy.model;

/**
 * Stores information of a field which can be written for instrumentation after
 * their collection.
 * 
 * @author Lina Tran
 *
 */
import javassist.CtBehavior;

public class Field extends Variable {

	private String fieldBelongedClassName; // package.Person

	// object_filed

	private IndirectFieldObject indirectFieldObject;

	public Field(String fieldName, String fieldType,
			String fieldBelongedClassName, int fieldSourceLineNr, int pos,
			int posAfterAssignment, CtBehavior behavior, boolean isStatic,
			IndirectFieldObject indirectFieldObject) {

		super(fieldName, fieldSourceLineNr, fieldType, isStatic, behavior, pos,
				posAfterAssignment);

		this.fieldBelongedClassName = fieldBelongedClassName;

		// OBJECT_field

		this.indirectFieldObject = indirectFieldObject;
	}

	public String getFieldBelongedClassName() {
		return fieldBelongedClassName;
	}

	public IndirectFieldObject getIndirectFieldObject() {
		return indirectFieldObject;
	}

}
