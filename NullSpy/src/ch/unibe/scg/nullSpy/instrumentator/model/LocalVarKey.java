package ch.unibe.scg.nullSpy.instrumentator.model;

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
}
