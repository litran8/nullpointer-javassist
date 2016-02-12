package javassistPackage;

import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

public class RegexNullAssignment {

	public static String className = "isFieldOrLocalVariableNull.AssignToNull";

	// public static String className =
	// "isFieldOrLocalVariableNull.NullPointerException";

	// public static String className = "isFieldOrLocalVariableNull.TestClass";

	public static void main(String[] args) throws Exception {
		CtClass cc = ClassPool.getDefault().get(className);
		cc.stopPruning(true);

		// not possible, because the WHOLE main method will be in the try
		// block...
		// final String beforeMethod =
		// "{long startTime = System.currentTimeMillis(); System.out.println(\"Before Foo\");";
		// final String afterMethod =
		// "finally {long diff = System.currentTimeMillis() - startTime; System.out.println(\"Foo completed in:\" + diff);}}";
		// CtMethod m = cc.getDeclaredMethod("main");
		// m.insertBefore("try{");
		// m.insertAfter("}");
		// CtClass eType = ClassPool.getDefault().get("java.lang.Throwable");
		// // m.setBody(beforeMethod + " try {$proceed($$); } " + afterMethod);
		// m.addCatch("{System.out.println(\"Java ERROR: \"+$e); throw $e;}",
		// eType);

		// Field/InsanceVar
		searchAndStoreField(cc);

		for (Field f : MyClass.getFieldMap().keySet()) {
			f.getMethod().insertAt(
					f.getFieldLineNumber() + 1,
					"javassistPackage.MyClass.test( \"" + cc.getName() + "\","
							+ f.getFieldName() + "," + f.getFieldLineNumber()
							+ ");");
		}

		// LocVar
		for (CtMethod method : cc.getDeclaredMethods()) {

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

			// store current instruction and the one before
			ArrayList<Integer> instrIndex = new ArrayList<Integer>();
			int instrCounter = 0;
			int instrBeforeOp = 0;

			searchAndStorLocVar(method, codeIterator, localVarTable,
					lineNrTableList, lineNrTableValue, instrIndex,
					instrCounter, instrBeforeOp);
		}

		for (LocalVar v : MyClass.getLocalVarMap().keySet()) {
			v.getCtMethod().insertAt(
					v.getLocalVarLineNr() + 1,
					"javassistPackage.MyClass.test( \"" + cc.getName() + "\","
							+ v.getLocalVarName() + "," + v.getLocalVarLineNr()
							+ ");");
		}

		Class<?> c = cc.toClass();
		if (hasMainMethod(cc.getDeclaredMethods()))
			c.getDeclaredMethod("main", new Class[] { String[].class }).invoke(
					null, new Object[] { args });

	}

	private static void searchAndStorLocVar(CtMethod method,
			CodeIterator codeIterator, LocalVariableAttribute localVarTable,
			ArrayList<Integer> lineNrTableList,
			ArrayList<Integer> lineNrTableValue, ArrayList<Integer> instrIndex,
			int instrCounter, int instrBeforeOp) throws BadBytecode {

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
				if (isLocVarObject(op)) {
					if (!Mnemonic.OPCODE[instrBeforeOp].matches("goto.*")
							&& index <= lineNrTableList.get(lineNrTableList
									.size() - 1)) {

						int r = getLocVarTableIndex(codeIterator,
								localVarTable, index, op);

						MyClass.storeLocVar(
								localVarTable.variableName(r),
								getLocVarLineNr(lineNrTableList,
										lineNrTableValue, index), method, r);
					}
				}
			}
		}
	}

	private static boolean hasMainMethod(CtMethod[] methods) {
		for (CtMethod m : methods) {
			if (m.getName().equals("main"))
				return true;
		}
		return false;

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

	private static boolean isLocVarObject(int op) {
		return Mnemonic.OPCODE[op].matches("a{1,2}store.*");
	}

	private static boolean isLocVar(int op) {
		return Mnemonic.OPCODE[op].matches(".*store.*");
	}

	// search all fields; store fieldLineNumbers and fieldNames
	private static void searchAndStoreField(CtClass cc)
			throws CannotCompileException {

		cc.instrument(new ExprEditor() {
			public void edit(FieldAccess arg) throws CannotCompileException {
				if (arg.isWriter()) {

					if (arg.getLineNumber() > cc.getDeclaredMethods()[0]
							.getMethodInfo().getLineNumber(0)) {
						try {

							// System.out.println(arg.getLineNumber());
							MyClass.storeField(
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
		if (op >= 75 && op <= 78)
			return Integer.parseInt(Mnemonic.OPCODE[op].substring(
					Mnemonic.OPCODE[op].length() - 1,
					Mnemonic.OPCODE[op].length()));
		else
			return codeIterator.u16bitAt(index) - 14848;
	}
}
