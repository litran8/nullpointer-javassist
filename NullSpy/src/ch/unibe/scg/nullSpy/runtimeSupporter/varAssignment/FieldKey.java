package ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment;

public class FieldKey extends Key {

	private String varType;
	private String varDeclaringClassName;
	private boolean isVarStatic;

	private String indirectVarName;
	private String indirectVarType;
	private String indirectVarDeclaringClassName;
	private boolean isIndirectVarStatic;

	public FieldKey(String classNameInWhichVarIsUsed, String varName,
			String varType, String varDeclaringClassName, boolean isVarStatic,
			String indirectVarName, String indirectVarType,
			String indirectVarDeclaringClassName, boolean isIndirectVarStatic,
			String behaviorName, String behaviorSignature) {
		super(varName, classNameInWhichVarIsUsed, behaviorName,
				behaviorSignature, "field");
		this.varType = varType;
		this.varDeclaringClassName = varDeclaringClassName;
		this.isVarStatic = isVarStatic;

		this.indirectVarName = indirectVarName;
		this.indirectVarType = indirectVarType;
		this.indirectVarDeclaringClassName = indirectVarDeclaringClassName;
		this.isIndirectVarStatic = isIndirectVarStatic;
	}

	public String getVarType() {
		return varType;
	}

	public void setVarType(String varType) {
		this.varType = varType;
	}

	public String getVarDeclaringClassName() {
		return varDeclaringClassName;
	}

	public void setVarDeclaringClassName(String varDeclaringClassName) {
		this.varDeclaringClassName = varDeclaringClassName;
	}

	public boolean isVarStatic() {
		return isVarStatic;
	}

	public void setVarStatic(boolean isVarStatic) {
		this.isVarStatic = isVarStatic;
	}

	public String getIndirectVarName() {
		return indirectVarName;
	}

	public void setIndirectVarName(String indirectVarName) {
		this.indirectVarName = indirectVarName;
	}

	public String getIndirectVarType() {
		return indirectVarType;
	}

	public void setIndirectVarType(String indirectVarType) {
		this.indirectVarType = indirectVarType;
	}

	public String getIndirectVarDeclaringClassName() {
		return indirectVarDeclaringClassName;
	}

	public void setIndirectVarDeclaringClassName(
			String indirectVarDeclaringClassName) {
		this.indirectVarDeclaringClassName = indirectVarDeclaringClassName;
	}

	public boolean isIndirectVarStatic() {
		return isIndirectVarStatic;
	}

	public void setIndirectVarStatic(boolean isIndirectVarStatic) {
		this.isIndirectVarStatic = isIndirectVarStatic;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((indirectVarDeclaringClassName == null) ? 0
						: indirectVarDeclaringClassName.hashCode());
		result = prime * result
				+ ((indirectVarName == null) ? 0 : indirectVarName.hashCode());
		result = prime * result
				+ ((indirectVarType == null) ? 0 : indirectVarType.hashCode());
		result = prime * result + (isIndirectVarStatic ? 1231 : 1237);
		result = prime * result + (isVarStatic ? 1231 : 1237);
		result = prime
				* result
				+ ((varDeclaringClassName == null) ? 0 : varDeclaringClassName
						.hashCode());
		result = prime * result + ((varType == null) ? 0 : varType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldKey other = (FieldKey) obj;
		if (indirectVarDeclaringClassName == null) {
			if (other.indirectVarDeclaringClassName != null)
				return false;
		} else if (!indirectVarDeclaringClassName
				.equals(other.indirectVarDeclaringClassName))
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
		if (varDeclaringClassName == null) {
			if (other.varDeclaringClassName != null)
				return false;
		} else if (!varDeclaringClassName.equals(other.varDeclaringClassName))
			return false;
		if (varType == null) {
			if (other.varType != null)
				return false;
		} else if (!varType.equals(other.varType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + "\nFieldKey [varType=" + varType
				+ ",\nvarDeclaringClassName=" + varDeclaringClassName
				+ ",\nisVarStatic=" + isVarStatic + ",\nindirectVarName="
				+ indirectVarName + ", \nindirectVarType=" + indirectVarType
				+ ", \nindirectVarDeclaringClassName="
				+ indirectVarDeclaringClassName + ", \nisIndirectVarStatic="
				+ isIndirectVarStatic + ", \ngetVarType()=" + getVarType()
				+ ", \ngetVarDeclaringClassName()="
				+ getVarDeclaringClassName() + ", \nisVarStatic()="
				+ isVarStatic() + ", \ngetIndirectVarName()="
				+ getIndirectVarName() + ", \ngetIndirectVarType()="
				+ getIndirectVarType()
				+ ", \ngetIndirectVarDeclaringClassName()="
				+ getIndirectVarDeclaringClassName()
				+ ", \nisIndirectVarStatic()=" + isIndirectVarStatic()
				+ ", \nhashCode()=" + hashCode() + "]";
	}

}
