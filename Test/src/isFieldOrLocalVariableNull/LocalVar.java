package isFieldOrLocalVariableNull;

import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;

public class LocalVar {

	private CtClass cc;

	public LocalVar(CtClass cc) {
		this.cc = cc;
	}

	/*
	 * Goes through byte code of a method and matches some regular expressions
	 * to check if a local variable is directly set to null
	 */
	public void isLocalVarNull(CtMethod m) throws NotFoundException,
			BadBytecode, CannotCompileException {

		MethodInfo minfo = this.cc.getDeclaredMethod(m.getName())
				.getMethodInfo();
		CodeAttribute ca = minfo.getCodeAttribute();

		CodeIterator ci = ca.iterator();
		ci.begin();

		ArrayList<Integer> instrIndex = new ArrayList<Integer>();
		ArrayList<Integer> localVariableLineNumbers = new ArrayList<Integer>();
		int instrCounter = 0;
		int instrBeforeOp = 0;

		while (ci.hasNext()) {
			int index = ci.next();
			instrIndex.add(index);

			int op = ci.byteAt(index);

			if (instrCounter > 0)
				instrBeforeOp = ci.byteAt(instrIndex.get(instrCounter - 1));
			instrCounter++;

			if (Mnemonic.OPCODE[op].matches(".store_.")
					&& Mnemonic.OPCODE[instrBeforeOp].matches("aconst_null")) {
				localVariableLineNumbers.add(minfo.getLineNumber(index));
			}
		}

		for (int i = 0; i < localVariableLineNumbers.size(); i++) {
			int insertLineNumber = localVariableLineNumbers.get(i);
			String nullLink = setNullLink(insertLineNumber);
			m.insertAt(insertLineNumber,
					"System.out.println(\"Local var in line "
							+ insertLineNumber + " is null: (" + nullLink
							+ ")\");");
		}
	}

	private String setNullLink(int insertLineNumber) {
		return this.cc.getName() + ".java:" + insertLineNumber;
	}
}
