package ch.unibe.scg.nullSpy.instrumentator.controller.methodInvocation;

import java.util.ArrayList;
import java.util.Collections;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;

public class MultipleLineManager {

	private CtBehavior behavior;
	private LineNumberAttribute lineNrAttr;
	private CodeIterator codeIter;

	public MultipleLineManager(CtBehavior behavior) {
		this.behavior = behavior;
		CodeAttribute codeAttr = this.behavior.getMethodInfo()
				.getCodeAttribute();
		this.lineNrAttr = (LineNumberAttribute) codeAttr
				.getAttribute(LineNumberAttribute.tag);
		this.codeIter = codeAttr.iterator();
	}

	public int[] getMultipleLineInterval(int pos) throws BadBytecode {
		int lineAttrSize = lineNrAttr.tableLength();
		int lineNr = this.lineNrAttr.toLineNumber(pos);
		int lineNrAttrIndex = getLineNrIndex(lineNr);

		for (int i = lineNrAttrIndex + 1; i < lineAttrSize; i++) {
			int provLineNr = this.lineNrAttr.lineNumber(i);
			if (provLineNr <= lineNr) {

				int[] alternatingInterval = getAlternatingInterval(
						lineNrAttrIndex, i);

				if (alternatingInterval != null)
					return alternatingInterval;

				// 2: second index, e.g. 127...!127!
				int provLineNrIndex_2 = getSmallestWrapLineNrIndex(i);
				// 1: first index, e.g. !127!...127
				int provLineNrIndex_1 = getCorrespondingLineNrIndex(provLineNrIndex_2);

				return getNonAlternatingInterval(provLineNrIndex_1,
						provLineNrIndex_2, pos);
			}
		}

		for (int i = lineNrAttrIndex - 1; i >= 0; i--) {
			int provLineNr = this.lineNrAttr.lineNumber(i);
			if (provLineNr == lineNr) {
				int provLineNrIndex_1 = i;
				int provLineNrIndex_2 = lineNrAttrIndex;

				return getNonAlternatingInterval(provLineNrIndex_1,
						provLineNrIndex_2, pos);
			}
		}
		return null;
	}

	private int[] getAlternatingInterval(int lineNrAttrIndex, int index)
			throws BadBytecode {
		int lineNrAttrIndexLineNr = this.lineNrAttr.lineNumber(lineNrAttrIndex);
		int indexLineNr = this.lineNrAttr.lineNumber(index);

		if (indexLineNr == lineNrAttrIndexLineNr) {
			index = lineNrAttrIndex + 1;
			indexLineNr = this.lineNrAttr.lineNumber(index);
		}

		boolean isAlternating = isAlternating(lineNrAttrIndex,
				lineNrAttrIndexLineNr) && isAlternating(index, indexLineNr);

		if (isAlternating) {
			ArrayList<Integer> interval_1 = new ArrayList<>();

			interval_1.add(lineNrAttrIndex);
			interval_1.add(index);

			getPrevInterval(lineNrAttrIndex, lineNrAttrIndexLineNr, 2,
					interval_1);
			getPrevInterval(lineNrAttrIndex, lineNrAttrIndexLineNr, -2,
					interval_1);

			getPrevInterval(index, indexLineNr, 2, interval_1);
			getPrevInterval(index, indexLineNr, -2, interval_1);

			Collections.sort(interval_1);

			int startPc = this.lineNrAttr.startPc(interval_1.get(0));
			int endPc = getAlternatingEndPc(interval_1
					.get(interval_1.size() - 1));

			return new int[] { startPc, endPc };
		}
		return null;
	}

	private void getPrevInterval(int index, int lineNr, int i,
			ArrayList<Integer> interval) {
		if (isInLineNrAttrBound(index + i)
				&& this.lineNrAttr.lineNumber(index + i) == lineNr) {
			interval.add(index + i);
			getPrevInterval(index + i, lineNr, i, interval);
		}
	}

	private boolean isAlternating(int index, int lineNr) {
		return (isInLineNrAttrBound(index + 2) && this.lineNrAttr
				.lineNumber(index + 2) == lineNr)
				|| (isInLineNrAttrBound(index - 2) && this.lineNrAttr
						.lineNumber(index - 2) == lineNr);
	}

	private boolean isInLineNrAttrBound(int i) {
		return i >= 0 && i < this.lineNrAttr.tableLength();
	}

	private int[] getNonAlternatingInterval(int provLineNrIndex_1,
			int provLineNrIndex_2, int pos) throws BadBytecode {
		int maxLineNrIndex = getMaxLineNrIndex(provLineNrIndex_2);
		int nextLineNrStartPc = getNextLineNumberStartPcAfterMaxLineNr(maxLineNrIndex);
		int endPc = 0;

		while (this.codeIter.hasNext() && pos < nextLineNrStartPc) {
			endPc = pos;
			pos = this.codeIter.next(); // excluding nextLineNrStartPc
		}

		int startLineNr = this.lineNrAttr.lineNumber(provLineNrIndex_1);
		int startPc = this.lineNrAttr.toStartPc(startLineNr);

		return new int[] { startPc, endPc };
	}

	private int getAlternatingEndPc(int biggestIndex) throws BadBytecode {
		int biggestIndexLineNr = this.lineNrAttr.lineNumber(biggestIndex);
		int pos = this.lineNrAttr.toStartPc(biggestIndexLineNr);
		this.codeIter.move(pos);
		int nextLineNrStartPc = 0;
		int nextLineNrIndex = biggestIndex + 1;

		if (nextLineNrIndex < this.lineNrAttr.tableLength()) {
			int nextLineNr = this.lineNrAttr.lineNumber(nextLineNrIndex);
			nextLineNrStartPc = this.lineNrAttr.toStartPc(nextLineNr);

			int provPos = pos;
			while (this.codeIter.hasNext() && provPos < nextLineNrStartPc) {
				pos = provPos;
				provPos = this.codeIter.next();
			}

		} else {
			int provPos = pos;
			while (this.codeIter.hasNext()) {
				pos = provPos;
				provPos = this.codeIter.next();
			}
		}
		return pos;
	}

	private int getCorrespondingLineNrIndex(int provLineNrIndex) {
		int provLineNr = this.lineNrAttr.lineNumber(provLineNrIndex);
		for (int i = provLineNrIndex; i >= 0; i--) {
			int checkLineNr = this.lineNrAttr.lineNumber(i);
			if (checkLineNr == provLineNr)
				return i;
		}
		return 0;
	}

	private int getSmallestWrapLineNrIndex(int index) {
		int lineNr = this.lineNrAttr.lineNumber(index);
		if (index + 1 < this.lineNrAttr.tableLength()) {
			int checkLineNr = this.lineNrAttr.lineNumber(index + 1);
			if (checkLineNr < lineNr)
				index = getSmallestWrapLineNrIndex(index + 1);
		}
		return index;
	}

	private int getLineNrIndex(int lineNr) {
		for (int i = 0; i < this.lineNrAttr.tableLength(); i++) {
			if (this.lineNrAttr.lineNumber(i) == lineNr)
				return i;
		}
		return 0;
	}

	private int getMaxLineNrIndex(int possibleStartLineNrIndex) {
		int startLineNr = this.lineNrAttr.lineNumber(possibleStartLineNrIndex);
		int maxLineNrIndex = 0;
		int maxLineNr = startLineNr;
		for (int i = possibleStartLineNrIndex - 1; i >= 0; i--) {
			int provLineNr = this.lineNrAttr.lineNumber(i);
			if (provLineNr > maxLineNr) {
				maxLineNrIndex = i;
				maxLineNr = this.lineNrAttr.lineNumber(maxLineNrIndex);
			} else if (provLineNr == startLineNr)
				break;
		}
		return maxLineNrIndex;
	}

	private int getNextLineNumberStartPcAfterMaxLineNr(int maxLineNrIndex) {
		int maxLineNr = lineNrAttr.lineNumber(maxLineNrIndex);

		for (int i = maxLineNrIndex + 1; i < this.lineNrAttr.tableLength(); i++) {
			int provLineNr = this.lineNrAttr.lineNumber(i);
			if (provLineNr > maxLineNr)
				return this.lineNrAttr.startPc(i);
		}
		return 0;
	}

}
