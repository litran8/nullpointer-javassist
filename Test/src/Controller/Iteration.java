package Controller;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import Modell.AnalyzedClassData;
import Modell.Field;
import Modell.LocalVar;
import Modell.MyClass;

public class Iteration implements Opcode {

	private static Iteration instance;
	private static AnalyzedClassData analyzedClassData;
	private static HashMap<String, CtClass> analyzedClasses;

	private Iteration() {
		analyzedClassData = new AnalyzedClassData();
		analyzedClasses = new HashMap<String, CtClass>();
	}

	public static Iteration getInstance() {
		if (instance == null) {
			instance = new Iteration();
		}
		return instance;
	}

	public static void goThrough(CtClass cTClass) throws NotFoundException,
			CannotCompileException, BadBytecode, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {

		// System.out.println(cTClass.getName());

		CtClass cc;
		if (!analyzedClasses.containsKey(cTClass.getName())) {
			analyzedClasses.put(cTClass.getName(), cTClass);
			cc = cTClass;
		} else {
			return;
		}

		cc.stopPruning(true);

		MyClass myClass = new MyClass(cc);
		analyzedClassData.addClass(myClass);

		// Field
		FieldLogic fieldLogic = new FieldLogic(cc, myClass);
		fieldLogic.searchAndStoreField();
		fieldLogic.isFieldMethodCallOfSameClass();

		// LocVar
		LocVarLogic locVarLogic = new LocVarLogic(cc, myClass);
		locVarLogic.lookForLocVar();

		for (Field f : myClass.getFieldMap().keySet()) {
			f.getMethod().insertAt(
					f.getFieldLineNumber() + 1,
					"Controller.Iteration.getInstance().test( \""
							+ cc.getName() + "\"," + f.getFieldName() + ","
							+ f.getFieldLineNumber() + ",\"" + f.getFieldName()
							+ "\");");
		}

		for (LocalVar v : myClass.getLocalVarMap().keySet()) {
			v.getCtMethod().insertAt(
					v.getLocalVarLineNr() + 1,
					"Controller.Iteration.getInstance().test( \""
							+ cc.getName() + "\"," + v.getLocalVarName() + ","
							+ v.getLocalVarLineNr() + ",\""
							+ v.getLocalVarName() + "\");");
		}

		// for (CtMethod method : cc.getDeclaredMethods()) {
		//
		// print(method);
		// System.out.println();
		// }

	}

	public static void test(String className, Object objValue, int lineNr,
			String objName) {
		if (objValue == null) {

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

	/**
	 * Prints the instructions and the frame states of the given method.
	 */
	public void print(CtMethod method) {
		// System.out.println("\n" + method.getName());
		MethodInfo info = method.getMethodInfo2();
		ConstPool pool = info.getConstPool();
		CodeAttribute code = info.getCodeAttribute();
		if (code == null)
			return;

		CodeIterator iterator = code.iterator();
		while (iterator.hasNext()) {
			int pos;
			try {
				pos = iterator.next();
			} catch (BadBytecode e) {
				throw new RuntimeException(e);
			}

			String instrString = InstructionPrinter.instructionString(iterator,
					pos, pool);
			System.out.println(pos + ": " + instrString);
		}
	}

	// ------------------ Getters and Setters ------------------

	public AnalyzedClassData getAnalyzedClassData() {
		return analyzedClassData;
	}

	public void setAnalyzedClassData(AnalyzedClassData analyzedClassData) {
		this.analyzedClassData = analyzedClassData;
	}

}
