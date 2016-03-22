package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
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
		// null if field is instantiated outside method
		if (method != null) {
			byteCodeAdapter.insertTestLineAfterVariableAssignment(method,
					variableName, variableLineNumber, variableType, variableID);
		} else {
			for (CtConstructor constructor : cc.getConstructors()) {
				byteCodeAdapter
						.insertTestLineAfterFieldInstantiatedOutSideMethod(
								constructor, variableName, variableLineNumber,
								variableType, variableID);
			}
		}
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
		int targetListIndex = 0;

		for (int i = 0; i < keys.length; i++) {
			if (pos >= (int) keys[i]) {
				targetListIndex = i;
				lineNumber = lineNumberMap.get((int) keys[i]);
			} else {
				break;
			}
		}

		// if for storing a variable needs more than 1 line, it "calculates" the
		// last line which storing needs
		for (int i = targetListIndex - 1; i >= 0; i--) {
			// if (lineNumberMap.get((int) keys[i]) == lineNumber) {
			// lineNumber = lineNumberMap.get((int) keys[targetListIndex -
			// 1]);
			// }
			if (lineNumberMap.get((int) keys[i]) > lineNumber) {
				lineNumber = lineNumberMap.get((int) keys[targetListIndex - 1]);
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

	protected ArrayList<LocalVariableTableEntry> getStableLocalVariableTableAsList(
			LocalVariableAttribute locVarTable) {

		ArrayList<LocalVariableTableEntry> localVariableList = new ArrayList<>();

		for (int i = 0; i < locVarTable.tableLength(); i++) {
			if (isNotPrimitive(locVarTable.descriptor(i))) {
				int startPc = locVarTable.startPc(i);
				int length = locVarTable.codeLength(i);
				int index = locVarTable.index(i);
				String varName = locVarTable.variableName(i);
				localVariableList.add(new LocalVariableTableEntry(startPc,
						length, index, varName));
			}
		}

		return localVariableList;
	}

	private boolean isNotPrimitive(String variableDescriptor) {
		return !(variableDescriptor.equals("I")
				|| variableDescriptor.equals("B")
				|| variableDescriptor.equals("J")
				|| variableDescriptor.equals("D")
				|| variableDescriptor.equals("F")
				|| variableDescriptor.equals("C")
				|| variableDescriptor.equals("S") || variableDescriptor
					.equals("Z"));
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
			ArrayList<LocalVariableTableEntry> localVarTable, int pos,
			String checkFor) {
		int res = 0;
		String opString = Mnemonic.OPCODE[codeIterator.byteAt(pos)];

		for (int j = 0; j < localVarTable.size(); j++) {
			if (opString.matches(checkFor)) {
				int i = getLocVarArraySlotAtStoring(codeIterator, pos);
				LocalVariableTableEntry entry = localVarTable.get(j);
				int k = entry.index;
				int slot = getLocVarArraySlotAtStoring(codeIterator, pos);
				if (entry.getIndex() == getLocVarArraySlotAtStoring(
						codeIterator, pos)) {
					String varName = entry.varName;
					int start = entry.startPc;
					int length = entry.length;
					int end = start + length;
					if (end - pos > 0) {
						res = j;
						break;
					}
				}
			}
		}

		return res;
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
		// String astoreInstruction =
		// javassist.bytecode.InstructionPrinter.instructionString(codeIterator,
		// pos, c)

		if (!opString.matches("astore"))
			return Integer.parseInt(opString.substring(opString.length() - 1,
					opString.length()));
		else {
			return codeIterator.u16bitAt(pos) - 14848;
		}
	}

	protected class LocalVariableTableEntry {
		int startPc;
		int length;
		int index;
		String varName;

		public LocalVariableTableEntry(int startPc, int length, int index,
				String varName) {
			this.startPc = startPc;
			this.length = length;
			this.index = index;
			this.varName = varName;
		}

		public int getStartPc() {
			return startPc;
		}

		public int getLength() {
			return length;
		}

		public int getIndex() {
			return index;
		}

		public String getVarName() {
			return varName;
		}

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