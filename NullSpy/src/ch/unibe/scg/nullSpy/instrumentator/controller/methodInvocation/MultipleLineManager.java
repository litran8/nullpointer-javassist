package ch.unibe.scg.nullSpy.instrumentator.controller.methodInvocation;

import java.util.ArrayList;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LineNumberAttribute.Pc;
import javassist.bytecode.Mnemonic;

public class MultipleLineManager {

	private CtBehavior behavior;
	private LineNumberAttribute lineNrAttr;
	private CodeIterator codeIter;

	// public MultipleLineManager(CtBehavior behavior) {
	// this.behavior = behavior;
	// }

	public MultipleLineManager(CtBehavior behavior) {
		this.behavior = behavior;
		CodeAttribute codeAttr = this.behavior.getMethodInfo()
				.getCodeAttribute();
		this.lineNrAttr = (LineNumberAttribute) codeAttr
				.getAttribute(LineNumberAttribute.tag);
		this.codeIter = codeAttr.iterator();
	}

	public ArrayList<Integer> getMultipleLineInterval2(int pos) {
		ArrayList<Integer> multipleLineInterval = new ArrayList<>();
		int lineAttrSize = lineNrAttr.tableLength();
		boolean isAlternating = false;
		int lineNr = this.lineNrAttr.toLineNumber(pos);
		int lineNrAttrIndex = getIndexOfLineNrFromLineNrAttr(lineNr);

		for (int i = lineNrAttrIndex + 1; i < lineAttrSize; i++) {
			int provLineNr = this.lineNrAttr.lineNumber(i);
			if (provLineNr <= lineNr) {
				// 2: second index, e.g. 127...!127!
				int provLineNrIndex_2 = getSmallestWrapLineNrIndex(i);
				// 1: first index, e.g. !127!...127
				int provLineNrIndex_1 = getCorrespondingLineNrIndex(provLineNrIndex_2);
				isAlternating = isAlternating(provLineNrIndex_2);
			}
		}
		return null;
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

	public ArrayList<Integer> getMultipleLineInterval(int pos)
			throws BadBytecode {
		ArrayList<Integer> multipleLineInterval = new ArrayList<>();

		CodeAttribute codeAttr = this.behavior.getMethodInfo()
				.getCodeAttribute();

		LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
				.getAttribute(LineNumberAttribute.tag);
		int startPc;
		int endPc = 0;
		int lineNr = lineNrAttr.toLineNumber(pos);
		CodeIterator codeIter = codeAttr.iterator();

		int lineNrAttrIndex = getIndexOfLineNrFromLineNrAttr(lineNr);

		int nextLineNr = lineNrAttrIndex + 1 < lineNrAttr.tableLength() ? lineNrAttr
				.lineNumber(lineNrAttrIndex + 1) : 0;
		boolean isAlternating = false;

		for (int i = lineNrAttrIndex + 1; i < lineNrAttr.tableLength(); i++) {

			if (lineNrAttr.lineNumber(i) <= lineNr
					|| (i > lineNrAttrIndex + 1 && lineNrAttr.lineNumber(i) <= nextLineNr)) {
				int possibleStartLineNr = lineNrAttr.lineNumber(i);
				// int possibleStartLineNr = lineNrAttr.lineNumber(i) < lineNr ?
				// lineNrAttr
				// .lineNumber(i) : lineNr;

				if (lineNrAttrIndex == 0 && i - lineNrAttrIndex <= 1) {
					return multipleLineInterval;
				} else if (possibleStartLineNr != lineNr) {
					for (int k = i + 1; k < lineNrAttr.tableLength(); k++) {
						int possibleStartLineNr2 = lineNrAttr.lineNumber(k);
						if (possibleStartLineNr2 == nextLineNr) {
							isAlternating = true;
							break;
						}

					}
					boolean isMultipleLine = false;
					if (!isAlternating) {
						for (int j = lineNrAttrIndex - 1; j >= 0; j--) {
							if (lineNrAttr.lineNumber(j) == possibleStartLineNr) {
								isMultipleLine = true;
								break;
							}
						}
						for (int k = i; k < lineNrAttr.tableLength(); k++) {
							if (lineNrAttr.lineNumber(k) == possibleStartLineNr) {
								isMultipleLine = true;
								i = k;
								break;
							}
						}
						if (!isMultipleLine)
							return multipleLineInterval;
					}
				}

				// for (int k = i; k < lineNrAttr.tableLength(); k++) {
				// if (lineNrAttr.lineNumber(j) != possibleStartLineNr) {
				// i = k;
				// break;
				// } else
				// return multipleLineInterval;
				// }
				// else if (lineNrAttr.lineNumber(j) == possibleStartLineNr)
				// break;
				int maxLineNrIndex = getMaxLineNrIndex(lineNrAttr, i);

				codeIter.move(pos);

				if (i != lineNrAttr.tableLength() - 1) {

					int nextLineNrStartPc = getNextBiggerLineNrPcThanMaxLineNrPc(
							lineNrAttr, maxLineNrIndex);
					while (codeIter.hasNext() && pos < nextLineNrStartPc) {
						endPc = pos;
						pos = codeIter.next();
					}
				} else {
					while (codeIter.hasNext()) {
						endPc = pos;
						pos = codeIter.next();
					}
				}

				int op = codeIter.byteAt(endPc);

				int maxLineNr = lineNrAttr.lineNumber(maxLineNrIndex);
				int lineNrDiff = maxLineNr - possibleStartLineNr;
				int startPc2 = 0;
				int startPc3 = 0;
				Pc nearPc2 = null;
				Pc nearPc3 = null;
				// FIXME: startLine -1
				if (isAlternating) {
					startPc = lineNrAttr.toStartPc(lineNr);
					startPc2 = lineNrAttr.toStartPc(168);
				} else if (lineNrDiff > 1 && i > 0
						&& possibleStartLineNr != lineNrAttr.lineNumber(0)
						&& !Mnemonic.OPCODE[op].matches("if.*")) {
					possibleStartLineNr = possibleStartLineNr - 1 <= lineNr ? possibleStartLineNr - 1
							: lineNr;
					startPc = lineNrAttr.toStartPc(possibleStartLineNr);
					startPc2 = lineNrAttr.toStartPc(168);
					if (startPc < 0)
						startPc = lineNrAttr.toStartPc(possibleStartLineNr + 1);
				} else {
					startPc = lineNrAttr.toStartPc(possibleStartLineNr);
					startPc2 = lineNrAttr.toStartPc(168);
					nearPc2 = lineNrAttr.toNearPc(168);
					startPc3 = lineNrAttr.toStartPc(169);
					nearPc3 = lineNrAttr.toNearPc(169);
				}

				multipleLineInterval.add(startPc);

				getNestedMultipleLineInterval(multipleLineInterval, lineNrAttr,
						i, possibleStartLineNr);

				startPc = getMinPc(multipleLineInterval);

				multipleLineInterval.clear();
				multipleLineInterval.add(startPc);
				multipleLineInterval.add(endPc);
				return multipleLineInterval;
			}
		}

		return multipleLineInterval;

	}

	private int getIndexOfLineNrFromLineNrAttr(int lineNr) {
		for (int i = 0; i < this.lineNrAttr.tableLength(); i++) {

			if (this.lineNrAttr.lineNumber(i) == lineNr) {
				return i;
			}
		}
		return 0;
	}

	private int getMaxLineNrIndex(LineNumberAttribute lineNrAttr,
			int possibleStartLineNrIndex) {
		int startLineNr = lineNrAttr.lineNumber(possibleStartLineNrIndex);
		int maxLineNrIndex = 0;
		int maxLineNr = startLineNr;
		for (int i = possibleStartLineNrIndex - 1; i >= 0; i--) {
			int provLineNr = lineNrAttr.lineNumber(i);
			if (provLineNr > maxLineNr) {
				maxLineNrIndex = i;
				maxLineNr = lineNrAttr.lineNumber(maxLineNrIndex);
			} else if (provLineNr == startLineNr)
				break;

		}
		return maxLineNrIndex;
	}

	private int getNextBiggerLineNrPcThanMaxLineNrPc(
			LineNumberAttribute lineNrAttr, int maxLineNrIndex) {
		int maxLineNr = lineNrAttr.lineNumber(maxLineNrIndex);

		for (int i = maxLineNrIndex + 1; i < lineNrAttr.tableLength(); i++) {
			int provLineNr = lineNrAttr.lineNumber(i);
			if (provLineNr > maxLineNr)
				return lineNrAttr.startPc(i);
		}
		return 0;
	}

	private void getNestedMultipleLineInterval(
			ArrayList<Integer> multipleLineInterval,
			LineNumberAttribute lineNrAttr, int lineNrAttrIndex,
			int possibleStartLineNr) {
		for (int i = lineNrAttrIndex; i < lineNrAttr.tableLength(); i++) {
			int provLineNr = lineNrAttr.lineNumber(i);
			if (provLineNr <= possibleStartLineNr) {
				for (int j = lineNrAttrIndex; j >= 0; j--) {
					int provLineNr2 = lineNrAttr.lineNumber(j);

					if (provLineNr2 == provLineNr) {
						if (!multipleLineInterval.contains(lineNrAttr
								.toStartPc(provLineNr)))
							multipleLineInterval.add(lineNrAttr
									.toStartPc(provLineNr));
						getNestedMultipleLineInterval(multipleLineInterval,
								lineNrAttr, i + 1, provLineNr);
						return;
					}
				}

			}
		}
	}

	private int getMinPc(ArrayList<Integer> multipleLineInterval)
			throws BadBytecode {
		int res = multipleLineInterval.get(0);
		for (int i = 1; i < multipleLineInterval.size(); i++) {
			int prov = multipleLineInterval.get(i);
			if (prov < res)
				res = prov;
		}
		return res;

	}

}
