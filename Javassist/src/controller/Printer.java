package controller;

import java.lang.reflect.InvocationTargetException;

import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.MethodInfo;

public class Printer {

	public Printer() {
	}

	public void printInstrAtPos(CtMethod method, CodeIterator codeIterator,
			int instrCounter) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			NotFoundException, CannotCompileException, BadBytecode {

		ConstPool cpool = method.getMethodInfo2().getConstPool();

		System.out.println(instrCounter
				+ ": "
				+ InstructionPrinter.instructionString(codeIterator,
						instrCounter, cpool));

		int index = codeIterator.u16bitAt(instrCounter + 1);

		System.out.println("MethodRefClassName: "
				+ cpool.getMethodrefClassName(index));
		System.out.println("Method: " + cpool.getMethodrefName(index));
		System.out.println();

	}

	/**
	 * Prints the instructions and the frame states of the given method.
	 */
	public void print(CtMethod method) {
		// System.out.println("\n" + method.getName());
		MethodInfo info = method.getMethodInfo2();
		ConstPool pool = info.getConstPool();
		CodeAttribute code = info.getCodeAttribute();
		if (code == null)
			return;

		CodeIterator iterator = code.iterator();
		while (iterator.hasNext()) {
			int pos;
			try {
				pos = iterator.next();
			} catch (BadBytecode e) {
				throw new RuntimeException(e);
			}

			String instrString = InstructionPrinter.instructionString(iterator,
					pos, pool);
			System.out.println(pos + ": " + instrString);
		}
	}

}