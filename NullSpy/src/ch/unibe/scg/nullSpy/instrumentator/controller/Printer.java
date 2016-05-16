package ch.unibe.scg.nullSpy.instrumentator.controller;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.InstructionPrinter;

public class Printer {

	public void printInstruction(CtBehavior behavior, int pos)
			throws BadBytecode {
		CodeAttribute ca = behavior.getMethodInfo().getCodeAttribute();
		CodeIterator iter = ca.iterator();
		ConstPool pool = behavior.getMethodInfo2().getConstPool();
		iter.move(pos);
		while (iter.hasNext()) {
			int pos2 = iter.next();
			System.out.println("" + pos2 + ": "
					+ InstructionPrinter.instructionString(iter, pos2, pool));

		}
	}

	public String getInstruction(CtBehavior behavior, int pos)
			throws BadBytecode {
		CodeAttribute ca = behavior.getMethodInfo().getCodeAttribute();
		CodeIterator iter = ca.iterator();
		ConstPool pool = behavior.getMethodInfo2().getConstPool();

		return InstructionPrinter.instructionString(iter, pos, pool);
	}
}
