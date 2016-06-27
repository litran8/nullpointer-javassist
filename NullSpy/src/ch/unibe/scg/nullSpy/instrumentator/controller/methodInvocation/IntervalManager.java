package ch.unibe.scg.nullSpy.instrumentator.controller.methodInvocation;

import java.util.ArrayList;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.Opcode;

public class IntervalManager {

	private CtBehavior behavior;

	public IntervalManager(CtBehavior behavior) {
		this.behavior = behavior;
	}

	public ArrayList<Integer> getInvocationInterval(CodeAttribute codeAttr,
			LineNumberAttribute lineNrAttr, int pos,
			ArrayList<Integer> multipleLineInterval) throws BadBytecode {
		ArrayList<Integer> invocationInterval = new ArrayList<>();
		int startPos;
		if (multipleLineInterval.size() == 0) {
			int lineNr = lineNrAttr.toLineNumber(pos);
			startPos = lineNrAttr.toStartPc(lineNr);

			// store bytecode interval until invocation
			return getCleanInvocationInterval(codeAttr, startPos);
		} else {
			CodeIterator codeIter = codeAttr.iterator();
			startPos = multipleLineInterval.get(0);
			int endPos = multipleLineInterval.get(1);
			codeIter.move(startPos);
			int pos2 = pos;
			while (codeIter.hasNext() && pos2 <= endPos) {
				pos2 = codeIter.next();
				invocationInterval.add(pos2);
			}

			removeUnnecessaryOpcodesFromInvocationInterval(codeIter,
					invocationInterval);
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
	private ArrayList<Integer> getCleanInvocationInterval(
			CodeAttribute codeAttr, int startPos) throws BadBytecode {
		LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
				.getAttribute(LineNumberAttribute.tag);
		CodeIterator codeIter = codeAttr.iterator();
		ArrayList<Integer> invocationBytecodeInterval = new ArrayList<>();

		int startLineNr = lineNrAttr.toLineNumber(startPos);
		codeIter.move(startPos);
		int pos2 = codeIter.next();
		invocationBytecodeInterval.add(pos2);

		pos2 = codeIter.next();

		int lineNr2 = lineNrAttr.toLineNumber(pos2);
		int startPos2 = lineNrAttr.toStartPc(lineNr2);

		// adding all pc that has the same startPos -> get whole interval of
		// the "source line"
		while (startPos2 == startPos || lineNr2 == startLineNr) {
			invocationBytecodeInterval.add(pos2);
			if (!codeIter.hasNext())
				break;
			pos2 = codeIter.next();
			lineNr2 = lineNrAttr.toLineNumber(pos2);
			startPos2 = lineNrAttr.toStartPc(lineNr2);
		}

		if (invocationBytecodeInterval.size() != 0)
			removeUnnecessaryOpcodesFromInvocationInterval(codeIter,
					invocationBytecodeInterval);

		return invocationBytecodeInterval;

	}

	private void removeUnnecessaryOpcodesFromInvocationInterval(
			CodeIterator codeIter, ArrayList<Integer> invocationBytecodeInterval) {

		if (invocationBytecodeInterval.size() == 0)
			return;
		int endPos = getIntervalEndPos(invocationBytecodeInterval);
		int op = codeIter.byteAt(endPos);

		// remove the rest after invocation
		while (!isInvoke(op) && invocationBytecodeInterval.size() != 0) {
			int i = invocationBytecodeInterval.indexOf(endPos);
			invocationBytecodeInterval.remove(i);

			if (invocationBytecodeInterval.size() == 0)
				return;

			endPos = getIntervalEndPos(invocationBytecodeInterval);
			op = codeIter.byteAt(endPos);
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
