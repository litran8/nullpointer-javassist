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
	private String opCode_field;
	private IndirectFieldObject indirectFieldObject;

	public Field(String fieldName, String fieldType,
			String fieldBelongedClassName, int fieldSourceLineNr, int pos,
			int posAfterAssignment, CtBehavior behavior, boolean isStatic,
			String opCode_field, IndirectFieldObject indirectFieldObject) {

		super(fieldName, fieldSourceLineNr, fieldType, isStatic, behavior, pos,
				posAfterAssignment);

		this.fieldBelongedClassName = fieldBelongedClassName;

		// OBJECT_field
		this.opCode_field = opCode_field;
		this.indirectFieldObject = indirectFieldObject;
	}

	public String getFieldBelongedClassName() {
		return fieldBelongedClassName;
	}

	public String getOpCode_field() {
		return opCode_field;
	}

	public IndirectFieldObject getIndirectFieldObject() {
		return indirectFieldObject;
	}

}
