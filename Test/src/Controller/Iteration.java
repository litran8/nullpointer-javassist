package Controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassistPackage.Field;
import javassistPackage.LocalVar;
import javassistPackage.MyClass;
import Modell.AnalyzedClassData;

public class Iteration {

	private static AnalyzedClassData analyzedClassData;
	private static HashMap<String, CtClass> analyzedClasses;

	private Iteration() {
		analyzedClassData = new AnalyzedClassData();
		analyzedClasses = new HashMap<String, CtClass>();
	}

	public void goThrough(CtClass cTClass) throws NotFoundException,
			CannotCompileException, BadBytecode, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {

		System.out.println(cTClass.getName());

		CtClass cc;
		if (!analyzedClasses.containsKey(cTClass.getName())) {
			analyzedClasses.put(cTClass.getName(), cTClass);
			cc = cTClass;
		} else {
			// cc = analyzedClasses.get(cTClass.getName());
			return;
		}

		cc.stopPruning(true);

		MyClass myClass = new MyClass(cc);

		analyzedClassData.addClass(myClass);
		// Field/InsanceVar
		searchAndStoreField(cc, myClass);

		for (Field f : myClass.getFieldMap().keySet()) {
			f.getMethod().insertAt(
					f.getFieldLineNumber() + 1,
					"Controller.Iteration.getInstance().test( \""
							+ cc.getName() + "\"," + f.getFieldName() + ","
							+ f.getFieldLineNumber() + ",\"" + f.getFieldName()
							+ "\");");
		}

		// LocVar
		for (CtMethod method : cc.getDeclaredMethods()) {
			System.out.println(method.getName());

			// get everything what is needed bellow
			CodeAttribute codeAttribute = method.getMethodInfo()
					.getCodeAttribute();
			CodeIterator codeIterator = codeAttribute.iterator();
			codeIterator.begin();

			LocalVariableAttribute localVarTable = (LocalVariableAttribute) codeAttribute
					.getAttribute(javassist.bytecode.LocalVariableAttribute.tag);

			LineNumberAttribute lineNrTable = (LineNumberAttribute) codeAttribute
					.getAttribute(LineNumberAttribute.tag);

			// store lineNrTable into ArrayLists (because directly get lineNr
			// changed the lineNrTable somehow...
			ArrayList<Integer> lineNrTableList = new ArrayList<Integer>();
			ArrayList<Integer> lineNrTableValue = new ArrayList<Integer>();

			for (int j = 0; j < lineNrTable.tableLength(); j++) {
				lineNrTableList.add(lineNrTable.startPc(j));
				lineNrTableValue.add(lineNrTable.lineNumber(j));
			}

			searchAndStoreLocVar(cc, myClass, method, codeIterator,
					localVarTable, lineNrTableList, lineNrTableValue);

			// print(method);
		}

		for (LocalVar v : myClass.getLocalVarMap().keySet()) {
			v.getCtMethod().insertAt(
					v.getLocalVarLineNr() + 1,
					"Controller.Iteration.getInstance().test( \""
							+ cc.getName() + "\"," + v.getLocalVarName() + ","
							+ v.getLocalVarLineNr() + ",\""
							+ v.getLocalVarName() + "\");");
		}

		for (CtMethod method : cc.getDeclaredMethods()) {

			print(method);
			System.out.println();
		}

	}

	private static Iteration instance;

	public static Iteration getInstance() {
		if (instance == null) {
			instance = new Iteration();
		}
		return instance;
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
		System.out.println("\n" + method.getName());
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

			System.out
					.println(pos
							+ ": "
							+ InstructionPrinter.instructionString(iterator,
									pos, pool));
		}

	}

	private void searchAndStoreLocVar(CtClass cc, MyClass myClass,
			CtMethod method, CodeIterator codeIterator,
			LocalVariableAttribute localVarTable,
			ArrayList<Integer> lineNrTableList,
			ArrayList<Integer> lineNrTableValue) throws BadBytecode,
			NotFoundException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			CannotCompileException {

		// store current instruction and the one before
		ArrayList<Integer> instrIndex = new ArrayList<Integer>();

		int instrCounter = 0;
		int instrBeforeOp = 0;
		while (codeIterator.hasNext()) {
			int index = codeIterator.next();
			instrIndex.add(index);

			int op = codeIterator.byteAt(index);

			if (instrCounter > 0)
				instrBeforeOp = codeIterator.byteAt(instrIndex
						.get(instrCounter - 1));
			instrCounter++;

			// check if it's a locVar
			if (isLocVar(op)) {

				// check if it's NOT a primitive one
				if (isLocVarObject(op)
						&& (!Mnemonic.OPCODE[instrBeforeOp].matches("goto.*") && index <= lineNrTableList
								.get(lineNrTableList.size() - 1))) {

					System.out.println(Mnemonic.OPCODE[op]);
					System.out.println(Mnemonic.OPCODE[instrBeforeOp]);
					int r = getLocVarTableIndex(codeIterator, localVarTable,
							index, op);

					myClass.storeLocVar(
							localVarTable.variableName(r),
							getLocVarLineNr(lineNrTableList, lineNrTableValue,
									index), method, r);

					if (Mnemonic.OPCODE[instrBeforeOp].matches("invoke.*")) {
						printInstrAtPos(cc, method, codeIterator, instrIndex,
								instrIndex.get(instrCounter - 2), index);
					}

					if ((Mnemonic.OPCODE[instrBeforeOp].matches(".*cast.*") && Mnemonic.OPCODE[codeIterator
							.byteAt(instrIndex.get(instrCounter - 3))]
							.matches("invoke.*"))) {
						System.out.println(Mnemonic.OPCODE[codeIterator
								.byteAt(instrIndex.get(instrCounter - 3))]);
						printInstrAtPos(cc, method, codeIterator, instrIndex,
								instrIndex.get(instrCounter - 3), index);
					}

				}
			}
		}
	}

	public static AnalyzedClassData getAnalyzedClassData() {
		return analyzedClassData;
	}

	public static void setAnalyzedClassData(AnalyzedClassData analyzedClassData) {
		Iteration.analyzedClassData = analyzedClassData;
	}

	private void printInstrAtPos(CtClass cc, CtMethod method,
			CodeIterator codeIterator, ArrayList<Integer> instrIndex,
			int instrCounter, int index) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			NotFoundException, CannotCompileException, BadBytecode {
		// System.out.println("index: " + index);
		// System.out.println("index before: " + instrCounter);
		// System.out.println(instrCounter
		// + ": "
		// + InstructionPrinter.instructionString(codeIterator,
		// instrCounter, method.getMethodInfo2().getConstPool()));
		// System.out.println(method.getMethodInfo2().getConstPool()
		// .getMethodrefName(codeIterator.u16bitAt(instrCounter + 1)));
		// System.out
		// .println(method
		// .getMethodInfo2()
		// .getConstPool()
		// .getMethodrefClassName(
		// codeIterator.u16bitAt(instrCounter + 1)));
		//
		// System.out.println();
		if (!(method.getMethodInfo2().getConstPool()
				.getMethodrefClassName(codeIterator.u16bitAt(instrCounter + 1)))
				.equals(cc.getName())) {
			goThrough(ClassPool.getDefault().get(
					method.getMethodInfo2()
							.getConstPool()
							.getMethodrefClassName(
									codeIterator.u16bitAt(instrCounter + 1))));
		}
	}

	private static boolean isLocVarObject(int op) {
		return Mnemonic.OPCODE[op].matches("a{1,2}store.*");
	}

	private static boolean isLocVar(int op) {
		return Mnemonic.OPCODE[op].matches(".*store.*");
	}

	// search all fields; store fieldLineNumbers and fieldNames
	private static void searchAndStoreField(CtClass cc, MyClass myClass)
			throws CannotCompileException {

		cc.instrument(new ExprEditor() {
			public void edit(FieldAccess arg) throws CannotCompileException {
				if (arg.isWriter()) {

					if (arg.getLineNumber() > cc.getDeclaredMethods()[0]
							.getMethodInfo().getLineNumber(0)) {
						try {

							// System.out.println(arg.getLineNumber());
							myClass.storeField(
									arg.getFieldName(),
									arg.getLineNumber(),
									cc.getDeclaredMethod(arg.where()
											.getMethodInfo().getName()), arg);
						} catch (NotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}

	private static int getLocVarTableIndex(CodeIterator codeIterator,
			LocalVariableAttribute localVarTable, int index, int op) {
		int r = 0;
		boolean b = true;
		while (b) {
			if (localVarTable.index(r) == getLocVarIndex(codeIterator, index,
					op))
				b = false;
			else
				r++;
		}
		return r;
	}

	private static int getLocVarLineNr(ArrayList<Integer> lineNrTableList,
			ArrayList<Integer> lineNrTableValue, int index) {
		int res = 0;
		boolean b = true;
		int j = 0;
		int k = 1;
		while (b) {
			if (index < lineNrTableList.get(k)
					&& index > lineNrTableList.get(j)) {
				res = lineNrTableValue.get(j);
				b = false;
			} else {
				j++;
				k++;
			}
		}
		return res;
	}

	// get index of locVar in locVarStack
	private static int getLocVarIndex(CodeIterator codeIterator, int index,
			int op) {
		// check if locVar is stored in astore_0..._3 (one byte)
		// if not it calculates the slot in which it stored by getting the
		// number in the second byte (two bytes)
		if (!Mnemonic.OPCODE[op].matches("astore"))
			return Integer.parseInt(Mnemonic.OPCODE[op].substring(
					Mnemonic.OPCODE[op].length() - 1,
					Mnemonic.OPCODE[op].length()));
		else
			return codeIterator.u16bitAt(index) - 14848;
	}
}
