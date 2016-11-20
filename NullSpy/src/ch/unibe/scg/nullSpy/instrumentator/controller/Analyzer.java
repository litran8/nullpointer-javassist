package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.analysis.ControlFlow.Block;
import ch.unibe.scg.nullSpy.instrumentator.model.PcLine;
import ch.unibe.scg.nullSpy.instrumentator.model.Variable;

public abstract class Analyzer {
	protected CtClass cc;
	protected ByteCodeAdapter byteCodeAdapter;

	public Analyzer(CtClass cc) {
		this.cc = cc;
		this.byteCodeAdapter = new ByteCodeAdapter();
	}

	protected void adaptByteCode(Variable var) throws CannotCompileException,
			NotFoundException, BadBytecode {
		byteCodeAdapter.insertTestLineAfterVariableAssignment(var);
	}

	protected HashMap<Integer, Integer> getLineNumberMap(CtBehavior behavior) {
		LineNumberAttribute lineNrTable = getLineNumberAttribute(behavior);

		HashMap<Integer, Integer> lineNumberMap = new HashMap<>();

		for (int j = 0; j < lineNrTable.tableLength(); j++) {
			lineNumberMap
					.put(lineNrTable.startPc(j), lineNrTable.lineNumber(j));
		}
		return lineNumberMap;
	}

	protected ArrayList<PcLine> getSortedLineNrMapAsList(
			HashMap<Integer, Integer> lineNumberMap) {
		Object[] keys = lineNumberMap.keySet().toArray();
		Arrays.sort(keys);

		ArrayList<PcLine> res = new ArrayList<>();

		for (int i = 0; i < keys.length; i++) {
			res.add(new PcLine((int) keys[i], lineNumberMap.get(keys[i])));
		}
		return res;
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

	protected ArrayList<LocalVarAttrEntry> getLocalVarAttrAsList(
			LocalVariableAttribute locVarTable) {

		ArrayList<LocalVarAttrEntry> localVariableList = new ArrayList<>();

		for (int i = 0; i < locVarTable.tableLength(); i++) {
			if (isNotPrimitive(locVarTable.descriptor(i))) {
				int startPc = locVarTable.startPc(i);
				int length = locVarTable.codeLength(i);
				int index = locVarTable.index(i);
				String varType = locVarTable.descriptor(i);
				String varName = locVarTable.variableName(i);
				localVariableList.add(new LocalVarAttrEntry(startPc, length,
						index, varName, varType));
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
	 * @return index of locVar in locVarTable
	 */
	protected int getLocalVarAttrIndex(CodeIterator codeIterator,
			ArrayList<LocalVarAttrEntry> localVarAttrAsList, int pos,
			String checkFor) {
		int res = 0;
		String opString = Mnemonic.OPCODE[codeIterator.byteAt(pos)];

		for (int j = 0; j < localVarAttrAsList.size(); j++) {
			if (opString.matches(checkFor)) {
				// int i = getLocVarArraySlot(codeIterator, pos);
				LocalVarAttrEntry entry = localVarAttrAsList.get(j);
				// int k = entry.index;
				// int slot = getLocVarArraySlot(codeIterator, pos);
				if (entry.getIndex() == getLocVarArraySlot(codeIterator, pos)) {
					// String varName = entry.varName;
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
	 * @return slot/index of locVar in locVarArray
	 */
	protected static int getLocVarArraySlot(CodeIterator codeIterator, int pos) {
		// check if locVar is stored in astore_0..._3 (one byte)
		// if not it calculates the slot in which it stored by getting the
		// number in the second byte (two bytes)

		int op = codeIterator.byteAt(pos);
		String opString = Mnemonic.OPCODE[op];

		if (!(opString.matches("astore") || opString.matches("aload")))
			// astore_X || aload_X
			return Integer.parseInt(opString.substring(opString.length() - 1,
					opString.length()));
		else if (opString.matches("aload.*")) {
			// aload X
			// int i = codeIterator.u16bitAt(pos);
			return codeIterator.u16bitAt(pos) - 6400;
		} else {
			// astore X
			// int i = codeIterator.u16bitAt(pos);
			return codeIterator.u16bitAt(pos) - 14848;
		}
	}

	protected class LocalVarAttrEntry {
		int startPc;
		int length;
		int index;
		String varName;
		String varType;

		public LocalVarAttrEntry(int startPc, int length, int index,
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

		public String toString() {
			String s = "VarName: " + varName + " Index: " + index
					+ "\nStartPc: " + startPc + ", EndPc" + (startPc + length);
			return s;
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

	protected CtBehavior[] getDeclaredBehaviors() {
		return this.cc.getDeclaredBehaviors();
	}

	protected CodeAttribute getCodeAttribute(CtBehavior behavior) {
		return behavior.getMethodInfo().getCodeAttribute();
	}

	protected LineNumberAttribute getLineNumberAttribute(CtBehavior behavior) {
		return (LineNumberAttribute) getCodeAttribute(behavior).getAttribute(
				LineNumberAttribute.tag);
	}

	protected LineNumberAttribute getLineNumberAttribute(
			CodeAttribute codeAttribute) {
		return (LineNumberAttribute) codeAttribute
				.getAttribute(LineNumberAttribute.tag);
	}

	protected LocalVariableAttribute getLocalVariableAttribute(
			CtBehavior behavior) {
		return (LocalVariableAttribute) getCodeAttribute(behavior)
				.getAttribute(LocalVariableAttribute.tag);
	}

	protected CodeIterator getCodeIterator(CtBehavior behavior) {
		return getCodeAttribute(behavior).iterator();
	}

	protected ConstPool getConstPool(CtBehavior behavior) {
		return behavior.getMethodInfo2().getConstPool();
	}

	protected boolean isSameBehavior(CtBehavior behavior, Variable lastVar) {
		boolean inSameBehavior = false;
		CtBehavior lastBehavior = lastVar.getBehavior();
		if (lastBehavior != null) {
			inSameBehavior = behavior.getName().equals(lastBehavior.getName())
					&& behavior.getDeclaringClass().getName()
							.equals(lastVar.getClassWhereVarIsUsed().getName())
					&& behavior.getSignature().equals(
							lastBehavior.getSignature());
		}
		return inSameBehavior;
	}

}