package ch.unibe.scg.nullSpy.model;

import javassist.CtBehavior;

public class Variable {

	protected String varName;
	protected int varLineNr;
	protected String varType;

	private boolean isStatic;

	protected CtBehavior behavior;

	protected int pos;
	protected int posAfterAssignment;

	public Variable(String varName, int varLineNr, String varType,
			boolean isStatic, CtBehavior behavior, int pos,
			int posAfterAssignment) {
		this.varName = varName;
		this.varLineNr = varLineNr;
		this.varType = varType;

		this.isStatic = isStatic;

		this.behavior = behavior;

		this.pos = pos;
		this.posAfterAssignment = posAfterAssignment;
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

}
