package ch.unibe.scg.nullSpy.instrumentator.model;

import javassist.CtBehavior;
import javassist.CtClass;

public class LocalVar extends Variable {

	private int localVarAttrIndex;
	private int localVarSlot;

	public LocalVar(String varID, String localVarName, int localVarLineNr,
			String localVarType, int storePos, int startPos, int afterPos,
			CtClass currentAnalyzedClass, CtBehavior behavior,
			int localVarAttrIndex, int localVarSlot) {

		super(varID, localVarName, localVarLineNr, localVarType, false,
				currentAnalyzedClass, behavior, storePos, startPos, afterPos);

		this.localVarAttrIndex = localVarAttrIndex;
		this.localVarSlot = localVarSlot;
	}

	public int getLocalVarAttrIndex() {
		return localVarAttrIndex;
	}

	public int getLocalVarSlot() {
		return localVarSlot;
	}

	public String toString() {
		return super.toString();
	}

}
