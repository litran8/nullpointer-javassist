package ch.unibe.scg.nullSpy.model;

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

	private String nameOfClassFieldIsInstantiatedIn; // package.Person

	// object_filed
	private IndirectFieldObject indirectFieldObject;

	public Field(String varID, String fieldName, String fieldType,
			String nameOfClassFieldIsInstantiatedIn, int fieldSourceLineNr,
			int storePos, int startPos, int afterPos,
			CtClass classWhereVarIsUsed, CtBehavior behavior,
			boolean isStatic, IndirectFieldObject indirectFieldObject) {

		super(varID, fieldName, fieldSourceLineNr, fieldType, isStatic,
				classWhereVarIsUsed, behavior, storePos, startPos, afterPos);

		this.nameOfClassFieldIsInstantiatedIn = nameOfClassFieldIsInstantiatedIn;

		// OBJECT_field
		this.indirectFieldObject = indirectFieldObject;
	}

	public String getNameOfClassInWhichFieldIsInstantiated() {
		return nameOfClassFieldIsInstantiatedIn;
	}

	public IndirectFieldObject getIndirectFieldObject() {
		return indirectFieldObject;
	}

	public String toString() {
		return super.toString();
	}

}
