package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.analysis.ControlFlow.Block;
import ch.unibe.scg.nullSpy.model.Variable;

public abstract class Analyzer {
	protected CtClass cc;
	protected ByteCodeAdapter byteCodeAdapter;

	public Analyzer(CtClass cc) {
		this.cc = cc;
		this.byteCodeAdapter = new ByteCodeAdapter();
	}

	// field
	// adaptByteCode( _, _ , belongedClassNameOfField, _, _, "field", isStatic,
	// _);
	// }

	// localVar
	// adaptByteCode( _, _, null, _, _, "localVariable_" + localVarSlot, false,
	// _);

	// adaptByteCode(Variable var, )

	protected void adaptByteCode(Variable var, String variableID)
			throws CannotCompileException, NotFoundException, BadBytecode {
		CtBehavior method = var.getBehavior();
		// null if field is instantiated outside method
		if (method != null) {
			byteCodeAdapter.insertTestLineAfterVariableAssignment(var,
					variableID);
		} else {
			for (CtConstructor constructor : cc.getConstructors()) {
				byteCodeAdapter
						.insertTestLineAfterFieldInstantiatedOutSideMethod(
								constructor, var, variableID);
			}
		}
	}

	// protected void adaptByteCode(CtBehavior method, String variableName,
	// String belongedClassNameOfVariable, int variableLineNumber,
	// String variableType, String variableID, boolean isStatic,
	// int posAfterAsignment) throws CannotCompileException,
	// NotFoundException, BadBytecode {
	// // null if field is instantiated outside method
	// if (method != null) {
	// byteCodeAdapter.insertTestLineAfterVariableAssignment(method,
	// variableName, belongedClassNameOfVariable,
	// variableLineNumber, variableType, variableID, isStatic,
	// posAfterAsignment);
	// } else {
	// for (CtConstructor constructor : cc.getConstructors()) {
	// byteCodeAdapter
	// .insertTestLineAfterFieldInstantiatedOutSideMethod(
	// constructor, variableName, variableLineNumber,
	// variableType, variableID);
	// }
	// }
	// }

	protected HashMap<Integer, Integer> getLineNumberTable(CtBehavior method) {
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
				String varType = locVarTable.descriptor(i);
				String varName = locVarTable.variableName(i);
				localVariableList.add(new LocalVariableTableEntry(startPc,
						length, index, varName, varType));
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
				int i = getLocVarArraySlot(codeIterator, pos);
				LocalVariableTableEntry entry = localVarTable.get(j);
				int k = entry.index;
				int slot = getLocVarArraySlot(codeIterator, pos);
				if (entry.getIndex() == getLocVarArraySlot(codeIterator, pos)) {
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
	protected static int getLocVarArraySlot(CodeIterator codeIterator, int pos) {
		// check if locVar is stored in astore_0..._3 (one byte)
		// if not it calculates the slot in which it stored by getting the
		// number in the second byte (two bytes)

		int op = codeIterator.byteAt(pos);
		String opString = Mnemonic.OPCODE[op];
		// String astoreInstruction =
		// javassist.bytecode.InstructionPrinter.instructionString(codeIterator,
		// pos, c)

		if (!(opString.matches("astore") || opString.matches("aload")))
			return Integer.parseInt(opString.substring(opString.length() - 1,
					opString.length()));
		else if (opString.matches("aload.*")) {
			int i = codeIterator.u16bitAt(pos);
			return codeIterator.u16bitAt(pos) - 6400;
		} else {
			int i = codeIterator.u16bitAt(pos);
			return codeIterator.u16bitAt(pos) - 14848;
		}
	}

	protected class LocalVariableTableEntry {
		int startPc;
		int length;
		int index;
		String varName;
		String varType;

		public LocalVariableTableEntry(int startPc, int length, int index,
				String varName, String varType) {
			this.startPc = startPc;
			this.length = length;
			this.index = index;
			this.varName = varName;
			this.varType = varType;
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

	protected Block getAimBlock(int pos, Block[] blocks) {
		for (Block b : blocks) {
			if (pos >= b.position() && pos <= (b.position() + b.length())) {
				return b;
			}
		}
		return null;
	}

	protected int getPosAfterAssignment(int pos, CodeIterator codeIterator,
			String opCheck) throws BadBytecode {
		codeIterator.move(pos);
		while (codeIterator.hasNext()) {
			int pc = codeIterator.next();
			int op = codeIterator.byteAt(pc);
			if (Mnemonic.OPCODE[op].matches(opCheck)) {
				return codeIterator.next();
			}
		}
		return 0;
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