package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LineNumberAttribute;
import ch.unibe.scg.nullSpy.instrumentator.model.Field;
import ch.unibe.scg.nullSpy.instrumentator.model.FieldKey;
import ch.unibe.scg.nullSpy.instrumentator.model.LocalVar;
import ch.unibe.scg.nullSpy.instrumentator.model.LocalVarKey;
import ch.unibe.scg.nullSpy.instrumentator.model.Variable;

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
	private HashMap<FieldKey, Field> fieldMap = new HashMap<>();
	private ArrayList<Variable> localVarList = new ArrayList<>();
	private HashMap<LocalVarKey, LocalVar> localVarMap = new HashMap<>();

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

		// escape libs (they don't have lineNrAttr)
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

		// System.out.println("\n------------- INVOKES -------------\n");

		System.out.println("\n------------- FIELD -------------\n");

		FieldAnalyzer fieldAnalyzer = new FieldAnalyzer(cc,
				fieldIsWritterInfoList, fieldMap);
		fieldAnalyzer.instrumentAfterFieldAssignment();

		System.out.println("\n------------- LOCAL VAR -------------\n");

		LocalVariableAnalyzer localVarAnalyzer = new LocalVariableAnalyzer(cc,
				localVarList, localVarMap);
		localVarAnalyzer.instrumentAfterLocVarAssignment();

		// CtBehavior mainBehavior = cc.getDeclaredMethod("main");
		//
		// if (mainBehavior != null) {
		// CtClass etype = ClassPool.getDefault().get(
		// "java.lang.NullPointerException");
		// mainBehavior.addCatch("{System.out.println($e); throw $e;}", etype);
		// }

		// MethodCallAnalyzer methodCallAnalyzer = new MethodCallAnalyzer(cc,
		// fieldIsWritterInfoList, localVarList);
		// methodCallAnalyzer.checkInvokes();
		System.out.println();
	}
}