package ch.unibe.scg.nullSpy.model;

/**
 * Info about OBJECT_field
 * 
 * @author Lina Tran
 *
 */
public class IndirectFieldObject {

	private String objectName_field;
	private String objectType_field;
	private String objectBelongedClassName_field; // Person p; p.a : p

	private boolean isObjectStatic_field;
	private String opCode_field;

	public IndirectFieldObject(String objectName_field,
			String objectType_field, String objectBelongedClassName_field,
			boolean isObjectStatic_field, String opCode_field) {

		this.objectName_field = objectName_field;
		if (objectType_field.startsWith("[")) {
			objectType_field = objectType_field.substring(1);
		}

		// if (!objectType_field.equals("")) {
		// objectType_field = objectType_field.substring(0,
		// objectType_field.length() - 1);
		// }
		this.objectType_field = objectType_field;
		this.objectBelongedClassName_field = objectBelongedClassName_field;

		this.isObjectStatic_field = isObjectStatic_field;

		this.opCode_field = opCode_field;
	}

	public String getObjectName_field() {
		return objectName_field;
	}

	public String getObjectType_field() {
		return objectType_field;
	}

	public String getObjectBelongedClassName_field() {
		return objectBelongedClassName_field;
	}

	public boolean isObjectStatic_field() {
		return isObjectStatic_field;
	}

	public String getOpCode_field() {
		return opCode_field;
	}

	public int getLocalVarSlot(String localVarOpCode) {
		String slotAsString = localVarOpCode.substring(localVarOpCode
				.indexOf("_") + 1);

		return Integer.parseInt(slotAsString);
	}

}
