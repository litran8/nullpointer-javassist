package ch.unibe.scg.nullSpy.model;

/**
 * Info about OBJECT_field
 * 
 * @author Lina Tran
 *
 */
public class IndirectFieldObject {

	private String indirectVarName;
	private String indirectVarType;
	private String indirectClassNameInWhichObjectIsInstantiated; // Person p; p.a : p

	private boolean isIndirectVarStatic;
	private String indirectVarOpCode;

	public IndirectFieldObject(String indirectVarName,
			String indirectVarType, String indirectClassNameInWhichObjectIsInstantiated,
			boolean isIndirectVarStatic, String indirectVarOpCode) {

		this.indirectVarName = indirectVarName;
		if (indirectVarType.startsWith("[")) {
			indirectVarType = indirectVarType.substring(1);
		}

		// if (!objectType_field.equals("")) {
		// objectType_field = objectType_field.substring(0,
		// objectType_field.length() - 1);
		// }
		this.indirectVarType = indirectVarType;
		this.indirectClassNameInWhichObjectIsInstantiated = indirectClassNameInWhichObjectIsInstantiated;

		this.isIndirectVarStatic = isIndirectVarStatic;

		this.indirectVarOpCode = indirectVarOpCode;
	}

	public String getIndirectVarName() {
		return indirectVarName;
	}

	public String getIndirectVarType() {
		return indirectVarType;
	}

	public String getIndirectClassNameInWhichObjectIsInstantiated() {
		return indirectClassNameInWhichObjectIsInstantiated;
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
