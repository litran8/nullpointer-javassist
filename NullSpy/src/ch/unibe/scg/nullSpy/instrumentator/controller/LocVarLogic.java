package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;

public class LocVarLogic {

	private CtClass cc;

	public LocVarLogic(CtClass cc) {
		this.cc = cc;
	}

	/**
	 * Checks all locVar in a class and instrument test-code after their
	 * assignments.
	 * 
	 * @throws BadBytecode
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 */
	public void instrumentAfterLocVarAssignment() throws BadBytecode,
			CannotCompileException, NotFoundException {

		for (CtMethod method : cc.getDeclaredMethods()) {

			if (method.getName().equals("main")) {
				CtField f = CtField.make("public static long startTime;", cc);
				cc.addField(f);
				method.insertBefore("startTime = System.nanoTime();");
				method.insertAfter("System.out.println(\"\\nOriginal class time: \" +((System.nanoTime() - startTime)/1000000) + \" ms\");");
			}

			// get everything what is needed for checking locVars in the
			// byte code
			CodeAttribute codeAttribute = method.getMethodInfo()
					.getCodeAttribute();
			CodeIterator codeIterator = codeAttribute.iterator();
			codeIterator.begin();

			LocalVariableAttribute locVarTable = (LocalVariableAttribute) codeAttribute
					.getAttribute(javassist.bytecode.LocalVariableAttribute.tag);

			LineNumberAttribute lineNrTable = (LineNumberAttribute) codeAttribute
					.getAttribute(LineNumberAttribute.tag);

			// store lineNrTable into ArrayLists (because directly get lineNr
			// changes the lineNrTable somehow...
			ArrayList<Integer> lineNrTablePc = new ArrayList<Integer>();
			ArrayList<Integer> lineNrTableLine = new ArrayList<Integer>();

			for (int j = 0; j < lineNrTable.tableLength(); j++) {
				lineNrTablePc.add(lineNrTable.startPc(j));
				lineNrTableLine.add(lineNrTable.lineNumber(j));
			}

			codeIterator.begin();

			instrumentAfterLocVarObject(method, codeIterator, locVarTable,
					lineNrTablePc, lineNrTableLine);
		}
	}

	/**
	 * Searches all locVar and directly instrument tester after assignment.
	 * 
	 * @param method
	 * @param codeIterator
	 * @param localVarTable
	 * @param lineNrTablePc
	 * @param lineNrTableLine
	 * @throws BadBytecode
	 * @throws CannotCompileException
	 */
	private void instrumentAfterLocVarObject(CtMethod method,
			CodeIterator codeIterator, LocalVariableAttribute localVarTable,
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

			// check if it's NOT a primitive one
			if (isLocVarObject(op)
					&& (!Mnemonic.OPCODE[prevInstrOp].matches("goto.*") && pos <= lineNrTablePc
							.get(lineNrTablePc.size() - 1))) {

				int locVarIndexInLocVarTable = getLocVarIndexInLocVarTable(
						codeIterator, localVarTable, pos);

				// store locVar
				String locVarName = localVarTable
						.variableName(locVarIndexInLocVarTable);

				int locVarSourceLineNr = getLocVarLineNrInSourceCode(
						lineNrTablePc, lineNrTableLine, pos);

				// insertAt( int lineNr + 1, test(String className, Object
				// varValue, int lineNr, String varName) );
				method.insertAt(locVarSourceLineNr + 1,
						"ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test( \""
								+ method.getDeclaringClass().getName() + "\","
								+ locVarName + "," + locVarSourceLineNr + ",\""
								+ locVarName + "\");");
			}
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
	 * @param pos
	 * @return index of locVar in locVarTable
	 */
	private static int getLocVarIndexInLocVarTable(CodeIterator codeIterator,
			LocalVariableAttribute localVarTable, int pos) {
		int i = 0;
		boolean b = true;
		while (b) {
			if (localVarTable.index(i) == getLocVarArraySlot(codeIterator, pos))
				b = false;
			else
				i++;
		}
		return i;
	}

	/**
	 * Gets the slot/index of locVar in locVarArray of frame
	 * 
	 * @param codeIterator
	 * @param pos
	 * @return slot/index of locVar in locVarArray
	 */
	private static int getLocVarArraySlot(CodeIterator codeIterator, int pos) {
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

}