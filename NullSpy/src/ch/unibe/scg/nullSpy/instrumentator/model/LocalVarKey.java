package ch.unibe.scg.nullSpy.instrumentator.model;

public class LocalVarKey extends Key {

	public LocalVarKey(String varName, String classNameInWhichVarIsUsed,
			String behaviorName, String behaviorSignature) {
		super(varName, classNameInWhichVarIsUsed, behaviorName,
				behaviorSignature, "locVar");
	}

}
