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
					int nameAndType = getNameAndType(codeIter, pos);

					String methodInvokationName = getMethodName(nameAndType);
					boolean isSuper = isSuper(behavior, pos);

					// init or super call can't cause NPE, so ignore them
					if (methodInvokationName.equals("<init>") || isSuper)
						continue;

					int lineNr = lineNrAttr.toLineNumber(pos);
					int startPos = lineNrAttr.toStartPc(lineNr);
					ArrayList<String> varData = new ArrayList<>();
					varData.add("" + testNr);
					varData.add("Keira" + testNr);
					varData.add("Tran.Keira" + testNr);
					varData.add("Human" + testNr);
					varData.add("ClassUsing" + testNr);
					varData.add("ClassInstantiated" + testNr);
					varData.add("MethodName" + testNr);
					varData.add("MethodSignature" + testNr);
					// TODO: Add to csv
					csvCreator.addCsvLine(varData);
					testNr += 1;
					// store
					codeIter.move(startPos);
					ArrayList<Integer> instrList = new ArrayList<>();
					int pos2 = codeIter.next();
					instrList.add(pos2);
					pos2 = codeIter.next();

					while (pos2 <= pos) {
						instrList.add(pos2);
						pos2 = codeIter.next();
					}

					String instr = p.getInstruction(behavior, pos);

					String targetVarClassName = getClassName(codeIter, pos);

					// TODO: find out reference on which method is called

					String methodInvokationSignature = getSignature(nameAndType);
					int paramAmount = getParameterAmount(methodInvokationSignature);
					System.out.println();
				}
			}
		}
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
				|| op == Opcode.INVOKEVIRTUAL;
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
