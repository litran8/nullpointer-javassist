package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LineNumberAttribute;
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

		CtBehavior[] constructors = cc.getDeclaredConstructors();

		if (constructors.length != 0) {
			CtBehavior constructor = constructors[0];
			CodeAttribute codeAttr = constructor.getMethodInfo()
					.getCodeAttribute();
			if (codeAttr != null) {
				LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
						.getAttribute(LineNumberAttribute.tag);
				if (lineNrAttr == null)
					return;
			}

		}

		System.out.println("\n\nCLASS: " + cc.getName());
		System.out.println("\n------------- FIELD -------------\n");

		FieldAnalyzer fieldLogic = new FieldAnalyzer(cc, fieldIsWritterInfoList);
		fieldLogic.instrumentAfterFieldAssignment();

		System.out.println("\n------------- LOCAL VAR -------------\n");

		LocalVariableAnalyzer locVarLogic = new LocalVariableAnalyzer(cc,
				localVarList);
		locVarLogic.instrumentAfterLocVarAssignment();
		System.out.println();
	}
}