package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.lang.reflect.InvocationTargetException;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;

public class Iteration {

	private static Iteration instance;

	private Iteration() {
	}

	public static Iteration getInstance() {
		if (instance == null) {
			instance = new Iteration();
		}
		return instance;
	}

	public void instrumentCodeAfterFieldLocVarAssignment(CtClass cc) throws NotFoundException,
			CannotCompileException, BadBytecode, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {

		// Field
		FieldLogic fieldLogic = new FieldLogic(cc);
		fieldLogic.instrumentAfterFieldAssignment();

		// LocVar
		LocVarLogic locVarLogic = new LocVarLogic(cc);
		locVarLogic.instrumentAfterLocVarAssignment();
	}

}