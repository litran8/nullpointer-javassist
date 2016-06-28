package ch.unibe.scg.nullSpy.runtimeSupporter;

public class ReceiverData {

	private String nr;
	private String lineNr;
	private String varID;
	private String varName;
	private String varType;
	private String isStatic;
	private String classWhereVarIsUsed;
	private String behavior;
	private String behaviorSignature;
	private String localVarAttrIndex;
	private String fieldDeclaringClassName;

	public ReceiverData(String nr, String lineNr, String varID, String varName,
			String varType, String isStatic, String classWhereVarIsUsed,
			String behavior, String behaviorSignature,
			String localVarAttrIndex, String fieldDeclaringClassName) {

		this.nr = nr;
		this.lineNr = lineNr;
		this.varID = varID;
		this.varName = varName;
		this.varType = varType;
		this.isStatic = isStatic;
		this.classWhereVarIsUsed = classWhereVarIsUsed;
		this.behavior = behavior;
		this.behaviorSignature = behaviorSignature;
		this.localVarAttrIndex = localVarAttrIndex;
		this.fieldDeclaringClassName = fieldDeclaringClassName;
	}
}
