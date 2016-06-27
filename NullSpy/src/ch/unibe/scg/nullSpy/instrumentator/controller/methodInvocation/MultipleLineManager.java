package ch.unibe.scg.nullSpy.instrumentator.controller.methodInvocation;

import java.util.ArrayList;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.Mnemonic;

public class MultipleLineManager {

	private CtBehavior behavior;

	public MultipleLineManager(CtBehavior behavior) {
		this.behavior = behavior;
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

		int lineNrAttrIndex = getIndexOfLineNrFromLineNrAttr(lineNr, lineNrAttr);

		int nextLineNr = lineNrAttrIndex + 1 < lineNrAttr.tableLength() ? lineNrAttr
				.lineNumber(lineNrAttrIndex + 1) : 0;
		boolean isAlternating = false;

		for (int i = lineNrAttrIndex + 1; i < lineNrAttr.tableLength(); i++) {

			if (lineNrAttr.lineNumber(i) <= lineNr) {
				int possibleStartLineNr = lineNrAttr.lineNumber(i);

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
					if (!isAlternating) {
						for (int j = lineNrAttrIndex - 1; j >= 0; j--) {
							if (j == 0
									&& lineNrAttr.lineNumber(j) != possibleStartLineNr) {
								return multipleLineInterval;
							} else if (lineNrAttr.lineNumber(j) == possibleStartLineNr)
								break;
						}
					}
				}

				int maxLineNrIndex = getMaxLineNrIndex(lineNrAttr, i);

				int maxLineNr = lineNrAttr.lineNumber(maxLineNrIndex);

				int lineNrDiff = maxLineNr - possibleStartLineNr;

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

				// FIXME: startLine -1
				if (isAlternating) {
					startPc = lineNrAttr.toStartPc(lineNr);
				} else if (lineNrDiff > 1 && i > 0
						&& possibleStartLineNr != lineNrAttr.lineNumber(0)
						&& !Mnemonic.OPCODE[op].matches("if.*")) {
					startPc = lineNrAttr.toStartPc(possibleStartLineNr - 1);
					if (startPc < 0)
						startPc = lineNrAttr.toStartPc(possibleStartLineNr);
				} else {
					startPc = lineNrAttr.toStartPc(possibleStartLineNr);
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

	private int getIndexOfLineNrFromLineNrAttr(int lineNr,
			LineNumberAttribute lineNrAttr) {
		for (int i = 0; i < lineNrAttr.tableLength(); i++) {

			if (lineNrAttr.lineNumber(i) == lineNr) {
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
