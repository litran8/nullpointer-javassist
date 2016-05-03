package ch.unibe.scg.nullSpy.model;

import javassist.CtBehavior;
import javassist.CtClass;

public class Variable {

	protected String varID;

	protected String varName;
	protected int varLineNr;
	protected String varType;

	private boolean isStatic;

	protected CtClass classWhereVarIsUsed;
	protected CtBehavior behavior;

	protected int startPos;
	protected int storePos;
	protected int afterPos;

	public Variable(String varID, String varName, int varLineNr,
			String varType, boolean isStatic,
			CtClass classWhereVarIsUsed, CtBehavior behavior,
			int storePos, int startPos, int afterPos) {

		this.varID = varID;
		this.varName = varName;
		this.varLineNr = varLineNr;

		if (varType.startsWith("[")) {
			varType = varType.substring(1);
		}

		this.varType = varType;

		this.isStatic = isStatic;

		this.classWhereVarIsUsed = classWhereVarIsUsed;
		this.behavior = behavior;

		this.storePos = storePos;
		this.startPos = startPos;
		this.afterPos = afterPos;
	}

	public String getVarID() {
		return varID;
	}

	public String getVarName() {
		return varName;
	}

	public int getVarLineNr() {
		return varLineNr;
	}

	public String getVarType() {
		return varType;
	}

	public boolean isStatic() {
		return this.isStatic;
	}

	public CtClass getClassWhereVarIsUsed() {
		return this.classWhereVarIsUsed;
	}

	public CtBehavior getBehavior() {
		return behavior;
	}

	public void setBehavior(CtBehavior behavior) {
		this.behavior = behavior;
	}

	public int getStorePos() {
		return storePos;
	}

	public int getStartPos() {
		return startPos;
	}

	public int getAfterPos() {
		return afterPos;
	}

	public String toString() {
		String s = "VarName: "
				+ varName
				+ ", LineNr: "
				+ varLineNr
				+ ",\nVarType: "
				+ varType
				+ ",\nStartPos: "
				+ startPos
				+ ", StorePos: "
				+ storePos
				+ ", AfterPos:"
				+ afterPos
				+ (behavior == null ? "" : ",\nBehavior: " + behavior.getName()
						+ ", Class: " + behavior.getDeclaringClass().getName());
		return s;
	}
}
