package ch.unibe.scg.nullSpy.instrumentor.controller.methodInvocation;

import java.util.ArrayList;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.Opcode;

public class IntervalManager {

	private CtBehavior behavior;
	private CodeAttribute codeAttr;
	private CodeIterator codeIter;
	private LineNumberAttribute lineNrAttr;

	public IntervalManager(CtBehavior behavior) {
		this.behavior = behavior;
		this.codeAttr = this.behavior.getMethodInfo().getCodeAttribute();
		this.lineNrAttr = (LineNumberAttribute) codeAttr
				.getAttribute(LineNumberAttribute.tag);
		this.codeIter = codeAttr.iterator();
	}

	public ArrayList<Integer> getInvocationInterval(int[] multipleLineInterval,
			int pos) throws BadBytecode {

		ArrayList<Integer> invocationInterval = new ArrayList<>();
		int startPos;
		if (multipleLineInterval == null) {
			int lineNr = this.lineNrAttr.toLineNumber(pos);
			startPos = this.lineNrAttr.toStartPc(lineNr);

			// store bytecode interval until invocation
			return getCleanInvocationInterval(startPos);
		} else {
			startPos = multipleLineInterval[0];
			int endPos = multipleLineInterval[1];
			this.codeIter.move(startPos);
			int pos2 = pos;
			while (this.codeIter.hasNext() && pos2 <= endPos) {
				pos2 = this.codeIter.next();
				invocationInterval.add(pos2);
			}

			removeUnnecessaryOpcodesFromInvocationInterval(invocationInterval);
			return invocationInterval;
		}
	}

	/**
	 * Returns only the interval of the invocation without anything else after
	 * the invocation opcode
	 * 
	 * @param codeAttr
	 * @param startPos
	 * @return interval of invocation only, without anything else after it as
	 *         ArrayList
	 * @throws BadBytecode
	 */
	private ArrayList<Integer> getCleanInvocationInterval(int startPos)
			throws BadBytecode {
		ArrayList<Integer> invocationBytecodeInterval = new ArrayList<>();

		int startLineNr = this.lineNrAttr.toLineNumber(startPos);
		this.codeIter.move(startPos);
		int pos2 = this.codeIter.next();
		invocationBytecodeInterval.add(pos2);

		pos2 = this.codeIter.next();

		int lineNr2 = this.lineNrAttr.toLineNumber(pos2);
		int startPos2 = this.lineNrAttr.toStartPc(lineNr2);

		// adding all pc that has the same startPos -> get whole interval of
		// the "source line"
		while (startPos2 == startPos || lineNr2 == startLineNr) {
			invocationBytecodeInterval.add(pos2);
			if (!this.codeIter.hasNext())
				break;
			pos2 = this.codeIter.next();
			lineNr2 = this.lineNrAttr.toLineNumber(pos2);
			startPos2 = this.lineNrAttr.toStartPc(lineNr2);
		}

		if (invocationBytecodeInterval.size() != 0)
			removeUnnecessaryOpcodesFromInvocationInterval(invocationBytecodeInterval);

		return invocationBytecodeInterval;

	}

	private void removeUnnecessaryOpcodesFromInvocationInterval(
			ArrayList<Integer> invocationBytecodeInterval) {

		if (invocationBytecodeInterval.size() == 0)
			return;
		int endPos = getIntervalEndPos(invocationBytecodeInterval);
		int op = this.codeIter.byteAt(endPos);

		// remove the rest after invocation
		while (!isInvoke(op) && invocationBytecodeInterval.size() != 0) {
			int i = invocationBytecodeInterval.indexOf(endPos);
			invocationBytecodeInterval.remove(i);

			if (invocationBytecodeInterval.size() == 0)
				return;

			endPos = getIntervalEndPos(invocationBytecodeInterval);
			op = this.codeIter.byteAt(endPos);
		}
	}

	private Integer getIntervalEndPos(
			ArrayList<Integer> invocationBytecodeInterval) {
		return invocationBytecodeInterval
				.get(invocationBytecodeInterval.size() - 1);
	}

	private boolean isInvoke(int op) {
		return op == Opcode.INVOKEINTERFACE || op == Opcode.INVOKESPECIAL
				|| op == Opcode.INVOKEVIRTUAL || op == Opcode.INVOKESTATIC;
	}
}
