package ch.unibe.scg.nullSpy.runtimeSupporter;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((behaviorDeclaredInClassName == null) ? 0
						: behaviorDeclaredInClassName.hashCode());
		result = prime * result
				+ ((behaviorName == null) ? 0 : behaviorName.hashCode());
		result = prime
				* result
				+ ((behaviorSignature == null) ? 0 : behaviorSignature
						.hashCode());
		result = prime
				* result
				+ ((classNameInWhichVarIsInstantiated == null) ? 0
						: classNameInWhichVarIsInstantiated.hashCode());
		result = prime
				* result
				+ ((indirectClassNameInWhichVarIsInstantiated == null) ? 0
						: indirectClassNameInWhichVarIsInstantiated.hashCode());
		result = prime * result
				+ ((indirectVarName == null) ? 0 : indirectVarName.hashCode());
		result = prime * result
				+ ((indirectVarType == null) ? 0 : indirectVarType.hashCode());
		result = prime * result + (isIndirectVarStatic ? 1231 : 1237);
		result = prime * result + (isVarStatic ? 1231 : 1237);
		result = prime * result + keySize;
		result = prime * result + ((varName == null) ? 0 : varName.hashCode());
		result = prime * result + ((varType == null) ? 0 : varType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldKey other = (FieldKey) obj;
		if (behaviorDeclaredInClassName == null) {
			if (other.behaviorDeclaredInClassName != null)
				return false;
		} else if (!behaviorDeclaredInClassName
				.equals(other.behaviorDeclaredInClassName))
			return false;
		if (behaviorName == null) {
			if (other.behaviorName != null)
				return false;
		} else if (!behaviorName.equals(other.behaviorName))
			return false;
		if (behaviorSignature == null) {
			if (other.behaviorSignature != null)
				return false;
		} else if (!behaviorSignature.equals(other.behaviorSignature))
			return false;
		if (classNameInWhichVarIsInstantiated == null) {
			if (other.classNameInWhichVarIsInstantiated != null)
				return false;
		} else if (!classNameInWhichVarIsInstantiated
				.equals(other.classNameInWhichVarIsInstantiated))
			return false;
		if (indirectClassNameInWhichVarIsInstantiated == null) {
			if (other.indirectClassNameInWhichVarIsInstantiated != null)
				return false;
		} else if (!indirectClassNameInWhichVarIsInstantiated
				.equals(other.indirectClassNameInWhichVarIsInstantiated))
			return false;
		if (indirectVarName == null) {
			if (other.indirectVarName != null)
				return false;
		} else if (!indirectVarName.equals(other.indirectVarName))
			return false;
		if (indirectVarType == null) {
			if (other.indirectVarType != null)
				return false;
		} else if (!indirectVarType.equals(other.indirectVarType))
			return false;
		if (isIndirectVarStatic != other.isIndirectVarStatic)
			return false;
		if (isVarStatic != other.isVarStatic)
			return false;
		if (keySize != other.keySize)
			return false;
		if (varName == null) {
			if (other.varName != null)
				return false;
		} else if (!varName.equals(other.varName))
			return false;
		if (varType == null) {
			if (other.varType != null)
				return false;
		} else if (!varType.equals(other.varType))
			return false;
		return true;
	}

}
