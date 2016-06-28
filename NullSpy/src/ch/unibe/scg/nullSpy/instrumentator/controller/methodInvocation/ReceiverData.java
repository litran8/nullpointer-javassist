package ch.unibe.scg.nullSpy.instrumentator.controller.methodInvocation;

import java.io.IOException;
import java.util.ArrayList;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;

public class ReceiverData {

	private CtBehavior behavior;

	public ReceiverData(CtBehavior behavior) {
		this.behavior = behavior;
	}

	public ArrayList<String> getFieldData(ArrayList<String> varData,
			int lineNr, int pos, int count) {

		MethodInfo methodInfo = this.behavior.getMethodInfo2();
		ConstPool constPool = methodInfo.getConstPool();
		CodeAttribute codeAttr = methodInfo.getCodeAttribute();
		CodeIterator codeIter = codeAttr.iterator();

		String varID = "field";
		int index = codeIter.u16bitAt(pos + 1);
		String varName = constPool.getFieldrefName(index);
		String varSign = constPool.getFieldrefType(index);

		String isStatic;
		int op = codeIter.byteAt(pos);
		if (op == Opcode.GETFIELD)
			isStatic = Boolean.toString(true);
		else
			isStatic = Boolean.toString(false);

		String className = this.behavior.getDeclaringClass().getName();
		String declaringClassName = constPool.getFieldrefClassName(index);

		// #9
		varData.add(Integer.toString(count));
		varData.add(Integer.toString(lineNr));
		varData.add(varID);
		varData.add(varName);
		varData.add(varSign);
		varData.add(isStatic);
		varData.add(className);
		varData.add(this.behavior.getName());
		varData.add(this.behavior.getSignature());

		// localVar additional #1
		varData.add("");

		// field #1
		varData.add(declaringClassName);

		return varData;
	}

	public ArrayList<String> getLocalVarData(ArrayList<String> varData,
			int lineNr, int pos, int count) throws IOException, BadBytecode {
		CodeAttribute codeAttr = this.behavior.getMethodInfo()
				.getCodeAttribute();
		CodeIterator codeIter = codeAttr.iterator();
		int slot = 0;
		int op = codeIter.byteAt(pos);
		String opString = Mnemonic.OPCODE[op];

		if (opString.matches("aload_.*")) {
			slot = Integer
					.parseInt(opString.substring(opString.indexOf("_") + 1));
		} else {
			slot = codeIter.u16bitAt(pos) - 6400;
		}

		LocalVariableAttribute localVarAttr = (LocalVariableAttribute) codeAttr
				.getAttribute(LocalVariableAttribute.tag);
		for (int i = 0; i < localVarAttr.tableLength(); i++) {
			if (localVarAttr.index(i) == slot) {
				int startPc = localVarAttr.startPc(i);
				int length = localVarAttr.codeLength(i);
				int endPc = startPc + length;
				if (pos >= startPc && pos <= endPc) {
					String varID = "aload_" + slot;
					String varName = localVarAttr.variableName(i);
					String varSign = localVarAttr.signature(i);
					String className = this.behavior.getDeclaringClass()
							.getName();
					String isStatic = Boolean.toString(false);

					// System.out.println("Nr.: " + count + ", VarName: "
					// + varName);

					// #9
					varData.add(Integer.toString(count));
					varData.add(Integer.toString(lineNr));
					varData.add(varID);
					varData.add(varName);
					varData.add(varSign);
					varData.add(isStatic);
					varData.add(className);
					varData.add(this.behavior.getName());
					varData.add(this.behavior.getSignature());

					// localVar additional #1
					varData.add(Integer.toString(i));

					// field #1
					varData.add("");
				}
			}
		}
		return varData;
	}

}
