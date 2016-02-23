package model;

import javassist.CtMethod;

public class LocalVar {

	private String locVarName;
	private int locVarLineNr;
	private CtMethod ctMethod;

	public LocalVar(String locVarName, int locVarLnNr, CtMethod m) {
		this.locVarName = locVarName;
		this.locVarLineNr = locVarLnNr;
		this.ctMethod = m;
	}

	public CtMethod getCtMethod() {
		return ctMethod;
	}

	public void setCtMethod(CtMethod ctMethod) {
		this.ctMethod = ctMethod;
	}

	public String getLocalVarName() {
		return locVarName;
	}

	public void setLocalVarName(String localVarName) {
		this.locVarName = localVarName;
	}

	public int getLocalVarLineNr() {
		return locVarLineNr;
	}

	public void setLocalVarLineNr(int localVarLineNr) {
		this.locVarLineNr = localVarLineNr;
	}
}