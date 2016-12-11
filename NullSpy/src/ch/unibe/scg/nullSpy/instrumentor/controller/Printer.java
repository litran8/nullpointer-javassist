package ch.unibe.scg.nullSpy.instrumentor.controller;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.LineNumberAttribute;

public class Printer {

	public static void printBehavior(CtBehavior behavior, int pos)
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

	public static String printInstruction(CtBehavior behavior, int pos)
			throws BadBytecode {
		CodeAttribute ca = behavior.getMethodInfo().getCodeAttribute();
		CodeIterator iter = ca.iterator();
		ConstPool pool = behavior.getMethodInfo2().getConstPool();

		return InstructionPrinter.instructionString(iter, pos, pool);
	}

	public static void printLineNumberAttribute(LineNumberAttribute lineNrAttr) {
		for (int i = 0; i < lineNrAttr.tableLength(); i++) {
			System.out.println("Pc: " + lineNrAttr.startPc(i) + ", LineNr: "
					+ lineNrAttr.lineNumber(i));
		}
	}
}
