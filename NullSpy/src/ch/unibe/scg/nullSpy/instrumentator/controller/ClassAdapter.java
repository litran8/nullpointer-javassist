package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.lang.reflect.InvocationTargetException;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;

/**
 * Iterates through the whole class and instrument a test-code after each field
 * or locVar.
 * 
 * @author Lina Tran
 *
 */
public class ClassAdapter {

	private static ClassAdapter instance;

	// private AnalyzedClassData analyzedClassData = new AnalyzedClassData();

	// private FieldAndLocVarContainerOfOneClass fieldLocVarContainer;

	private ClassAdapter() {
	}

	public static ClassAdapter getInstance() {
		if (instance == null) {
			instance = new ClassAdapter();
		}
		return instance;
	}

	public void instrumentCodeAfterFieldLocVarAssignment(CtClass cc)
			throws NotFoundException, CannotCompileException, BadBytecode,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		// fieldLocVarContainer = new FieldAndLocVarContainerOfOneClass();
		// Field
		// FieldLogic fieldLogic = new FieldLogic(cc, fieldLocVarContainer);
		FieldAnalyzer fieldLogic = new FieldAnalyzer(cc);
		fieldLogic.instrumentAfterFieldAssignment();
		// fieldLocVarContainer.getFieldMap();

		// LocVar
		// LocVarLogic locVarLogic = new LocVarLogic(cc, fieldLocVarContainer);
		LocalVariableAnalyzer locVarLogic = new LocalVariableAnalyzer(cc);
		locVarLogic.instrumentAfterLocVarAssignment();
		// fieldLocVarContainer.getLocVarMap();

		// analyzedClassData.addClass(fieldLocVarContainer);
	}
}