package ch.unibe.scg.nullSpy.model;

import javassist.CtBehavior;

public class Variable {

	protected String varID;

	protected String varName;
	protected int varLineNr;
	protected String varType;

	private boolean isStatic;

	protected CtBehavior behavior;

	protected int pos;
	protected int posAfterAssignment;

	public Variable(String varID, String varName, int varLineNr,
			String varType, boolean isStatic, CtBehavior behavior, int pos,
			int posAfterAssignment) {

		this.varID = varID;
		this.varName = varName;
		this.varLineNr = varLineNr;
		this.varType = varType.substring(1, varType.indexOf(";"));

		this.isStatic = isStatic;

		this.behavior = behavior;

		this.pos = pos;
		this.posAfterAssignment = posAfterAssignment;
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

	public CtBehavior getBehavior() {
		return behavior;
	}

	public int getPos() {
		return pos;
	}

	public int getPosAfterAssignment() {
		return posAfterAssignment;
	}

	public String toString() {
		String s = "VarName: "
				+ varName
				+ ", LineNr: "
				+ varLineNr
				+ ",\nPos: "
				+ pos
				+ ", PosAfter: "
				+ posAfterAssignment
				+ (behavior == null ? "" : ", Behavior: " + behavior.getName()
						+ ", Class: " + behavior.getDeclaringClass().getName());
		return s;
	}
}
