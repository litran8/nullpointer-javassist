package controller;

import java.util.ArrayList;

import model.MyClass;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;

public class LocVarLogic {

	private CtClass cc;
	private MyClass myClass;

	// private Printer printer;

	public LocVarLogic(CtClass cc, MyClass myClass) {
		this.cc = cc;
		this.myClass = myClass;
		// this.printer = new Printer();
	}

	public void searchAndStoreLocVar() throws BadBytecode {
		for (CtMethod method : this.cc.getDeclaredMethods()) {

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

			checkAndStoreLocVar(method, codeIterator, locVarTable,
					lineNrTablePc, lineNrTableLine);

			// print(method);
		}
	}

	/**
	 * Search all locVar; store <(name, lineNr, method), varIndexInLocVarTables>
	 * 
	 * @param method
	 * @param codeIterator
	 * @param localVarTable
	 * @param lineNrTablePc
	 * @param lineNrTableLine
	 * @throws BadBytecode
	 */
	private void checkAndStoreLocVar(CtMethod method,
			CodeIterator codeIterator, LocalVariableAttribute localVarTable,
			ArrayList<Integer> lineNrTablePc, ArrayList<Integer> lineNrTableLine)
			throws BadBytecode {

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
				String varName = localVarTable
						.variableName(locVarIndexInLocVarTable);

				int varSourceLineNr = getLocVarLineNrInSourceCode(
						lineNrTablePc, lineNrTableLine, pos);

				this.myClass.storeLocVar(varName, varSourceLineNr, method,
						locVarIndexInLocVarTable);

				// locVar methodCalls
				// int invokePos = getInvokePos(prevInstrOp, codeIterator,
				// instrPositions, instrCounter);
				//
				// if (isLocVarCallingMethod(prevInstrOp, codeIterator,
				// instrPositions, instrCounter)) {
				// System.out.println("\n--- INVOKE OF LOCVAR STARTS ---");
				// System.out
				// .println("VarName: " + varName + "\tVarLineNr: "
				// + varSourceLineNr + "\tClass: "
				// + this.cc.getName());
				// printer.printInstrAtPos(method, codeIterator, invokePos);
				// System.out.println("--- INVOKE OF LOCVAR ENDS ---\n");
				// isLocVarMethodCallOfSameClass(method, codeIterator,
				// invokePos);
				// }
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

	/**
	 * Checks if the value of the locVar is set by a methodCall
	 * 
	 * @param prevInstrOp
	 * @param codeIter
	 * @param instrPositions
	 * @param instrCounter
	 * @return
	 */
	// private boolean isLocVarCallingMethod(int prevInstrOp,
	// CodeIterator codeIter, ArrayList<Integer> instrPositions,
	// int instrCounter) {
	// return Mnemonic.OPCODE[prevInstrOp].matches("invoke.*")
	// || (Mnemonic.OPCODE[prevInstrOp].matches(".*cast.*") &&
	// Mnemonic.OPCODE[codeIter
	// .byteAt(instrPositions.get(instrCounter - 3))]
	// .matches("invoke.*"));
	// }

	/**
	 * Checks if the called method is from the same class as the locVar. If not,
	 * go through the methodRefClass and check for fields and locVars.
	 * 
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
	// private void isLocVarMethodCallOfSameClass(CtMethod method,
	// CodeIterator codeIterator, int instrCounter)
	// throws NotFoundException, CannotCompileException, BadBytecode,
	// IllegalAccessException, InvocationTargetException,
	// NoSuchMethodException {
	// String methodRefClassName = method.getMethodInfo2().getConstPool()
	// .getMethodrefClassName(codeIterator.u16bitAt(instrCounter + 1));
	// String currentClassName = this.cc.getName();
	//
	// if (!(methodRefClassName.equals(currentClassName))) {
	// Controller.Iteration.goThrough(ClassPool.getDefault().get(
	// methodRefClassName));
	// }
	// }

	/**
	 * // * Gets the pos of the invokeInstr // * // * @param prevInstrOp // * @param
	 * instrPositions // * @param instrCounter // * @param codeIter // * @return
	 * pos of invokeInstr of locVar //
	 */
	// private int getInvokePos(int prevInstrOp, CodeIterator codeIter,
	// ArrayList<Integer> instrPositions, int instrCounter) {
	//
	// if (Mnemonic.OPCODE[prevInstrOp].matches("invoke.*"))
	// return instrPositions.get(instrCounter - 2);
	// else if ((Mnemonic.OPCODE[prevInstrOp].matches(".*cast.*") &&
	// Mnemonic.OPCODE[codeIter
	// .byteAt(instrPositions.get(instrCounter - 3))]
	// .matches("invoke.*"))) {
	// return instrPositions.get(instrCounter - 3);
	// } else
	// return 0;
	// }
}