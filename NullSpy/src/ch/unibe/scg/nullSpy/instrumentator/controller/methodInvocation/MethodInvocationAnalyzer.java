package ch.unibe.scg.nullSpy.instrumentator.controller.methodInvocation;

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
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import ch.unibe.scg.nullSpy.instrumentator.controller.VariableAnalyzer;
import ch.unibe.scg.nullSpy.testRun.TestInstrumentor;

public class MethodInvocationAnalyzer extends VariableAnalyzer {

	private ConstPool constPool;
	private static int count = 0;

	public MethodInvocationAnalyzer(CtClass cc) {
		super(cc);
	}

	/**
	 * Get method receiver data of constructor and normal methods.
	 * 
	 * @throws CannotCompileException
	 * @throws BadBytecode
	 * @throws IOException
	 * @throws NotFoundException
	 */
	public void getMethodReceiver() throws CannotCompileException, BadBytecode,
			IOException, NotFoundException {
		getMethodReceiverData(cc.getDeclaredBehaviors());
	}

	/**
	 * Starts to collect data of method receiver
	 * 
	 * @param behaviorList
	 * @throws CannotCompileException
	 * @throws BadBytecode
	 * @throws IOException
	 * @throws NotFoundException
	 */
	private void getMethodReceiverData(CtBehavior[] behaviorList)
			throws CannotCompileException, BadBytecode, IOException,
			NotFoundException {

		// Printer p = new Printer();
		// System.out.println(cc.getName());
		// System.out.println();

		// FIXME: class choice
		// if (!cc.getName().equals("org.jhotdraw.util.PaletteLayout"))
		// return;

		for (CtBehavior behavior : behaviorList) {
			System.out.println(behavior.getName());

			// FIXME: method choice
			// if (!behavior.getName().equals("layoutContainer"))
			// continue;

			MethodInfo methodInfo = behavior.getMethodInfo2();
			constPool = methodInfo.getConstPool();
			CodeAttribute codeAttr = methodInfo.getCodeAttribute();

			if (codeAttr == null) {
				continue;
			}

			LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
					.getAttribute(LineNumberAttribute.tag);

			// FIXME: just printer mark
			// p.printBehavior(behavior, 0);
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

				if (isInvoke(op)) {

					MultipleLineManager multipleLineManager = new MultipleLineManager(
							behavior);
					ArrayList<Integer> multipleLineInterval = multipleLineManager
							.getMultipleLineInterval(pos);

					IntervalManager intervalManager = new IntervalManager(
							behavior);
					ArrayList<Integer> invocationInterval = intervalManager
							.getInvocationInterval(codeAttr, lineNrAttr, pos,
									multipleLineInterval);

					if (invocationInterval.size() == 0)
						continue;

					// store all receiver, the nested ones (as parameter) too
					checkInvocationInterval(behavior, invocationInterval);

					int lastInvocationPc = invocationInterval
							.get(invocationInterval.size() - 1);

					codeIter.move(lastInvocationPc);
					codeIter.next();

					System.out.println();

				}
			}
		}
	}

	// private ArrayList<Integer> getInvocationInterval(CodeAttribute codeAttr,
	// LineNumberAttribute lineNrAttr, int pos,
	// ArrayList<Integer> multipleLineInterval) throws BadBytecode {
	// ArrayList<Integer> invocationInterval = new ArrayList<>();
	// int startPos;
	// if (multipleLineInterval.size() == 0) {
	// int lineNr = lineNrAttr.toLineNumber(pos);
	// startPos = lineNrAttr.toStartPc(lineNr);
	//
	// // store bytecode interval until invocation
	// return getCleanInvocationInterval(codeAttr, startPos);
	// } else {
	// CodeIterator codeIter = codeAttr.iterator();
	// startPos = multipleLineInterval.get(0);
	// int endPos = multipleLineInterval.get(1);
	// codeIter.move(startPos);
	// int pos2 = pos;
	// while (codeIter.hasNext() && pos2 <= endPos) {
	// pos2 = codeIter.next();
	// invocationInterval.add(pos2);
	// }
	//
	// removeUnnecessaryOpcodesFromInvocationInterval(codeIter,
	// invocationInterval);
	// return invocationInterval;
	// }
	// }

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
	// private ArrayList<Integer> getCleanInvocationInterval(
	// CodeAttribute codeAttr, int startPos) throws BadBytecode {
	// LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
	// .getAttribute(LineNumberAttribute.tag);
	// CodeIterator codeIter = codeAttr.iterator();
	// ArrayList<Integer> invocationBytecodeInterval = new ArrayList<>();
	//
	// int startLineNr = lineNrAttr.toLineNumber(startPos);
	// codeIter.move(startPos);
	// int pos2 = codeIter.next();
	// invocationBytecodeInterval.add(pos2);
	//
	// pos2 = codeIter.next();
	//
	// int lineNr2 = lineNrAttr.toLineNumber(pos2);
	// int startPos2 = lineNrAttr.toStartPc(lineNr2);
	//
	// // adding all pc that has the same startPos -> get whole interval of
	// // the "source line"
	// while (startPos2 == startPos || lineNr2 == startLineNr) {
	// invocationBytecodeInterval.add(pos2);
	// if (!codeIter.hasNext())
	// break;
	// pos2 = codeIter.next();
	// lineNr2 = lineNrAttr.toLineNumber(pos2);
	// startPos2 = lineNrAttr.toStartPc(lineNr2);
	// }
	//
	// if (invocationBytecodeInterval.size() != 0)
	// removeUnnecessaryOpcodesFromInvocationInterval(codeIter,
	// invocationBytecodeInterval);
	//
	// return invocationBytecodeInterval;
	//
	// }

	// private void removeUnnecessaryOpcodesFromInvocationInterval(
	// CodeIterator codeIter, ArrayList<Integer> invocationBytecodeInterval) {
	//
	// if (invocationBytecodeInterval.size() == 0)
	// return;
	// int endPos = getIntervalEndPos(invocationBytecodeInterval);
	// int op = codeIter.byteAt(endPos);
	//
	// // remove the rest after invocation
	// while (!isInvoke(op) && invocationBytecodeInterval.size() != 0) {
	// int i = invocationBytecodeInterval.indexOf(endPos);
	// invocationBytecodeInterval.remove(i);
	//
	// if (invocationBytecodeInterval.size() == 0)
	// return;
	//
	// endPos = getIntervalEndPos(invocationBytecodeInterval);
	// op = codeIter.byteAt(endPos);
	// }
	// }

	/**
	 * Store receiver data by only checking the received interval (whole and
	 * subintervals)
	 * 
	 * @param behavior
	 * @param invocationInterval
	 * @throws BadBytecode
	 * @throws IOException
	 * @throws NotFoundException
	 */
	private void checkInvocationInterval(CtBehavior behavior,
			ArrayList<Integer> invocationInterval) throws BadBytecode,
			IOException, NotFoundException {

		CodeAttribute codeAttr = behavior.getMethodInfo().getCodeAttribute();
		CodeIterator codeIter = codeAttr.iterator();

		int startPos = invocationInterval.get(0);
		int endPos = getIntervalEndPos(invocationInterval);

		if (startPos == endPos)
			return;

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

				// FIXME: paramCount
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

				// FIXME: wrong index
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

				// print possible receiver just for testing
				LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
						.getAttribute(LineNumberAttribute.tag);
				int lineNr = lineNrAttr.toLineNumber(pos);
				System.out.println("LineNr: " + lineNr + ", StartPc: "
						+ possibleReceiverStartPc);
				// end testing

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
	 * 
	 * @param endPc
	 * @param possibleReceiverIntervalList
	 * @param paramCount
	 * @param possibleReceiverListSize
	 * @param possibleReceiverIndex
	 * @param startPc
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

	private void storeMethodreceiverData(CtBehavior behavior,
			int possibleReceiverStartPc) throws BadBytecode, IOException {
		MethodInfo methodInfo = behavior.getMethodInfo2();
		CodeAttribute codeAttr = methodInfo.getCodeAttribute();
		CodeIterator codeIter = codeAttr.iterator();
		LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
				.getAttribute(LineNumberAttribute.tag);
		int lineNr = lineNrAttr.toLineNumber(possibleReceiverStartPc);

		ArrayList<String> varData = new ArrayList<>();

		codeIter.move(possibleReceiverStartPc);
		int pos = codeIter.next();
		int op = codeIter.byteAt(pos);
		int op2 = 0;

		ReceiverData receiverData = new ReceiverData(behavior);

		if (isField(op)) {
			// field
			varData = receiverData.getFieldData(varData, lineNr, pos,
					MethodInvocationAnalyzer.count);
			op2 = op;

		} else if (isLocalVar(op)) {
			// localVar
			varData = receiverData.getLocalVarData(varData, lineNr, pos,
					MethodInvocationAnalyzer.count);
			op2 = op;
		}

		// FIXME: Main || Test
		// MainProjectModifier.csv.addCsvLine(varData);
		TestInstrumentor.csv.addCsvLine(varData);

		if (codeIter.hasNext()) {
			pos = codeIter.next();
			op = codeIter.byteAt(pos);
			if (op == Opcode.GETFIELD) {
				storeMethodreceiverData(behavior, pos);
			}
		}

		if (shouldCountBeIncremented(op2, op) || !codeIter.hasNext()) {
			count++;
		}

	}

	private boolean shouldCountBeIncremented(int currentOp, int nextOp) {
		return (isLocalVar(currentOp) && nextOp != Opcode.GETFIELD)
				|| (isField(currentOp) && nextOp != Opcode.GETFIELD);
	}

	private boolean isLocalVar(int op) {
		return op == Opcode.AALOAD || op == Opcode.ALOAD_0
				|| op == Opcode.ALOAD_1 || op == Opcode.ALOAD_2
				|| op == Opcode.ALOAD_3 || op == Opcode.ALOAD;
	}

	private boolean isField(int op) {
		return op == Opcode.GETSTATIC || op == Opcode.GETFIELD;
	}

	/**
	 * Recursively checking the interval, subinterval and add possible receiver
	 * to list
	 * 
	 * @param possibleReceiverIntervalList
	 * @param codeIter
	 * @param startPos
	 * @param endPos
	 * @throws BadBytecode
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

		} else {
			if (!codeIter.hasNext())
				return;
			pos = codeIter.next();

		}

		possibleReceiverIntervalList.add(new MethodReceiverInterval(startPos,
				pos2));

		if (pos != endPos)
			filterPossibleReceiver(possibleReceiverIntervalList, codeIter, pos,
					endPos);
		else
			return;

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
	 * Returns the class name of the target object, which the method is called
	 * on.
	 */
	public String getClassName(CodeIterator codeIter, int pos) {
		String cname;

		int op = codeIter.byteAt(pos);
		int index = codeIter.u16bitAt(pos + 1);

		if (op == Opcode.INVOKEINTERFACE)
			cname = constPool.getInterfaceMethodrefClassName(index);
		else
			cname = constPool.getMethodrefClassName(index);

		if (cname.charAt(0) == '[')
			cname = Descriptor.toClassName(cname);

		return cname;
	}

	/**
	 * Returns the name of the called method.
	 */
	public String getMethodName(int nameAndType) {
		return constPool.getUtf8Info(constPool.getNameAndTypeName(nameAndType));
	}

	/**
	 * Returns the method signature (the parameter types and the return type).
	 * The method signature is represented by a character string called method
	 * descriptor, which is defined in the JVM specification.
	 *
	 * @see javassist.CtBehavior#getSignature()
	 * @see javassist.bytecode.Descriptor
	 * @since 3.1
	 */
	public String getSignature(int nameAndType) {
		return constPool.getUtf8Info(constPool
				.getNameAndTypeDescriptor(nameAndType));
	}

	/**
	 * Returns true if the called method is of a superclass of the current
	 * class.
	 */
	public boolean isSuper(CtBehavior behavior, int pos) {
		CodeIterator codeIter = behavior.getMethodInfo().getCodeAttribute()
				.iterator();
		return codeIter.byteAt(pos) == Opcode.INVOKESPECIAL
				&& !behavior.getDeclaringClass().getName()
						.equals(getClassName(codeIter, pos));
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
