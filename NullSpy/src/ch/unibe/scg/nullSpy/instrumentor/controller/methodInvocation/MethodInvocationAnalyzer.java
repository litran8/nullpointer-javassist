package ch.unibe.scg.nullSpy.instrumentor.controller.methodInvocation;

import java.io.IOException;
import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import ch.unibe.scg.nullSpy.instrumentor.controller.Analyzer;
import ch.unibe.scg.nullSpy.run.MainProjectModifier;

public class MethodInvocationAnalyzer extends Analyzer {

	private ConstPool constPool;
	private static int counter = 0;

	public MethodInvocationAnalyzer(CtClass cc) {
		super(cc);
	}

	/**
	 * Get method receiver data of constructor and normal methods.
	 */
	public void getMethodReceiver() throws CannotCompileException, BadBytecode,
			IOException, NotFoundException {
		getMethodReceiverData(cc.getDeclaredBehaviors());
	}

	/**
	 * Starts to collect data of method receiver
	 */
	private void getMethodReceiverData(CtBehavior[] behaviorList)
			throws CannotCompileException, BadBytecode, IOException,
			NotFoundException {

		// TODO: class choice debug
		// if (!cc.getName().equals("org.jhotdraw.application.DrawApplication"))
		// return;

		for (CtBehavior behavior : behaviorList) {

			// TODO: method choice debug
			// if (!behavior.getName().equals("createColorChoice"))
			// continue;

			constPool = getConstPool(behavior);
			CodeAttribute codeAttr = getCodeAttribute(behavior);

			if (codeAttr == null) {
				continue;
			}

			// TODO: just printer mark debug
			// Printer p = new Printer();
			// p.printBehavior(behavior, 0);
			// LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
			// .getAttribute(LineNumberAttribute.tag);
			// for (int j = 0; j < lineNrAttr.tableLength(); j++) {
			// int line = lineNrAttr.lineNumber(j);
			// int pc = lineNrAttr.toStartPc(line);
			// System.out.println("pc: " + pc + ", line: "
			// + lineNrAttr.lineNumber(j));
			// }

			CodeIterator codeIter = codeAttr.iterator();
			codeIter.begin();

			while (codeIter.hasNext()) {
				int pos = codeIter.next();
				int op = codeIter.byteAt(pos);

				// TODO: debug
				// if (pos != 31) {
				// continue;
				// }

				if (isInvoke(op)) {

					MultipleLineManager multipleLineManager = new MultipleLineManager(
							behavior);
					int[] multipleLineInterval = multipleLineManager
							.getMultipleLineInterval(pos);

					IntervalManager intervalManager = new IntervalManager(
							behavior);
					// ArrayList<Integer> invocationInterval = intervalManager
					// .getInvocationInterval(null, pos);
					ArrayList<Integer> invocationInterval = intervalManager
							.getInvocationInterval(multipleLineInterval, pos);

					if (invocationInterval.size() == 0)
						continue;

					// store all receiver, the nested ones (as parameter) too
					checkInvocationInterval(behavior, invocationInterval);

					int lastInvocationPc = invocationInterval
							.get(invocationInterval.size() - 1);

					codeIter.move(lastInvocationPc);
					codeIter.next();
				}
			}
		}
	}

	/**
	 * Store receiver data by only checking the received interval (whole and
	 * subintervals)
	 */
	private void checkInvocationInterval(CtBehavior behavior,
			ArrayList<Integer> invocationInterval) throws BadBytecode,
			IOException, NotFoundException {

		int startPos = invocationInterval.get(0);
		int endPos = getIntervalEndPos(invocationInterval);

		if (startPos == endPos)
			return;

		CodeIterator codeIter = getCodeIterator(behavior);
		codeIter.move(startPos);

		int pos = startPos;

		ArrayList<Integer> invocationPcList = new ArrayList<>();
		ArrayList<MethodReceiverInterval> receiverIntervalList = new ArrayList<>();

		while (codeIter.hasNext() && pos < endPos) {
			pos = codeIter.next();
			int op = codeIter.byteAt(pos);

			if (isInvoke(op)) {

				int startPos2 = getStartPos(invocationInterval, codeIter,
						startPos, invocationPcList, receiverIntervalList);

				invocationPcList.add(pos);

				int nameAndType = getNameAndType(codeIter, pos);

				int paramCount = getParameterAmount(nameAndType);

				// get receiver list
				filterPossibleReceiver(receiverIntervalList, codeIter,
						startPos2, pos);

				if (op == Opcode.INVOKESTATIC) {
					if (paramCount == 0) {
						receiverIntervalList.add(new MethodReceiverInterval(
								pos, pos));
					} else {
						int index = receiverIntervalList.size() - paramCount;
						MethodReceiverInterval firstParam = receiverIntervalList
								.get(index);
						for (int i = index; i < receiverIntervalList.size(); i++)
							receiverIntervalList.remove(i);
						receiverIntervalList.add(new MethodReceiverInterval(
								firstParam.startPc, pos));
					}
					if (codeIter.hasNext()) {
						startPos = codeIter.next();
						codeIter.move(startPos);
					}
					continue;
				}

				if (op != Opcode.INVOKESTATIC
						&& receiverIntervalList.size() <= paramCount) {
					updatePossibleReceiverIntervalList(codeIter,
							invocationInterval, paramCount);
					checkInvocationInterval(behavior, invocationInterval);
					return;
				}

				// get right receiver list index
				int possibleReceiverIndex = getMethodReceiverIndex(codeIter,
						receiverIntervalList, op, paramCount);

				MethodReceiverInterval possibleReceiverInterval = receiverIntervalList
						.get(possibleReceiverIndex);

				// ignore (final) or combine (nested) methodReceiver if it
				// includes methodInvocation
				if (doesMethodReceiverIncludeMethodInvocation(codeIter,
						possibleReceiverInterval)) {

					if (pos == endPos)
						return;

					updateReceiverList(receiverIntervalList,
							possibleReceiverIndex, pos, paramCount);

					codeIter.move(pos);
					codeIter.next();
					continue;
				}

				int possibleReceiverStartPc = possibleReceiverInterval.startPc;

				// actually storing data in csv file
				storeMethodreceiverData(behavior, possibleReceiverStartPc);

				codeIter.move(pos);
				codeIter.next();

				if (pos != endPos) {
					// updating receiver list
					updateReceiverList(receiverIntervalList,
							possibleReceiverIndex, pos, paramCount);
					startPos = codeIter.next();

					op = codeIter.byteAt(startPos);

					if (op == Opcode.CHECKCAST) {
						int lastIndex = receiverIntervalList.size() - 1;
						receiverIntervalList.get(lastIndex).endPc = startPos;
						if (codeIter.hasNext())
							startPos = codeIter.next();
						codeIter.move(startPos);
					} else {
						codeIter.move(pos);
						codeIter.next();
					}
				} else {
					codeIter.move(startPos);
					codeIter.next();
					return;
				}
			}
		}
	}

	private int getStartPos(ArrayList<Integer> invocationBytecodeInterval,
			CodeIterator codeIter, int startPos,
			ArrayList<Integer> invocationPcList,
			ArrayList<MethodReceiverInterval> possibleReceiverIntervalList) {
		int startPos2 = startPos;

		if (invocationPcList.size() != 0) {

			int lastAddedReceiverEndPc = possibleReceiverIntervalList
					.get(possibleReceiverIntervalList.size() - 1).endPc;

			if (codeIter.byteAt(lastAddedReceiverEndPc) != Opcode.CHECKCAST) {
				int lastAddedInvocationPos = invocationPcList
						.get(invocationPcList.size() - 1);
				int index = invocationBytecodeInterval
						.indexOf(lastAddedInvocationPos);
				startPos2 = invocationBytecodeInterval.get(index + 1);
			}
		}
		return startPos2;
	}

	private void updatePossibleReceiverIntervalList(CodeIterator codeIter,
			ArrayList<Integer> invocationBytecodeInterval, int paramCount)
			throws BadBytecode {
		codeIter.begin();
		int pos = 0;
		ArrayList<Integer> provInterval = new ArrayList<>();
		while (codeIter.hasNext()
				&& pos < invocationBytecodeInterval
						.get(invocationBytecodeInterval.size() - 1)) {
			pos = codeIter.next();
			provInterval.add(pos);
		}

		int startPcIndex = provInterval.indexOf(invocationBytecodeInterval
				.get(0));

		for (int i = startPcIndex - 1; i >= 0; i--) {
			int provPc = provInterval.get(i);
			invocationBytecodeInterval.add(0, provPc);
			int op = codeIter.byteAt(provPc);
			String opString = Mnemonic.OPCODE[op];
			if (op == Opcode.GETFIELD || opString.matches("dup.*"))
				continue;
			else if (opString.matches("aload.*") || opString.matches("new")) {
				return;
			}
		}
	}

	private boolean doesMethodReceiverIncludeMethodInvocation(
			CodeIterator codeIter,
			MethodReceiverInterval possibleReceiverInterval) throws BadBytecode {
		int pos = possibleReceiverInterval.startPc;
		int endPc = possibleReceiverInterval.endPc;
		codeIter.move(pos);
		codeIter.next();

		while (pos <= endPc) {

			int op = codeIter.byteAt(pos);

			if (isInvoke(op) && op != Opcode.INVOKESTATIC)
				return true;

			pos = codeIter.next();
		}
		return false;
	}

	private int getMethodReceiverIndex(CodeIterator codeIter,
			ArrayList<MethodReceiverInterval> methodReceiverIntervalList,
			int op, int paramCount) {
		int listSize = methodReceiverIntervalList.size();
		int methodReceiverIndex = listSize - paramCount - 1;

		MethodReceiverInterval methodReceiverInterval;
		int methodReceiverStartPc;
		int methodReceiverStartOp;

		// invokestatic does not need receiver, so the first parameter
		// of method is the starting point
		if (op == Opcode.INVOKESTATIC) {
			methodReceiverIndex = listSize - paramCount - 1;
			if (methodReceiverIndex >= 0) {
				methodReceiverInterval = methodReceiverIntervalList
						.get(methodReceiverIndex);
				methodReceiverStartPc = methodReceiverInterval.startPc;
				methodReceiverStartOp = codeIter.byteAt(methodReceiverStartPc);

				if (methodReceiverStartOp != Opcode.GETSTATIC) {
					methodReceiverIndex = listSize - paramCount;
				}
			} else {
				methodReceiverIndex = listSize - paramCount;
			}
		}
		return methodReceiverIndex;
	}

	/**
	 * Wrap nested invocation into one receiverList entry
	 */
	private void updateReceiverList(
			ArrayList<MethodReceiverInterval> possibleReceiverIntervalList,
			int possibleReceiverIndex, int endPc, int paramCount) {
		MethodReceiverInterval toBeReplaced = possibleReceiverIntervalList
				.get(possibleReceiverIndex);
		int startPc = toBeReplaced.startPc;
		MethodReceiverInterval replace = new MethodReceiverInterval(startPc,
				endPc);
		int listSize = possibleReceiverIntervalList.size();

		if (paramCount != 0) {
			for (int i = listSize - 1; i > possibleReceiverIndex; i--) {
				possibleReceiverIntervalList.remove(i);
			}
		}
		possibleReceiverIntervalList.set(possibleReceiverIndex, replace);
	}

	public void storeMethodreceiverData(CtBehavior behavior,
			int possibleReceiverStartPc) throws BadBytecode, IOException {
		LineNumberAttribute lineNrAttr = getLineNumberAttribute(behavior);

		CodeIterator codeIter = getCodeIterator(behavior);
		codeIter.move(possibleReceiverStartPc);
		//
		// if (!codeIter.hasNext()) {
		// return;
		// }

		int pos = codeIter.next();
		int op = codeIter.byteAt(pos);
		int op2 = 0;

		int lineNr = lineNrAttr.toLineNumber(possibleReceiverStartPc);

		ArrayList<String> varData = new ArrayList<>();
		ReceiverData receiverData = new ReceiverData(behavior);

		if (isField(op)) {
			// field
			varData = receiverData.getFieldData(lineNr, pos,
					MethodInvocationAnalyzer.counter);
			op2 = op;

		} else if (isLocalVar(op)) {
			// localVar
			varData = receiverData.getLocalVarData(lineNr, pos,
					MethodInvocationAnalyzer.counter);
			op2 = op;
		}

		// TODO: Main || Test
		MainProjectModifier.csv.addCsvLine(varData);
		// TestInstrumentor.csv.addCsvLine(varData);

		if (codeIter.hasNext()) {
			pos = codeIter.next();
			op = codeIter.byteAt(pos);
			if (op == Opcode.GETFIELD) {
				storeMethodreceiverData(behavior, pos);
			}
		}

		if (shouldCountBeIncremented(op2, op) || !codeIter.hasNext()) {
			counter++;
		}

	}

	private boolean shouldCountBeIncremented(int currentOp, int nextOp) {
		return (isLocalVar(currentOp) && nextOp != Opcode.GETFIELD)
				|| (isField(currentOp) && nextOp != Opcode.GETFIELD);
	}

	public static int getCounter() {
		return counter;
	}

	public static void setCounter(int count) {
		MethodInvocationAnalyzer.counter = count;
	}

	private boolean isLocalVar(int op) {
		return op == Opcode.ALOAD_0 || op == Opcode.ALOAD_1
				|| op == Opcode.ALOAD_2 || op == Opcode.ALOAD_3
				|| op == Opcode.ALOAD;
	}

	private boolean isField(int op) {
		return op == Opcode.GETSTATIC || op == Opcode.GETFIELD;
	}

	/**
	 * Recursively checking the interval, subinterval and add possible receiver
	 * to list
	 */
	private void filterPossibleReceiver(
			ArrayList<MethodReceiverInterval> possibleReceiverIntervalList,
			CodeIterator codeIter, int startPos, int endPos) throws BadBytecode {

		codeIter.move(startPos);

		int pos = codeIter.next();
		int pos2 = pos;

		int op = codeIter.byteAt(pos);

		if (isInvoke(op)) {
			return;
		} else if (op == Opcode.NEW || op == Opcode.NEWARRAY) { // new...invokespecial
			pos = codeIter.next();
			op = codeIter.byteAt(pos);
			pos2 = pos;

			pos = codeIter.next();

		} else if ((isField(op) && op != Opcode.GETFIELD) || isLocalVar(op)) {
			// only getstatic because getfield can't be at the beginning

			pos = codeIter.next();
			op = codeIter.byteAt(pos);

			while (op == Opcode.GETFIELD) {
				pos2 = pos; // marks the end of possible receiver interval, also
							// nested one
				pos = codeIter.next();
				op = codeIter.byteAt(pos);
			}

			if (op == Opcode.CHECKCAST) {
				pos2 = pos;
				pos = codeIter.next();
			}

		} else if (op == Opcode.AALOAD) {
			ArrayList<Integer> posList = new ArrayList<>();
			codeIter.begin();
			pos = 0;
			while (codeIter.hasNext() && pos < pos2) {
				pos = codeIter.next();
				posList.add(pos);
			}

			int posListSize = posList.size();

			for (int i = posListSize - 1; i >= 0; i--) {
				int provOp = codeIter.byteAt(posList.get(i));
				if (isLocalVar(provOp) || isField(provOp)) {
					int amountOfNonReceiver = posListSize - 1 - i;
					removeReceiverFromList(possibleReceiverIntervalList,
							amountOfNonReceiver - 1);
					startPos = possibleReceiverIntervalList
							.get(possibleReceiverIntervalList.size() - 1).startPc;
					removeReceiverFromList(possibleReceiverIntervalList, 1);
					break;
				}
			}
			pos = codeIter.next();
			op = codeIter.byteAt(pos);
			if (op == Opcode.CHECKCAST) {
				pos2 = pos;
				pos = codeIter.next();
			}

		} else {
			if (!codeIter.hasNext())
				return;

			if (Mnemonic.OPCODE[op].matches(".*store.*")
					&& possibleReceiverIntervalList.size() > 0)
				possibleReceiverIntervalList
						.remove(possibleReceiverIntervalList.size() - 1);
			pos = codeIter.next();

		}

		if (isReceiverCandidate(op))
			possibleReceiverIntervalList.add(new MethodReceiverInterval(
					startPos, pos2));

		if (pos != endPos)
			filterPossibleReceiver(possibleReceiverIntervalList, codeIter, pos,
					endPos);
		else
			return;

	}

	private boolean isReceiverCandidate(int op) {
		String opString = Mnemonic.OPCODE[op];
		return !((opString.matches(".*store.*") || opString.matches(".*ret.*")
				|| op == Opcode.GOTO || op == Opcode.GOTO_W || op == Opcode.IINC));
	}

	private void removeReceiverFromList(
			ArrayList<MethodReceiverInterval> possibleReceiverIntervalList,
			int removeAmaount) {
		for (int i = removeAmaount; i > 0; i--) {
			possibleReceiverIntervalList.remove(possibleReceiverIntervalList
					.size() - 1);
		}

	}

	private Integer getIntervalEndPos(
			ArrayList<Integer> invocationBytecodeInterval) {
		return invocationBytecodeInterval
				.get(invocationBytecodeInterval.size() - 1);
	}

	private int getParameterAmount(int nameAndType) throws BadBytecode {

		String methodInvokationSignature = getSignature(nameAndType);
		int paramCount = 0;
		paramCount = Descriptor.numOfParameters(methodInvokationSignature);
		return paramCount;

	}

	private boolean isInvoke(int op) {
		return op == Opcode.INVOKEINTERFACE || op == Opcode.INVOKESPECIAL
				|| op == Opcode.INVOKEVIRTUAL || op == Opcode.INVOKESTATIC;
	}

	private int getNameAndType(CodeIterator codeIter, int pos) {
		int op = codeIter.byteAt(pos);
		int index = codeIter.u16bitAt(pos + 1);

		if (op == Opcode.INVOKEINTERFACE)
			return constPool.getInterfaceMethodrefNameAndType(index);
		else
			return constPool.getMethodrefNameAndType(index);
	}

	/**
	 * Returns the method signature (the parameter types and the return type).
	 * The method signature is represented by a character string called method
	 * descriptor, which is defined in the JVM specification.
	 */
	public String getSignature(int nameAndType) {
		return constPool.getUtf8Info(constPool
				.getNameAndTypeDescriptor(nameAndType));
	}

	private class MethodReceiverInterval {
		public int startPc;
		public int endPc;

		public MethodReceiverInterval(int startPc, int endPc) {
			this.startPc = startPc;
			this.endPc = endPc;
		}

		public String toString() {
			return "StartPc: " + this.startPc + ", EndPc: " + this.endPc;
		}
	}

}