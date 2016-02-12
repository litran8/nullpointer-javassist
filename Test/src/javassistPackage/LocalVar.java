package javassistPackage;

import javassist.CtMethod;

public class LocalVar {

	private String localVarName;
	private int localVarLineNr;
	private CtMethod ctMethod;

	public LocalVar(String locVarName, int locVarLnNr, CtMethod m) {
		this.localVarName = locVarName;
		this.localVarLineNr = locVarLnNr;
		this.ctMethod = m;
	}

	public CtMethod getCtMethod() {
		return ctMethod;
	}

	public void setCtMethod(CtMethod ctMethod) {
		this.ctMethod = ctMethod;
	}

	public String getLocalVarName() {
		return localVarName;
	}

	public void setLocalVarName(String localVarName) {
		this.localVarName = localVarName;
	}

	public int getLocalVarLineNr() {
		return localVarLineNr;
	}

	public void setLocalVarLineNr(int localVarLineNr) {
		this.localVarLineNr = localVarLineNr;
	}
}