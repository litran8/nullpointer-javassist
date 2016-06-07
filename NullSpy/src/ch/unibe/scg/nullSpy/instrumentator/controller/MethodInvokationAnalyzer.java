package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import ch.unibe.scg.nullSpy.instrumentator.model.Variable;

public class MethodInvokationAnalyzer extends VariableAnalyzer {

	private HashMap<String, HashMap<Integer, Variable>> methodInvokationVarMap;
	private HashMap<Integer, Variable> methodInvokationVarDataMap;
	private CsvFileCreator csvCreator;
	private ConstPool constPool;

	public MethodInvokationAnalyzer(CtClass cc,
			HashMap<String, HashMap<Integer, Variable>> methodInvokationVarMap,
			CsvFileCreator csvCreator) {
		super(cc);
		this.methodInvokationVarMap = methodInvokationVarMap;
		this.methodInvokationVarDataMap = new HashMap<>();
		this.csvCreator = csvCreator;
	}

	public void getMethodInvokationVar() throws CannotCompileException,
			BadBytecode, IOException {
		getMethodInvokationVarData(cc.getDeclaredConstructors());
		getMethodInvokationVarData(cc.getDeclaredBehaviors());

	}

	private void getMethodInvokationVarData(CtBehavior[] behaviorList)
			throws CannotCompileException, BadBytecode, IOException {

		MethodInfo methodInfo;
		CodeAttribute codeAttr;
		CodeIterator codeIter;
		LineNumberAttribute lineNrAttr;
		LocalVariableAttribute localVarAttr;

		Printer p = new Printer();

		for (CtBehavior behavior : behaviorList) {

			methodInfo = behavior.getMethodInfo2();
			constPool = methodInfo.getConstPool();
			codeAttr = methodInfo.getCodeAttribute();

			if (codeAttr == null) {
				continue;
			}

			lineNrAttr = (LineNumberAttribute) codeAttr
					.getAttribute(LineNumberAttribute.tag);
			localVarAttr = (LocalVariableAttribute) codeAttr
					.getAttribute(LocalVariableAttribute.tag);

			codeIter = codeAttr.iterator();
			codeIter.begin();
			int testNr = 0;

			while (codeIter.hasNext()) {
				int pos = codeIter.next();
				int op = codeIter.byteAt(pos);

				if (isInvoke(op)) {

					int lineNr = lineNrAttr.toLineNumber(pos);
					int startPos = lineNrAttr.toStartPc(lineNr);

					// store bytecode until invokation
					ArrayList<Integer> invokationBytecodeInterval = getInvocationInterval(
							codeAttr, startPos);

					codeIter.move(pos);
					codeIter.next();

					// String instr = p.getInstruction(behavior, pos);
					// String targetVarClassName = getClassName(codeIter, pos);

					// TODO: find out method receiver
					storeMethodReceiverCombination(behavior,
							invokationBytecodeInterval);
					//
					// ArrayList<String> varData = new ArrayList<>();
					// varData.add("" + testNr);
					// varData.add("Keira" + testNr);
					// varData.add("Tran.Keira" + testNr);
					// varData.add("Human" + testNr);
					// varData.add("ClassUsing" + testNr);
					// varData.add("ClassInstantiated" + testNr);
					// varData.add("MethodName" + testNr);
					// varData.add("MethodSignature" + testNr);
					// // TODO: Add to csv
					// csvCreator.addCsvLine(varData);
					// testNr += 1;
					//
					// System.out.println();
				}
			}
		}
	}

	private ArrayList<Integer> getInvocationInterval(CodeAttribute codeAttr,
			int startPos) throws BadBytecode {
		LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
				.getAttribute(LineNumberAttribute.tag);
		CodeIterator codeIter = codeAttr.iterator();
		ArrayList<Integer> invocationBytecodeInterval = new ArrayList<>();

		codeIter.move(startPos);
		int pos2 = codeIter.next();
		invocationBytecodeInterval.add(pos2);

		pos2 = codeIter.next();

		int lineNr2 = lineNrAttr.toLineNumber(pos2);
		int startPos2 = lineNrAttr.toStartPc(lineNr2);

		while (startPos2 == startPos) {
			invocationBytecodeInterval.add(pos2);
			pos2 = codeIter.next();
			lineNr2 = lineNrAttr.toLineNumber(pos2);
			startPos2 = lineNrAttr.toStartPc(lineNr2);
		}

		int endPos = getIntervalEndPos(invocationBytecodeInterval);
		int op = codeIter.byteAt(endPos);

		while (!isInvoke(op)) {
			invocationBytecodeInterval.remove(endPos);
			endPos = getIntervalEndPos(invocationBytecodeInterval);
		}

		return invocationBytecodeInterval;

	}

	private void storeMethodReceiverCombination(CtBehavior behavior,
			ArrayList<Integer> invocationBytecodeInterval) throws BadBytecode {

		CodeAttribute codeAttr = behavior.getMethodInfo().getCodeAttribute();
		CodeIterator codeIter = codeAttr.iterator();

		int endPos = getIntervalEndPos(invocationBytecodeInterval);
		int startPos = getIntervalStartPos(invocationBytecodeInterval);

		codeIter.move(startPos);
		int pos = startPos;

		// HashMap<Integer, Integer> invocationPcIntervalMap = new HashMap<>();
		ArrayList<Integer> invocationPcList = new ArrayList<>();

		while (codeIter.hasNext() && pos <= endPos) {
			pos = codeIter.next();
			int op = codeIter.byteAt(pos);

			if (isInvoke(op)) {
				int startPos2 = startPos;

				if (pos == endPos) {
					startPos2 = startPos;
				} else if (invocationPcList.size() != 0) {
					int lastAddedInvocationPos = invocationPcList
							.get(invocationPcList.size() - 1);
					int index = invocationBytecodeInterval
							.indexOf(lastAddedInvocationPos);
					startPos2 = invocationBytecodeInterval.get(index + 1);
					// invocationPcIntervalMap.put(pos, startPos2);
				}

				invocationPcList.add(pos);

				int nameAndType = getNameAndType(codeIter, pos);
				String methodInvokationName = getMethodName(nameAndType);
				boolean isSuper = isSuper(behavior, pos);

				// init or super call can't cause NPE, so ignore them
				if (methodInvokationName.equals("<init>") || isSuper)
					continue;
				else {
					String methodInvokationSignature = getSignature(nameAndType);
					int paramAmount = getParameterAmount(methodInvokationSignature);
				}
			}
		}
	}

	private int getIntervalStartPos(
			ArrayList<Integer> invocationBytecodeInterval) {
		return invocationBytecodeInterval.get(0);
	}

	private Integer getIntervalEndPos(
			ArrayList<Integer> invocationBytecodeInterval) {
		return invocationBytecodeInterval
				.get(invocationBytecodeInterval.size() - 1);
	}

	private int getParameterAmount(String methodInvokationSignature) {
		int paramCount = 0;
		int closingBracket = methodInvokationSignature.indexOf(")");
		methodInvokationSignature = methodInvokationSignature.substring(1,
				closingBracket);
		for (int i = 0; i < methodInvokationSignature.length(); i++) {
			char c = methodInvokationSignature.charAt(i);
			// int, byte, long, double, float, char, short, boolean
			if (isParameterType(c) || c == 'L') {
				if (i == 0) {
					paramCount++;
				} else {
					char charBefore = methodInvokationSignature.charAt(i - 1);
					if (isParameterType(charBefore) || charBefore == ';')
						paramCount++;
				}
			}
		}
		return paramCount;

	}

	private boolean isParameterType(char c) {
		return c == 'I' || c == 'B' || c == 'J' || c == 'D' || c == 'F'
				|| c == 'C' || c == 'S' || c == 'Z';
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

	// /**
	// * Returns the class of the target object, which the method is called on.
	// */
	// protected CtClass getCtClass() throws NotFoundException {
	// return this.cc.getClassPool().get(getClassName());
	// }

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

	// /**
	// * Returns the called method.
	// */
	// public CtMethod getMethod() throws NotFoundException {
	// return getCtClass().getMethod(getMethodName(), getSignature());
	// }

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
				&& behavior.getDeclaringClass().getName()
						.equals(getClassName(codeIter, pos));
	}

	/*
	 * Returns the parameter types of the called method.
	 * 
	 * public CtClass[] getParameterTypes() throws NotFoundException { return
	 * Descriptor.getParameterTypes(getMethodDesc(), thisClass.getClassPool());
	 * }
	 */

	/*
	 * Returns the return type of the called method.
	 * 
	 * public CtClass getReturnType() throws NotFoundException { return
	 * Descriptor.getReturnType(getMethodDesc(), thisClass.getClassPool()); }
	 */

}
