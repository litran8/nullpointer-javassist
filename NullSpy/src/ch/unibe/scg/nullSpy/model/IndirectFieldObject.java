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

	public IndirectFieldObject(String objectName_field,
			String objectType_field, String objectBelongedClassName_field) {

		this.objectName_field = objectName_field;
		this.objectType_field = objectType_field;
		this.objectBelongedClassName_field = objectBelongedClassName_field;
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

}
