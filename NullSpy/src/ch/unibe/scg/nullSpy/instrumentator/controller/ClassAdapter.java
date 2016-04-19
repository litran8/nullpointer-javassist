package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import ch.unibe.scg.nullSpy.model.Variable;

/**
 * Iterates through the whole class and instrument a test-code after each field
 * or locVar.
 * 
 * @author Lina Tran
 *
 */
public class ClassAdapter {

	private static ClassAdapter instance;

	private ArrayList<Variable> fieldIsWritterInfoList = new ArrayList<Variable>();
	private ArrayList<Variable> localVarList = new ArrayList<>();

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

		System.out.println("\n\nCLASS: " + cc.getName());
		System.out.println("\n------------- FIELD -------------\n");

		FieldAnalyzer fieldLogic = new FieldAnalyzer(cc, fieldIsWritterInfoList);
		fieldLogic.instrumentAfterFieldAssignment();

		System.out.println("\n------------- LOCAL VAR -------------\n");

		LocalVariableAnalyzer locVarLogic = new LocalVariableAnalyzer(cc,
				localVarList);
		locVarLogic.instrumentAfterLocVarAssignment();
	}
}