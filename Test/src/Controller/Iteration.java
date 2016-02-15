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
import javassist.bytecode.Opcode;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import Modell.AnalyzedClassData;
import Modell.Field;
import Modell.LocalVar;
import Modell.MyClass;

public class Iteration implements Opcode {

	private static Iteration instance;
	private AnalyzedClassData analyzedClassData;
	private HashMap<String, CtClass> analyzedClasses;

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

	public void goThrough(CtClass cTClass) throws NotFoundException,
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
		searchAndStoreField(cc, myClass);

		isFieldMethodCallOfSameClass(cc, myClass);

		// LocVar
		for (CtMethod method : cc.getDeclaredMethods()) {

			CodeAttribute codeAttribute = method.getMethodInfo()
					.getCodeAttribute();
			CodeIterator codeIterator = codeAttribute.iterator();
			codeIterator.begin();

			LocalVariableAttribute locVarTable = (LocalVariableAttribute) codeAttribute
					.getAttribute(javassist.bytecode.LocalVariableAttribute.tag);

			LineNumberAttribute lineNrTable = (LineNumberAttribute) codeAttribute
					.getAttribute(LineNumberAttribute.tag);

			// store lineNrTable into ArrayLists (because directly get lineNr
			// changed the lineNrTable somehow...
			ArrayList<Integer> lineNrTablePc = new ArrayList<Integer>();
			ArrayList<Integer> lineNrTableLine = new ArrayList<Integer>();

			for (int j = 0; j < lineNrTable.tableLength(); j++) {
				lineNrTablePc.add(lineNrTable.startPc(j));
				lineNrTableLine.add(lineNrTable.lineNumber(j));
			}

			codeIterator.begin();

			searchAndStoreLocVar(cc, myClass, method, codeIterator,
					locVarTable, lineNrTablePc, lineNrTableLine);

			// print(method);
		}

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

	private void isFieldMethodCallOfSameClass(CtClass cc, MyClass myClass)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, NotFoundException, CannotCompileException,
			BadBytecode {
		System.out.println("\n--- INVOKE OF FIELDS STARTS ---");

		HashMap<Field, FieldAccess> fields = myClass.getFieldMap();

		for (Field f : fields.keySet()) {

			LineNumberAttribute lnNrTable = (LineNumberAttribute) f.getMethod()
					.getMethodInfo().getCodeAttribute()
					.getAttribute(LineNumberAttribute.tag);
			ArrayList<Integer> lnNrTableList = new ArrayList<Integer>();
			ArrayList<Integer> lnNrTableValue = new ArrayList<Integer>();
			CodeIterator codeIter;

			for (int k = 0; k < lnNrTable.tableLength(); k++) {
				lnNrTableList.add(lnNrTable.startPc(k));
				lnNrTableValue.add(lnNrTable.lineNumber(k));
			}

			codeIter = f.getMethod().getMethodInfo().getCodeAttribute()
					.iterator();
			for (int i = 0; i < lnNrTableValue.size(); i++) {
				if (f.getFieldLineNumber() == lnNrTableValue.get(i)) {
					int line = lnNrTableValue.get(i);
					// System.out.println(lnNrTable.toNearPc(line - 1).index);
					int startPcOfField = lnNrTable.toNearPc(line - 1).index;

					int op = codeIter.byteAt(startPcOfField);
					if (Mnemonic.OPCODE[op].matches("invoke.*")) {
						// print(f.getMethod());
						System.out.println("Field name: " + f.getFieldName()
								+ "\tField lineNr: " + f.getFieldLineNumber()
								+ "\tField lineNr: " + cc.getName());
						printInstrAtPos(cc, f.getMethod(), codeIter,
								startPcOfField);
					}
					i = lnNrTableValue.size();
				}
			}
		}

		System.out.println("--- INVOKE OF FIELDS ENDS ---\n");
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
	 * Search all fields; store <(name, lineNr, method), fieldAccess>
	 * 
	 * @param cc
	 * @param myClass
	 * @throws CannotCompileException
	 */
	private static void searchAndStoreField(CtClass cc, MyClass myClass)
			throws CannotCompileException {

		cc.instrument(new ExprEditor() {
			public void edit(FieldAccess arg) throws CannotCompileException {
				if (arg.isWriter()) {

					if (arg.getLineNumber() > cc.getDeclaredMethods()[0]
							.getMethodInfo().getLineNumber(0)) {
						try {

							// System.out.println(arg.indexOfBytecode());
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

	/**
	 * Search all locVar; store <(name, lineNr, method), varIndexInLocVarTables>
	 * 
	 * @param cc
	 * @param myClass
	 * @param method
	 * @param codeIterator
	 * @param localVarTable
	 * @param lineNrTablePc
	 * @param lineNrTableLine
	 * @throws BadBytecode
	 * @throws NotFoundException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws CannotCompileException
	 */
	private void searchAndStoreLocVar(CtClass cc, MyClass myClass,
			CtMethod method, CodeIterator codeIterator,
			LocalVariableAttribute localVarTable,
			ArrayList<Integer> lineNrTablePc, ArrayList<Integer> lineNrTableLine)
			throws BadBytecode, NotFoundException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			CannotCompileException {

		// store current instruction and the previous instructions
		ArrayList<Integer> instrPositions = new ArrayList<Integer>();

		int instrCounter = 0;
		int prevInstrOp = 0;
		while (codeIterator.hasNext()) {
			int pos = codeIterator.next();
			instrPositions.add(pos);

			int op = codeIterator.byteAt(pos);

			if (instrCounter > 0)
				prevInstrOp = codeIterator.byteAt(instrPositions
						.get(instrCounter - 1));
			instrCounter++;

			// check if it's NOT a primitive one
			if (isLocVarObject(op)
					&& (!Mnemonic.OPCODE[prevInstrOp].matches("goto.*") && pos <= lineNrTablePc
							.get(lineNrTablePc.size() - 1))) {

				int locVarIndexInLocVarTable = getLocVarIndexInLocVarTable(
						codeIterator, localVarTable, pos, op);

				// store locVar
				String varName = localVarTable
						.variableName(locVarIndexInLocVarTable);
				int varSourceLineNr = getLocVarLineNrInSourceCode(
						lineNrTablePc, lineNrTableLine, pos);

				myClass.storeLocVar(varName, varSourceLineNr, method,
						locVarIndexInLocVarTable);

				// locVar methodCalls
				int invokePos = getInvokePos(prevInstrOp, codeIterator,
						instrPositions, instrCounter);

				if (isLocVarCallingMethod(prevInstrOp, codeIterator,
						instrPositions, instrCounter)) {
					System.out.println("\n--- INVOKE OF LOCVAR STARTS ---");
					System.out.println("Var name: " + varName
							+ "\tVar lineNr: " + varSourceLineNr + "\tClass: "
							+ cc.getName());
					printInstrAtPos(cc, method, codeIterator, invokePos);
					System.out.println("--- INVOKE OF LOCVAR ENDS ---\n");
					isLocVarMethodCallOfSameClass(cc, method, codeIterator,
							invokePos);
				}

			}
			// }
		}
	}

	/**
	 * Gets the pos of the invokeInstr
	 * 
	 * @param prevInstrOp
	 * @param instrPositions
	 * @param instrCounter
	 * @param codeIter
	 * @return pos of invokeInstr of locVar
	 */
	private int getInvokePos(int prevInstrOp, CodeIterator codeIter,
			ArrayList<Integer> instrPositions, int instrCounter) {

		if (Mnemonic.OPCODE[prevInstrOp].matches("invoke.*"))
			return instrPositions.get(instrCounter - 2);
		else if ((Mnemonic.OPCODE[prevInstrOp].matches(".*cast.*") && Mnemonic.OPCODE[codeIter
				.byteAt(instrPositions.get(instrCounter - 3))]
				.matches("invoke.*"))) {
			return instrPositions.get(instrCounter - 3);
		} else
			return 0;
	}

	/**
	 * Checks if the value of the locVar is set by a methodCall
	 * 
	 * @param prevInstrOp
	 * @param codeIter
	 * @param instrPositions
	 * @param instrCounter
	 * @return
	 */
	private boolean isLocVarCallingMethod(int prevInstrOp,
			CodeIterator codeIter, ArrayList<Integer> instrPositions,
			int instrCounter) {
		return Mnemonic.OPCODE[prevInstrOp].matches("invoke.*")
				|| (Mnemonic.OPCODE[prevInstrOp].matches(".*cast.*") && Mnemonic.OPCODE[codeIter
						.byteAt(instrPositions.get(instrCounter - 3))]
						.matches("invoke.*"));
	}

	private void printInstrAtPos(CtClass cc, CtMethod method,
			CodeIterator codeIterator, int instrCounter)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, NotFoundException, CannotCompileException,
			BadBytecode {
		System.out.println(instrCounter
				+ ": "
				+ InstructionPrinter.instructionString(codeIterator,
						instrCounter, method.getMethodInfo2().getConstPool()));
		System.out.println("MethodRefClassName: "
				+ method.getMethodInfo2()
						.getConstPool()
						.getMethodrefClassName(
								codeIterator.u16bitAt(instrCounter + 1)));
		System.out.println("Method: "
				+ method.getMethodInfo2()
						.getConstPool()
						.getMethodrefName(
								codeIterator.u16bitAt(instrCounter + 1)));
		System.out.println();

	}

	/**
	 * Checks if the called method is from the same class as the locVar. If not,
	 * go through the methodRefClass and check for fields and locVars.
	 * 
	 * @param cc
	 * @param method
	 * @param codeIterator
	 * @param instrCounter
	 * @throws NotFoundException
	 * @throws CannotCompileException
	 * @throws BadBytecode
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	private void isLocVarMethodCallOfSameClass(CtClass cc, CtMethod method,
			CodeIterator codeIterator, int instrCounter)
			throws NotFoundException, CannotCompileException, BadBytecode,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		String methodRefClassName = method.getMethodInfo2().getConstPool()
				.getMethodrefClassName(codeIterator.u16bitAt(instrCounter + 1));
		String currentClassName = cc.getName();

		if (!(methodRefClassName.equals(currentClassName))) {
			goThrough(ClassPool.getDefault().get(methodRefClassName));
		}
	}

	/**
	 * Checks if the locVar is an object, NOT a primitive one
	 * 
	 * @param op
	 * @return
	 */
	private static boolean isLocVarObject(int op) {
		return Mnemonic.OPCODE[op].matches("a{1,2}store.*");
	}

	/**
	 * Gets the index of locVar in the locVarTable (Byte code)
	 * 
	 * @param codeIterator
	 * @param localVarTable
	 * @param index
	 * @param op
	 * @return
	 */
	private static int getLocVarIndexInLocVarTable(CodeIterator codeIterator,
			LocalVariableAttribute localVarTable, int index, int op) {
		int r = 0;
		boolean b = true;
		while (b) {
			if (localVarTable.index(r) == getLocVarStackSlot(codeIterator,
					index, op))
				b = false;
			else
				r++;
		}
		return r;
	}

	/**
	 * Gets the lineNr of the locVar in the Source Code
	 * 
	 * @param lineNrTablePc
	 * @param lineNrTableLine
	 * @param pos
	 * @return
	 */
	private static int getLocVarLineNrInSourceCode(
			ArrayList<Integer> lineNrTablePc,
			ArrayList<Integer> lineNrTableLine, int pos) {
		int res = 0;
		boolean b = true;
		int j = 0, k = 1;

		while (b) {
			if (pos < lineNrTablePc.get(k) && pos > lineNrTablePc.get(j)) {
				res = lineNrTableLine.get(j);
				b = false;
			} else {
				j++;
				k++;
			}
		}
		return res;
	}

	/**
	 * Gets the slot/index of the locVar in the locVarStack
	 * 
	 * @param codeIterator
	 * @param index
	 * @param op
	 * @return
	 */
	private static int getLocVarStackSlot(CodeIterator codeIterator, int index,
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
