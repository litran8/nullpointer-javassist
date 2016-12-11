package ch.unibe.scg.nullSpy.instrumentor.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LineNumberAttribute;
import ch.unibe.scg.nullSpy.instrumentor.controller.methodInvocation.MethodInvocationAnalyzer;

/**
 * Iterates through class file and instrument check method after assignment to a
 * variable
 * 
 * @author Lina Tran
 *
 */
public class ClassAdapter {

	private static ClassAdapter instance;

	public static ClassAdapter getInstance() {
		if (instance == null) {
			instance = new ClassAdapter();
		}
		return instance;
	}

	public void adaptProject(CtClass cc) throws NotFoundException,
			CannotCompileException, BadBytecode, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, IOException {

		CtBehavior[] constructors = cc.getDeclaredConstructors();

		// escape libs (they don't have lineNrAttr)
		if (constructors.length != 0) {
			CtBehavior constructor = constructors[0];
			CodeAttribute codeAttr = constructor.getMethodInfo()
					.getCodeAttribute();
			if (codeAttr != null) {
				LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
						.getAttribute(LineNumberAttribute.tag);
				if (lineNrAttr == null) {
					return;
				}
			}

		}

		// debugging used
		// if (!cc.getName().equals("org.jhotdraw.applet.DrawApplet"))
		// return;

		System.out.println("\n\nCLASS: " + cc.getName());

		System.out.println("\n------------- INVOKES -------------\n");

		MethodInvocationAnalyzer methodInvokationAnalyzer = new MethodInvocationAnalyzer(
				cc);
		methodInvokationAnalyzer.getMethodReceiver();

		System.out.println("\n------------- FIELD -------------\n");

		FieldAnalyzer fieldAnalyzer = new FieldAnalyzer(cc);
		fieldAnalyzer.instrumentAfterFieldAssignment();

		System.out.println("\n------------- LOCAL VAR -------------\n");

		LocalVariableAnalyzer localVarAnalyzer = new LocalVariableAnalyzer(cc);
		localVarAnalyzer.instrumentAfterLocVarAssignment();

		MainBehaviorModifier.addTryCatchToMainMethod(cc);

		System.out.println();
	}
}