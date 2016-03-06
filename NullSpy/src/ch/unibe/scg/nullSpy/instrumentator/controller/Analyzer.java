package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.Arrays;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;

public abstract class Analyzer {
	protected CtClass cc;
	protected ByteCodeAdapter byteCodeAdapter;

	public Analyzer(CtClass cc) {
		this.cc = cc;
		this.byteCodeAdapter = new ByteCodeAdapter();
	}

	protected void adaptByteCode(CtMethod method, String variableName,
			int variableLineNumber, String variableType, String variableID)
			throws CannotCompileException {
		byteCodeAdapter.insertTestLineAfterVariableAssignment(method,
				variableName, variableLineNumber, variableType, variableID);
	}

	protected HashMap<Integer, Integer> getLineNumberTable(CtMethod method) {
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

	protected int getLineNumber(HashMap<Integer, Integer> lineNumberMap, int pos) {
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

	protected int getPc(HashMap<Integer, Integer> lineNumberMap, int lineNumber) {

		for (int key : lineNumberMap.keySet()) {
			if (lineNumberMap.get(key) == lineNumber) {
				return key;
			}
		}
		return 0;
	}

	/**
	 * Gets the index of locVar in the locVarTable (Byte code)
	 * 
	 * @param codeIterator
	 * @param localVarTable
	 * @param pos
	 * @return index of locVar in locVarTable
	 */
	protected int getLocVarIndexInLocVarTable(CodeIterator codeIterator,
			LocalVariableAttribute localVarTable, int pos, String checkFor) {
		int i = 0;
		boolean b = true;
		while (b) {

			String opString = Mnemonic.OPCODE[codeIterator.byteAt(pos)];
			if (opString.matches(checkFor)) {
				// int a = localVarTable.index(i);
				// int c = getLocVarArraySlotAtStoring(codeIterator, pos);
				if (localVarTable.index(i) == getLocVarArraySlotAtStoring(
						codeIterator, pos))
					b = false;
				else
					i++;

			}

			// if (opString.matches("aload.*")) {
			// int a = localVarTable.index(i);
			// int c = getLocVarArraySlotAtLoading(codeIterator, pos);
			// if (localVarTable.index(i) == getLocVarArraySlotAtLoading(
			// codeIterator, pos))
			// b = false;
			// else
			// i++;
			//
			// }

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
	// private static int getLocVarArraySlotAtLoading(CodeIterator codeIterator,
	// int pos) {
	// // check if locVar is stored in astore_0..._3 (one byte)
	// // if not it calculates the slot in which it stored by getting the
	// // number in the second byte (two bytes)
	//
	// int op = codeIterator.byteAt(pos);
	// String opString = Mnemonic.OPCODE[op];
	//
	// if (!opString.matches("aload"))
	// return Integer.parseInt(opString.substring(opString.length() - 1,
	// opString.length()));
	// else
	// return codeIterator.u16bitAt(pos) - 14848;
	// }
}