package ch.unibe.scg.nullSpy.model;

public class FieldKey {

	public int keySize;

	public String varName;
	public String varType;
	public String classNameInWhichVarIsInstantiated;
	public boolean isVarStatic;

	public String indirectVarName;

	public String behaviorDeclaredInClassName;
	public String behaviorName;
	public String behaviorSignature;

	public String indirectClassNameInWhichVarIsInstantiated;

	public String indirectVarType;
	public boolean isIndirectVarStatic;

	public FieldKey(String varName, String classNameInWhichVarIsInstantiated) {
		this.keySize = 2;
		this.varName = varName;
		this.classNameInWhichVarIsInstantiated = classNameInWhichVarIsInstantiated;
	}

	public FieldKey(String varName, String varType, boolean isVarStatic) {
		this.keySize = 3;
		this.varName = varName;
		this.varType = varType;
		this.isVarStatic = isVarStatic;
	}

	public FieldKey(String varName, String classNameInWhichVarIsInstantiated,
			String indirectVarName, String behaviorDeclaredInClassName,
			String behaviorName, String behaviorSignature) {
		this.keySize = 6;
		this.varName = varName;
		this.classNameInWhichVarIsInstantiated = classNameInWhichVarIsInstantiated;
		this.indirectVarName = indirectVarName;
		this.behaviorDeclaredInClassName = behaviorDeclaredInClassName;
		this.behaviorName = behaviorName;
		this.behaviorSignature = behaviorSignature;
	}

	public FieldKey(String varName, String classNameInWhichVarIsInstantiated,
			String indirectVarName,
			String indirectClassNameInWhichVarIsInstantiated) {
		this.keySize = 4;
		this.varName = varName;
		this.classNameInWhichVarIsInstantiated = classNameInWhichVarIsInstantiated;
		this.indirectVarName = indirectVarName;
		this.indirectClassNameInWhichVarIsInstantiated = indirectClassNameInWhichVarIsInstantiated;
	}

	public FieldKey(String varName, String classNameInWhichVarIsInstantiated,
			String indirectVarName, String indirectVarType,
			boolean isIndirectVarStatic) {
		this.keySize = 5;
		this.varName = varName;
		this.classNameInWhichVarIsInstantiated = classNameInWhichVarIsInstantiated;
		this.indirectVarName = indirectVarName;
		this.indirectVarType = indirectVarType;
		this.isIndirectVarStatic = isIndirectVarStatic;
	}

	public int getKeySize() {
		return keySize;
	}

	public String getVarName() {
		return varName;
	}

	public String getVarType() {
		return varType;
	}

	public String getClassNameInWhichVarIsInstantiated() {
		return classNameInWhichVarIsInstantiated;
	}

	public boolean isVarStatic() {
		return isVarStatic;
	}

	public String getIndirectVarName() {
		return indirectVarName;
	}

	public String getBehaviorDeclaredInClassName() {
		return behaviorDeclaredInClassName;
	}

	public String getBehaviorName() {
		return behaviorName;
	}

	public String getBehaviorSignature() {
		return behaviorSignature;
	}

	public String getIndirectClassNameInWhichVarIsInstantiated() {
		return indirectClassNameInWhichVarIsInstantiated;
	}

	public String getIndirectVarType() {
		return indirectVarType;
	}

	public boolean isIndirectVarStatic() {
		return isIndirectVarStatic;
	}

}
