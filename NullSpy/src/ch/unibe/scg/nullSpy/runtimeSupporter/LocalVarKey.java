package ch.unibe.scg.nullSpy.runtimeSupporter;

public class LocalVarKey {
	public String varName;

	public String classNameInWhichVarIsUsed;
	public String behaviorName;
	public String behaviorSignature;

	public LocalVarKey(String varName, String classNameInWhichVarIsUsed,
			String behaviorName, String behaviorSignature) {
		this.varName = varName;
		this.classNameInWhichVarIsUsed = classNameInWhichVarIsUsed;
		this.behaviorName = behaviorName;
		this.behaviorSignature = behaviorSignature;
	}

	public String getVarName() {
		return varName;
	}

	public String getClassNameInWhichVarIsUsed() {
		return classNameInWhichVarIsUsed;
	}

	public String getBehaviorName() {
		return behaviorName;
	}

	public String getBehaviorSignature() {
		return behaviorSignature;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((behaviorName == null) ? 0 : behaviorName.hashCode());
		result = prime
				* result
				+ ((behaviorSignature == null) ? 0 : behaviorSignature
						.hashCode());
		result = prime
				* result
				+ ((classNameInWhichVarIsUsed == null) ? 0
						: classNameInWhichVarIsUsed.hashCode());
		result = prime * result + ((varName == null) ? 0 : varName.hashCode());
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
		LocalVarKey other = (LocalVarKey) obj;
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
		if (classNameInWhichVarIsUsed == null) {
			if (other.classNameInWhichVarIsUsed != null)
				return false;
		} else if (!classNameInWhichVarIsUsed
				.equals(other.classNameInWhichVarIsUsed))
			return false;
		if (varName == null) {
			if (other.varName != null)
				return false;
		} else if (!varName.equals(other.varName))
			return false;
		return true;
	}

}
