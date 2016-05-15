package ch.unibe.scg.nullSpy.runtimeSupporter;

/**
 * Info about OBJECT_field
 * 
 * @author Lina Tran
 *
 */
public class IndirectFieldObject {

	private Object indirectVar;

	private String indirectVarName;
	private String indirectVarType;
	private String classNameInWhichIndirectVarIsInstantiated; // Person p; p.a :
																// p

	private boolean isIndirectVarStatic;
	private String indirectVarOpCode;

	public IndirectFieldObject(Object indirectVar, String indirectVarName,
			String indirectVarType,
			String classNameInWhichIndirectVarIsInstantiated,
			boolean isIndirectVarStatic, String indirectVarOpCode) {

		this.indirectVar = indirectVar;
		this.indirectVarName = indirectVarName;
		this.indirectVarType = indirectVarType;
		this.classNameInWhichIndirectVarIsInstantiated = classNameInWhichIndirectVarIsInstantiated;

		this.isIndirectVarStatic = isIndirectVarStatic;

		this.indirectVarOpCode = indirectVarOpCode;
	}

	public String getIndirectVarName() {
		return indirectVarName;
	}

	public String getIndirectVarType() {
		return indirectVarType;
	}

	public String getClassNameInWhichIndirectVarIsInstantiated() {
		return classNameInWhichIndirectVarIsInstantiated;
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
