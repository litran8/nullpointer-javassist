package ch.unibe.scg.nullSpy.instrumentor.model;

/**
 * Info about OBJECT_field
 * 
 * @author Lina Tran
 *
 */
public class IndirectVar {

	private String indirectVarName;
	private String indirectVarType;
	private String indirectVarDeclaringClassName; // Person p; p.a :
																// p

	private boolean isIndirectVarStatic;
	private String indirectVarOpCode;

	public IndirectVar(String indirectVarName, String indirectVarType,
			String indirectVarDeclaringClassNamed,
			boolean isIndirectVarStatic, String indirectVarOpCode) {

		this.indirectVarName = indirectVarName;
		this.indirectVarType = indirectVarType;
		this.indirectVarDeclaringClassName = indirectVarDeclaringClassNamed;

		this.isIndirectVarStatic = isIndirectVarStatic;

		this.indirectVarOpCode = indirectVarOpCode;
	}

	public String getIndirectVarName() {
		return indirectVarName;
	}

	public String getIndirectVarType() {
		return indirectVarType;
	}

	public String getIndirectVarDeclaringClassName() {
		return indirectVarDeclaringClassName;
	}

	public boolean isIndirectVarStatic() {
		return isIndirectVarStatic;
	}

	public String getIndirectVarOpCode() {
		return indirectVarOpCode;
	}

	public int getLocalVarSlot(String localVarOpCode) {
		String slotAsString = localVarOpCode.substring(localVarOpCode
				.indexOf("_") + 1);

		return Integer.parseInt(slotAsString);
	}

}
