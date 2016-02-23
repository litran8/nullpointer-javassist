package controller;

import java.lang.reflect.InvocationTargetException;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import model.AnalyzedClassData;
import model.Field;
import model.LocalVar;
import model.MyClass;

public class Iteration {

	private static Iteration instance;
	private AnalyzedClassData analyzedClassData;

	private Iteration() {
		analyzedClassData = new AnalyzedClassData();
	}

	public static Iteration getInstance() {
		if (instance == null) {
			instance = new Iteration();
		}
		return instance;
	}

	public void goThrough(CtClass ctClass) throws NotFoundException,
			CannotCompileException, BadBytecode, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {

		CtClass cc;

		cc = ctClass;

		MyClass myClass = new MyClass(cc);
		analyzedClassData.addClass(myClass);

		// Field
		FieldLogic fieldLogic = new FieldLogic(cc, myClass);
		fieldLogic.searchAndStoreField();

		// LocVar
		LocVarLogic locVarLogic = new LocVarLogic(cc, myClass);
		locVarLogic.searchAndStoreLocVar();

		for (Field f : myClass.getFieldMap().keySet()) {
			f.getMethod().insertAt(
					f.getFieldLineNumber() + 1,
					"controller.Iteration.getInstance().test( \""
							+ cc.getName() + "\"," + f.getFieldName() + ","
							+ f.getFieldLineNumber() + ",\"" + f.getFieldName()
							+ "\");");
		}

		for (LocalVar v : myClass.getLocalVarMap().keySet()) {
			v.getCtMethod().insertAt(
					v.getLocalVarLineNr() + 1,
					"controller.Iteration.getInstance().test( \""
							+ cc.getName() + "\"," + v.getLocalVarName() + ","
							+ v.getLocalVarLineNr() + ",\""
							+ v.getLocalVarName() + "\");");
		}
	}

	public static void test(String className, Object obj, int lineNr,
			String objName) {
		if (obj == null) {

			// System.out.print(isField(lineNr) ? "Field " : "Local variable ");
			System.out.print(objName + " at line " + lineNr + " is null: ");
			System.out.println(getNullLink(className, lineNr));
		}
	}

	private static String getNullLink(String className, int lineNumber) {
		String nullLink;
		nullLink = "(" + className + ".java:" + lineNumber + ")";
		return nullLink;
	}

	// ------------------ Getters and Setters ------------------

	public AnalyzedClassData getAnalyzedClassData() {
		return analyzedClassData;
	}

}