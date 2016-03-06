package ch.unibe.scg.nullSpy.model;

import java.util.HashMap;

import javassist.CtMethod;
import javassist.expr.FieldAccess;

public class FieldAndLocVarContainerOfOneClass {
	private HashMap<Field, FieldAccess> fieldMap;
	private HashMap<LocVar, Integer> locVarMap;

	// private CtClass cc;

	public FieldAndLocVarContainerOfOneClass(/* CtClass cc */) {
		// this.cc = cc;
		this.fieldMap = new HashMap<Field, FieldAccess>();
		this.locVarMap = new HashMap<LocVar, Integer>();
	}

	public void storeFieldIsWriterInfo(String fName, int fLineNumber,
			CtMethod ctMethod, FieldAccess arg) {
		this.fieldMap.put(new Field(fName, fLineNumber, ctMethod), arg);
	}

	public void storeLocVarInfo(String varName, int varLineNumber, CtMethod m,
			int locVarIndexInLocVarTable) {
		locVarMap.put(new LocVar(varName, varLineNumber, m),
				locVarIndexInLocVarTable);
	}

	public HashMap<Field, FieldAccess> getFieldMap() {
		return fieldMap;
	}

	public HashMap<LocVar, Integer> getLocalVarMap() {
		return locVarMap;
	}

	public void setLocalVarMap(HashMap<LocVar, Integer> localVarMap) {
		this.locVarMap = localVarMap;
	}

	public HashMap<LocVar, Integer> getLocVarMap() {
		return locVarMap;
	}

	public void setLocVarMap(HashMap<LocVar, Integer> locVarMap) {
		this.locVarMap = locVarMap;
	}

	public void setFieldMap(HashMap<Field, FieldAccess> fieldMap) {
		this.fieldMap = fieldMap;
	}

	// public CtClass getCtClass() {
	// return cc;
	// }
	//
	// public void setCtClass(CtClass cc) {
	// this.cc = cc;
	// }
}
