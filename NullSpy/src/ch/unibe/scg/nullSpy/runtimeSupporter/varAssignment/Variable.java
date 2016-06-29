package ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment;


public class Variable {

	protected String varID;

	protected String varName;
	protected int varLineNr;
	protected String varType;

	protected String classNameInWhichVarIsUsed;
	protected String behaviorName;
	protected String behaviorSignature;

	protected int startPos;
	protected int storePos;
	protected int afterPos;

	public Variable(String classNameInWhichVarIsUsed, String behaviorName,
			String behaviorSignature, String varID, String varName,
			String varType, int varLineNr, int startPos, int storePos,
			int afterPos) {

		this.varID = varID;
		this.varName = varName;
		this.varLineNr = varLineNr;
		this.varType = varType;

		this.classNameInWhichVarIsUsed = classNameInWhichVarIsUsed;
		this.behaviorName = behaviorName;
		this.behaviorSignature = behaviorSignature;

		this.startPos = startPos;
		this.storePos = storePos;
		this.afterPos = afterPos;
	}

	public String getBehaviorSignature() {
		return behaviorSignature;
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

	public String getClassNameInWhichVarIsUsed() {
		return this.classNameInWhichVarIsUsed;
	}

	public String getBehaviorName() {
		return behaviorName;
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
		String s = "VarName: " + varName + ", LineNr: " + varLineNr
				+ ",\nVarType: " + varType + ",\nStartPos: " + startPos
				+ ", StorePos: " + storePos + ", AfterPos:" + afterPos
				+ "Behavior: " + behaviorName + ", ClassName: "
				+ classNameInWhichVarIsUsed;
		return s;
	}
}
