package ch.unibe.scg.nullSpy.model;

import javassist.CtBehavior;

public class LocalVar extends Variable {

	private int localVarTableIndex;
	private int localVarSlot;

	public LocalVar(String varID, String localVarName, int localVarLineNr,
			String localVarType, int storePos, int startPos, int afterPos,
			CtBehavior behavior, int localVarTableIndex, int localVarSlot) {

		super(varID, localVarName, localVarLineNr, localVarType, false,
				behavior, storePos, startPos, afterPos);

		this.localVarTableIndex = localVarTableIndex;
		this.localVarSlot = localVarSlot;
	}

	public int getLocalVarTableIndex() {
		return localVarTableIndex;
	}

	public int getLocalVarSlot() {
		return localVarSlot;
	}

	public String toString() {
		return super.toString();
	}

}
