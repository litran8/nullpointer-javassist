package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;

/**
 * Instruments test-code after locVars.
 * 
 * @author Lina Tran
 *
 */
public class LocVarLogic extends VariableAnalyzer implements Opcode {

	private CtClass cc;

	// private FieldAndLocVarContainerOfOneClass container;

	// public LocVarLogic(CtClass cc,
	// FieldAndLocVarContainerOfOneClass fieldLocVarContainer) {
	// this.cc = cc;
	// this.container = fieldLocVarContainer;
	// }

	public LocVarLogic(CtClass cc) {
		super(cc);
		this.cc = cc;
	}

	/**
	 * Checks all locVars in a class and instrument test-code after their
	 * assignments.
	 * 
	 * @throws BadBytecode
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 */
	public void instrumentAfterLocVarAssignment() throws BadBytecode,
			CannotCompileException, NotFoundException {

		for (CtMethod method : cc.getDeclaredMethods()) {

			// calculates the time modified project uses
			addTimeToModifiedProject(method);

			// get everything what is needed for checking locVars in the
			// byte code
			CodeAttribute codeAttribute = method.getMethodInfo()
					.getCodeAttribute();
			CodeIterator codeIterator = codeAttribute.iterator();

			LocalVariableAttribute locVarTable = (LocalVariableAttribute) codeAttribute
					.getAttribute(javassist.bytecode.LocalVariableAttribute.tag);

			HashMap<Integer, Integer> lineNumberMap = getLineNumberTable(method);
			LineNumberAttribute lineNrTable = (LineNumberAttribute) codeAttribute
					.getAttribute(LineNumberAttribute.tag);

			// if (method.getName().equals("setToNullMethod")) {

			codeIterator.begin();

			instrumentAfterLocVarObject(method, codeIterator, locVarTable,
					lineNumberMap, lineNrTable);

			// codeIterator.begin();
			// checkMethodCall(method, codeIterator, locVarTable, lineNrTablePc,
			// lineNrTableLine);
			// }

		}
	}

	private void addTimeToModifiedProject(CtMethod method)
			throws CannotCompileException {
		if (method.getName().equals("main")) {
			CtField f = CtField.make("public static long startTime;", cc);
			cc.addField(f);
			method.insertBefore("startTime = System.nanoTime();");
			method.insertAfter("System.out.println(\"\\nOriginal class time: \" +((System.nanoTime() - startTime)/1000000) + \" ms\");");

			// CtClass etype = ClassPool.getDefault().get(
			// "java.io.IOException");
			// method.addCatch("{ System.out.println($e); throw $e; }",
			// etype);
		}
	}

	/**
	 * Searches only locVar which are objects and directly instrument test-code.
	 * 
	 * @param method
	 * @param codeIterator
	 * @param locVarTable
	 * @param lineNumberMap
	 * @throws BadBytecode
	 * @throws CannotCompileException
	 */
	private void instrumentAfterLocVarObject(CtMethod method,
			CodeIterator codeIterator, LocalVariableAttribute locVarTable,
			HashMap<Integer, Integer> lineNumberMap,
			LineNumberAttribute lineNumberTable) throws BadBytecode,
			CannotCompileException {

		// store current instruction and the previous instructions
		ArrayList<Integer> instrPositions = new ArrayList<Integer>();

		int instrCounter = 0;
		int prevInstrOp = 0;

		int methodMaxPc = lineNumberTable
				.startPc(lineNumberTable.tableLength() - 1);

		while (codeIterator.hasNext()) {
			int pos = codeIterator.next();
			instrPositions.add(pos);

			int op = codeIterator.byteAt(pos);

			if (instrCounter > 0)
				prevInstrOp = codeIterator.byteAt(instrPositions
						.get(instrCounter - 1));
			instrCounter++;

			if (isLocVarObject(op)
					&& (!Mnemonic.OPCODE[prevInstrOp].matches("goto.*") && pos <= methodMaxPc)) {

				int locVarIndexInLocVarTable = getLocVarIndexInLocVarTable(
						codeIterator, locVarTable, pos, "astore.*");

				// store locVar
				String localVariableName = locVarTable
						.variableName(locVarIndexInLocVarTable);

				// int locVarSourceLineNr = getLocVarLineNrInSourceCode(
				// lineNrTablePc, lineNrTableLine, pos);
				int localVariableLineNumber = getLineNumber(lineNumberMap, pos);

				// insertTestLineForLocalVariableAssignment(method, locVarName,
				// locVarSourceLineNr);
				adaptByteCode(method, localVariableName,
						localVariableLineNumber, "localVariable",
						"localVariable");
				// container.storeLocVarInfo(locVarName, locVarSourceLineNr,
				// method, locVarIndexInLocVarTable);
			}
		}
	}

	/**
	 * Checks if the locVar is an object, NOT a primitive one.
	 * 
	 * @param op
	 * @return
	 */
	private static boolean isLocVarObject(int op) {
		return Mnemonic.OPCODE[op].matches("a{1,2}store.*");
	}

}