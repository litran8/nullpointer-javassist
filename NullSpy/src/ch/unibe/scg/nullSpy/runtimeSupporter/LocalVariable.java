package ch.unibe.scg.nullSpy.runtimeSupporter;


/**
 * Stores information of a locVar which can be written for instrumentation after
 * their collection.
 * 
 * @author Lina Tran
 *
 */
public class LocalVariable extends Variable {
	private int varSlot;

	public LocalVariable(String classNameInWhichVarIsUsed, String behaviorName,
			String behaviorSignature, String varID, String varName,
			String varType, int varLineNr, int varSlot, int startPos,
			int storePos, int afterPos) {
		super(classNameInWhichVarIsUsed, behaviorName, behaviorSignature,
				varID, varName, varType, varLineNr, startPos, storePos,
				afterPos);

		this.varSlot = varSlot;
	}

	public int getVarSlot() {
		return this.varSlot;
	}
}