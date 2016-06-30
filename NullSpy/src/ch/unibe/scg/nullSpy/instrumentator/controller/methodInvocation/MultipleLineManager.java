package ch.unibe.scg.nullSpy.instrumentator.controller.methodInvocation;

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
				// 2: second index, e.g. 127...!127!
				int provLineNrIndex_2 = getSmallestWrapLineNrIndex(i);
				// 1: first index, e.g. !127!...127
				int provLineNrIndex_1 = getCorrespondingLineNrIndex(provLineNrIndex_2);

				return getInterval(provLineNrIndex_2, provLineNrIndex_1, pos);
			}
		}

		for (int i = lineNrAttrIndex - 1; i >= 0; i--) {
			int provLineNr = this.lineNrAttr.lineNumber(i);
			if (provLineNr == lineNr) {
				int provLineNrIndex_1 = i;
				int provLineNrIndex_2 = lineNrAttrIndex;

				return getInterval(provLineNrIndex_2, provLineNrIndex_1, pos);

			}
		}
		return null;
	}

	private int[] getInterval(int provLineNrIndex_2, int provLineNrIndex_1,
			int pos) throws BadBytecode {
		boolean isAlternating = isAlternating(provLineNrIndex_2);
		if (!isAlternating) { // 127...127
			return getNonAlternatingMultipleInterval(provLineNrIndex_1,
					provLineNrIndex_2, pos);
		} else { // 127,128,127,128
			return getAlternatingMultipleLineInterval(provLineNrIndex_2);
		}
	}

	private int[] getNonAlternatingMultipleInterval(int provLineNrIndex_1,
			int provLineNrIndex_2, int pos) throws BadBytecode {
		int maxLineNrIndex = getMaxLineNrIndex(provLineNrIndex_2);
		int nextLineNrStartPc = getNextBiggerLineNrPcThanMaxLineNrPc(maxLineNrIndex);
		int endPc = 0;

		while (this.codeIter.hasNext() && pos < nextLineNrStartPc) {
			endPc = pos;
			pos = this.codeIter.next();
		}

		int startLineNr = this.lineNrAttr.lineNumber(provLineNrIndex_1);
		int startPc = this.lineNrAttr.toStartPc(startLineNr);

		return new int[] { startPc, endPc };
	}

	private int[] getAlternatingMultipleLineInterval(int provLineNrIndex_2)
			throws BadBytecode {
		int provLineNr = this.lineNrAttr.lineNumber(provLineNrIndex_2);
		int checkLineNr_1 = this.lineNrAttr.lineNumber(provLineNrIndex_2 - 1);
		int smallerLineNrIndex = 0;
		int biggestIndex = provLineNrIndex_2;

		if (checkLineNr_1 > provLineNr
				&& (provLineNrIndex_2 + 1) < this.lineNrAttr.tableLength()) { // 127(1),128(2),127(1),!128!
			smallerLineNrIndex = provLineNrIndex_2;
			biggestIndex = provLineNrIndex_2 + 1;
		} else if (checkLineNr_1 < provLineNr && (provLineNrIndex_2 - 3) >= 0) { // !127!,128(1),127(2),128(1)
			smallerLineNrIndex = provLineNrIndex_2 - 3;
		}

		int alternatingEndPc = getAlternatingEndPc(biggestIndex);
		int smallerLineNr = this.lineNrAttr.lineNumber(smallerLineNrIndex);
		int alternatingStartPc = this.lineNrAttr.toStartPc(smallerLineNr);

		return new int[] { alternatingStartPc, alternatingEndPc };
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

	/**
	 * E.g. 127,128,127,128
	 * 
	 * @param provLineNrIndex_2
	 * @return
	 */
	private boolean isAlternating(int provLineNrIndex_2) {
		int provLineNr = this.lineNrAttr.lineNumber(provLineNrIndex_2);
		int checkLineNr = this.lineNrAttr.lineNumber(provLineNrIndex_2 - 1);
		int checkLineNr_2 = 0;
		if (checkLineNr > provLineNr
				&& (provLineNrIndex_2 + 1) < this.lineNrAttr.tableLength()) { // 127,128,127,!128!
			checkLineNr_2 = this.lineNrAttr.lineNumber(provLineNrIndex_2 + 1);
		} else if (checkLineNr < provLineNr && (provLineNrIndex_2 - 3) >= 0) { // !127!,128,127,128
			checkLineNr_2 = this.lineNrAttr.lineNumber(provLineNrIndex_2 - 3);
		} else {
			return false;
		}

		if (checkLineNr == checkLineNr_2)
			return true;
		else
			return false;
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

			if (this.lineNrAttr.lineNumber(i) == lineNr) {
				return i;
			}
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

	private int getNextBiggerLineNrPcThanMaxLineNrPc(int maxLineNrIndex) {
		int maxLineNr = lineNrAttr.lineNumber(maxLineNrIndex);

		for (int i = maxLineNrIndex + 1; i < this.lineNrAttr.tableLength(); i++) {
			int provLineNr = this.lineNrAttr.lineNumber(i);
			if (provLineNr > maxLineNr)
				return this.lineNrAttr.startPc(i);
		}
		return 0;
	}

}
