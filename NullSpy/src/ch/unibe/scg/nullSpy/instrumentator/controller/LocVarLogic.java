package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;

/**
 * Instruments test-code after locVars.
 * 
 * @author Lina Tran
 *
 */
public class LocVarLogic extends VariableAnalyzer implements Opcode {

	private CtClass cc;
	// private FieldAndLocVarContainerOfOneClass container;
	private HashMap<String, Integer> methodCallOnLocVarObject = new HashMap<>();

	// public LocVarLogic(CtClass cc,
	// FieldAndLocVarContainerOfOneClass fieldLocVarContainer) {
	// this.cc = cc;
	// this.container = fieldLocVarContainer;
	// }

	public LocVarLogic(CtClass cc) {
		super(cc);
		this.cc = super.cc;
	}

	/**
	 * Checks all locVars in a class and instrument test-code after their
	 * assignments.
	 * 
	 * @throws BadBytecode
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 */
	public void instrumentAfterLocVarAssignment() throws BadBytecode,
			CannotCompileException, NotFoundException {

		for (CtMethod method : cc.getDeclaredMethods()) {

			// calculates the time modified project uses
			addTimeToModifiedProject(method);

			// get everything what is needed for checking locVars in the
			// byte code
			CodeAttribute codeAttribute = method.getMethodInfo()
					.getCodeAttribute();
			CodeIterator codeIterator = codeAttribute.iterator();

			LocalVariableAttribute locVarTable = (LocalVariableAttribute) codeAttribute
					.getAttribute(javassist.bytecode.LocalVariableAttribute.tag);

			HashMap<Integer, Integer> lineNumberMap = getLineNumberTable(method);
			LineNumberAttribute lineNrTable = (LineNumberAttribute) codeAttribute
					.getAttribute(LineNumberAttribute.tag);

			// store lineNrTable into ArrayLists (because directly get lineNr
			// changes the lineNrTable somehow...
			// ArrayList<Integer> lineNrTablePc = new ArrayList<Integer>();
			// ArrayList<Integer> lineNrTableLine = new ArrayList<Integer>();

			// for (int j = 0; j < lineNrTable.tableLength(); j++) {
			// lineNrTablePc.add(lineNrTable.startPc(j));
			// lineNrTableLine.add(lineNrTable.lineNumber(j));
			// }

			// if (method.getName().equals("setToNullMethod")) {

			codeIterator.begin();

			instrumentAfterLocVarObject(method, codeIterator, locVarTable,
					lineNumberMap, lineNrTable);

			// codeIterator.begin();
			// checkMethodCall(method, codeIterator, locVarTable, lineNrTablePc,
			// lineNrTableLine);
			// }

		}
	}

	private HashMap<Integer, Integer> getLineNumberTable(CtMethod method) {
		CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
		LineNumberAttribute lineNrTable = (LineNumberAttribute) codeAttribute
				.getAttribute(LineNumberAttribute.tag);

		HashMap<Integer, Integer> lineNumberMap = new HashMap<>();

		for (int j = 0; j < lineNrTable.tableLength(); j++) {
			lineNumberMap
					.put(lineNrTable.startPc(j), lineNrTable.lineNumber(j));
		}
		return lineNumberMap;
	}

	private void addTimeToModifiedProject(CtMethod method)
			throws CannotCompileException {
		if (method.getName().equals("main")) {
			CtField f = CtField.make("public static long startTime;", cc);
			cc.addField(f);
			method.insertBefore("startTime = System.nanoTime();");
			method.insertAfter("System.out.println(\"\\nOriginal class time: \" +((System.nanoTime() - startTime)/1000000) + \" ms\");");

			// CtClass etype = ClassPool.getDefault().get(
			// "java.io.IOException");
			// method.addCatch("{ System.out.println($e); throw $e; }",
			// etype);
		}
	}

	/**
	 * Searches only locVar which are objects and directly instrument test-code.
	 * 
	 * @param method
	 * @param codeIterator
	 * @param locVarTable
	 * @param lineNumberMap
	 * @throws BadBytecode
	 * @throws CannotCompileException
	 */
	private void instrumentAfterLocVarObject(CtMethod method,
			CodeIterator codeIterator, LocalVariableAttribute locVarTable,
			HashMap<Integer, Integer> lineNumberMap,
			LineNumberAttribute lineNumberTable) throws BadBytecode,
			CannotCompileException {

		// store current instruction and the previous instructions
		ArrayList<Integer> instrPositions = new ArrayList<Integer>();

		int instrCounter = 0;
		int prevInstrOp = 0;

		int methodMaxPc = lineNumberTable
				.startPc(lineNumberTable.tableLength() - 1);

		while (codeIterator.hasNext()) {
			int pos = codeIterator.next();
			instrPositions.add(pos);

			int op = codeIterator.byteAt(pos);

			if (instrCounter > 0)
				prevInstrOp = codeIterator.byteAt(instrPositions
						.get(instrCounter - 1));
			instrCounter++;

			if (isLocVarObject(op)
					&& (!Mnemonic.OPCODE[prevInstrOp].matches("goto.*") && pos <= methodMaxPc)) {

				int locVarIndexInLocVarTable = getLocVarIndexInLocVarTable(
						codeIterator, locVarTable, pos);

				// store locVar
				String locVarName = locVarTable
						.variableName(locVarIndexInLocVarTable);

				// int locVarSourceLineNr = getLocVarLineNrInSourceCode(
				// lineNrTablePc, lineNrTableLine, pos);
				int locVarSourceLineNr = getLineNumber(lineNumberMap, pos);

				insertTestLineForLocalVariableAssignment(method, locVarName,
						locVarSourceLineNr);

				// container.storeLocVarInfo(locVarName, locVarSourceLineNr,
				// method, locVarIndexInLocVarTable);
			}
		}
	}

	private int getLineNumber(HashMap<Integer, Integer> lineNumberMap, int pos) {
		int lineNumber = 0;

		Object[] keys = lineNumberMap.keySet().toArray();
		Arrays.sort(keys);

		for (int i = 0; i < keys.length; i++) {
			if (pos >= (int) keys[i]) {
				lineNumber = lineNumberMap.get((int) keys[i]);
			} else {
				break;
			}
		}

		return lineNumber;
	}

	private void insertTestLineForLocalVariableAssignment(CtMethod method,
			String locVarName, int locVarSourceLineNr)
			throws CannotCompileException {
		method.insertAt(locVarSourceLineNr + 1,
				"ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test( \""
						+ method.getDeclaringClass().getName() + "\",\""
						+ method.getName() + "\"," + locVarName + ","
						+ locVarSourceLineNr + ",\"" + locVarName
						+ "\", \"locVar\", \"locVar\");");
	}

	private void checkMethodCall(CtMethod method, CodeIterator codeIterator,
			LocalVariableAttribute locVarTable,
			ArrayList<Integer> lineNrTablePc, ArrayList<Integer> lineNrTableLine)
			throws BadBytecode, CannotCompileException {

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

			// check if it's NOT a primitive one and the iterator iterates only
			// through the "direct" byte code of the method
			String opString = Mnemonic.OPCODE[op].toString();
			int pos2;
			ArrayList<Integer> aloadsInInstructionsOfOneLine = new ArrayList<>();

			if (opString.matches("aload.*")
					&& (!Mnemonic.OPCODE[prevInstrOp].matches("goto.*") && pos <= lineNrTablePc
							.get(lineNrTablePc.size() - 1))) {
				aloadsInInstructionsOfOneLine.add(pos);
				int locVarIndexInLocVarTable = getLocVarIndexInLocVarTable(
						codeIterator, locVarTable, pos);

				// store locVar
				String locVarName = locVarTable
						.variableName(locVarIndexInLocVarTable);

				int locVarSourceLineNr = getLocVarLineNrInSourceCode(
						lineNrTablePc, lineNrTableLine, pos);

				int indexLineNrTable = lineNrTableLine
						.indexOf(locVarSourceLineNr);

				pos2 = codeIterator.next();
				op = codeIterator.byteAt(pos2);

				ArrayList<Object> methodCallInfoList = isMethodCallNotForStoringToVar(
						method, codeIterator, lineNrTablePc, op, pos2,
						aloadsInInstructionsOfOneLine, indexLineNrTable);

				if ((boolean) methodCallInfoList.get(0)) {

				}

				// method.insertAt(locVarSourceLineNr - 1,
				// "ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.isAlreadyNull( \""
				// + method.getDeclaringClass().getName()
				// + "\",\"" + method.getName() + "\","
				// + locVarName + "," + locVarSourceLineNr + ",\""
				// + locVarName + "\", \"locVar\");");

				// pos2 = codeIterator.next();
				// op = codeIterator.byteAt(pos2);
				// opString = Mnemonic.OPCODE[op];
				//
				// System.out.println("HEEELLLOO");
				//
				// if (Mnemonic.OPCODE[op].matches("athrow"))
				// System.out.println("ERROOOOOOR");
				//
				// while (opString.matches(".*load.*")
				// || opString.matches(".const.")) {
				// pos2 = codeIterator.next();
				// op = codeIterator.byteAt(pos2);
				// opString = Mnemonic.OPCODE[op];
				// }
				//
				// if (opString.matches("invoke.*")) {
				//
				// int locVarIndexInLocVarTable = getLocVarIndexInLocVarTable(
				// codeIterator, locVarTable, pos);
				//
				// // store locVar
				// String locVarName = locVarTable
				// .variableName(locVarIndexInLocVarTable);
				//
				// int locVarSourceLineNr = getLocVarLineNrInSourceCode(
				// lineNrTablePc, lineNrTableLine, pos);
				//
				// method.insertAt(locVarSourceLineNr - 1,
				// "ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.isAlreadyNull( \""
				// + method.getDeclaringClass().getName()
				// + "\",\"" + method.getName() + "\","
				// + locVarName + "," + locVarSourceLineNr
				// + ",\"" + locVarName + "\", \"locVar\");");
				//
				// System.out.println();
				// }
			}
		}
	}

	private ArrayList<Object> isMethodCallNotForStoringToVar(CtMethod method,
			CodeIterator codeIterator, ArrayList<Integer> lineNrTablePc,
			int op, int pos2, ArrayList<Integer> aloadsInInstructionsOfOneLine,
			int indexLineNrTable) throws BadBytecode {
		ArrayList<Object> resList = new ArrayList<>();
		int paramAmount = 0;
		int posOfObjectWithMehodCall;

		while (!Mnemonic.OPCODE[op].equals("areturn")
				&& pos2 < lineNrTablePc.get(indexLineNrTable + 1)) {
			boolean b;
			String check = Mnemonic.OPCODE[op];
			op = codeIterator.byteAt(pos2);
			if (Mnemonic.OPCODE[op].matches("aload.*")) {
				aloadsInInstructionsOfOneLine.add(pos2);
			}

			if (Mnemonic.OPCODE[op].matches("invoke.*")) {

				ConstPool pool = method.getMethodInfo2().getConstPool();

				String instr = javassist.bytecode.InstructionPrinter
						.instructionString(codeIterator, pos2, pool);
				System.out.println(instr);

				System.out.println("method ref class name: "
						+ pool.getMethodrefClassName(codeIterator
								.u16bitAt(pos2 + 1)));
				System.out
						.println("method ref name: "
								+ pool.getMethodrefName(codeIterator
										.u16bitAt(pos2 + 1)));

				String methodParametersAndReturn = pool
						.getMethodrefType(codeIterator.u16bitAt(pos2 + 1));

				System.out.println("method para and return type: "
						+ methodParametersAndReturn);

				String parameters = methodParametersAndReturn.substring(1,
						methodParametersAndReturn.indexOf(")"));

				for (int i = 0; i < parameters.length(); i++) {
					if (parameters.charAt(i) == ';') {
						paramAmount++;
					}
				}

				if (aloadsInInstructionsOfOneLine.size() > paramAmount) {
					posOfObjectWithMehodCall = aloadsInInstructionsOfOneLine
							.get(0);
					resList.add(posOfObjectWithMehodCall);
				} else {
					resList.add(null);
				}

			}

			if (Mnemonic.OPCODE[op].matches("astore.*")) {
				resList.add(0, false);
			} else {
				resList.add(0, true);
			}
			pos2 = codeIterator.next();

		}
		return resList;
	}

	/**
	 * Checks if the locVar is an object, NOT a primitive one.
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
	 * @param pos
	 * @return index of locVar in locVarTable
	 */
	private static int getLocVarIndexInLocVarTable(CodeIterator codeIterator,
			LocalVariableAttribute localVarTable, int pos) {
		int i = 0;
		boolean b = true;
		while (b) {

			String opString = Mnemonic.OPCODE[codeIterator.byteAt(pos)];
			if (opString.matches("astore.*")) {
				// int a = localVarTable.index(i);
				// int c = getLocVarArraySlotAtStoring(codeIterator, pos);
				if (localVarTable.index(i) == getLocVarArraySlotAtStoring(
						codeIterator, pos))
					b = false;
				else
					i++;

			}

			if (opString.matches("aload.*")) {
				int a = localVarTable.index(i);
				int c = getLocVarArraySlotAtStoring(codeIterator, pos);
				if (localVarTable.index(i) == getLocVarArraySlotAtLoading(
						codeIterator, pos))
					b = false;
				else
					i++;

			}

		}
		return i;
	}

	/**
	 * Gets the slot/index of locVar in locVarArray of frame.
	 * 
	 * @param codeIterator
	 * @param pos
	 * @return slot/index of locVar in locVarArray
	 */
	private static int getLocVarArraySlotAtStoring(CodeIterator codeIterator,
			int pos) {
		// check if locVar is stored in astore_0..._3 (one byte)
		// if not it calculates the slot in which it stored by getting the
		// number in the second byte (two bytes)

		int op = codeIterator.byteAt(pos);
		String opString = Mnemonic.OPCODE[op];

		if (!opString.matches("astore"))
			return Integer.parseInt(opString.substring(opString.length() - 1,
					opString.length()));
		else
			return codeIterator.u16bitAt(pos) - 14848;
	}

	/**
	 * Gets the slot/index of locVar in locVarArray of frame.
	 * 
	 * @param codeIterator
	 * @param pos
	 * @return slot/index of locVar in locVarArray
	 */
	private static int getLocVarArraySlotAtLoading(CodeIterator codeIterator,
			int pos) {
		// check if locVar is stored in astore_0..._3 (one byte)
		// if not it calculates the slot in which it stored by getting the
		// number in the second byte (two bytes)

		int op = codeIterator.byteAt(pos);
		String opString = Mnemonic.OPCODE[op];

		if (!opString.matches("aload"))
			return Integer.parseInt(opString.substring(opString.length() - 1,
					opString.length()));
		else
			return codeIterator.u16bitAt(pos) - 14848;
	}

	/**
	 * Gets the lineNr of the locVar in the Source Code.
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
			if (lineNrTablePc.size() == 1) {
				return lineNrTableLine.get(0);
			}

			if (pos < lineNrTablePc.get(k) && pos >= lineNrTablePc.get(j)) {
				res = lineNrTableLine.get(j);
				b = false;
			} else if (pos == lineNrTablePc.get(k)) {
				res = lineNrTableLine.get(k);
				b = false;
			} else {
				j++;
				k++;
			}
		}
		return res;
	}

}