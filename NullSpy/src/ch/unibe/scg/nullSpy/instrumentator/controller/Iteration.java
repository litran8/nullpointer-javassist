package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.lang.reflect.InvocationTargetException;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import ch.unibe.scg.nullSpy.model.AnalyzedClassData;

/**
 * Iterates through the whole class and instrument a test-code after each field
 * or locVar.
 * 
 * @author Lina Tran
 *
 */
public class Iteration {

	private static Iteration instance;
	private AnalyzedClassData analyzedClassData = new AnalyzedClassData();

	// private FieldAndLocVarContainerOfOneClass fieldLocVarContainer;

	private Iteration() {
	}

	public static Iteration getInstance() {
		if (instance == null) {
			instance = new Iteration();
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
		FieldLogic fieldLogic = new FieldLogic(cc);
		fieldLogic.instrumentAfterFieldAssignment();
		// fieldLocVarContainer.getFieldMap();

		// LocVar
		// LocVarLogic locVarLogic = new LocVarLogic(cc, fieldLocVarContainer);
		LocVarLogic locVarLogic = new LocVarLogic(cc);
		locVarLogic.instrumentAfterLocVarAssignment();
		// fieldLocVarContainer.getLocVarMap();

		// analyzedClassData.addClass(fieldLocVarContainer);
	}
}