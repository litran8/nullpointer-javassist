package Modell;

import java.util.HashMap;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.FieldAccess;

public class MyClass {

	private CtClass cc;

	private HashMap<Field, FieldAccess> fieldMap;
	private HashMap<LocalVar, Integer> locVarMap;

	public MyClass(CtClass cc) {
		this.cc = cc;
		this.fieldMap = new HashMap<Field, FieldAccess>();
		this.locVarMap = new HashMap<LocalVar, Integer>();
	}

	public void storeField(String fName, int fLineNumber, CtMethod ctMethod,
			FieldAccess arg) {
		this.fieldMap.put(new Field(fName, fLineNumber, ctMethod), arg);
	}

	public void storeLocVar(String varName, int varLineNumber, CtMethod m,
			int locVarIndexInLocVarTable) {
		locVarMap.put(new LocalVar(varName, varLineNumber, m),
				locVarIndexInLocVarTable);
	}

	public HashMap<Field, FieldAccess> getFieldMap() {
		return fieldMap;
	}

	public HashMap<LocalVar, Integer> getLocalVarMap() {
		return locVarMap;
	}

	public void setLocalVarMap(HashMap<LocalVar, Integer> localVarMap) {
		this.locVarMap = localVarMap;
	}

	public HashMap<LocalVar, Integer> getLocVarMap() {
		return locVarMap;
	}

	public void setLocVarMap(HashMap<LocalVar, Integer> locVarMap) {
		this.locVarMap = locVarMap;
	}

	public void setFieldMap(HashMap<Field, FieldAccess> fieldMap) {
		this.fieldMap = fieldMap;
	}

}