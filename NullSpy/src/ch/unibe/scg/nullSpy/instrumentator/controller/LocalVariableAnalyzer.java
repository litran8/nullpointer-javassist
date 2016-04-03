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
import javassist.bytecode.ExceptionTable;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import ch.unibe.scg.nullSpy.model.LocalVar;

/**
 * Instruments test-code after locVars.
 * 
 * @author Lina Tran
 *
 */
public class LocalVariableAnalyzer extends VariableAnalyzer implements Opcode {

	public LocalVariableAnalyzer(CtClass cc) {
		super(cc);
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

			// get everything what is needed for checking locVars in the
			// byte code
			CodeAttribute codeAttribute = method.getMethodInfo()
					.getCodeAttribute();

			// if (codeAttribute != null) {
			CodeIterator codeIterator = codeAttribute.iterator();

			LocalVariableAttribute localVariableTable = (LocalVariableAttribute) codeAttribute
					.getAttribute(LocalVariableAttribute.tag);
			ArrayList<LocalVariableTableEntry> localVariableList = getStableLocalVariableTableAsList(localVariableTable);

			HashMap<Integer, Integer> lineNumberMap = getLineNumberTable(method);
			LineNumberAttribute lineNumberTable = (LineNumberAttribute) codeAttribute
					.getAttribute(LineNumberAttribute.tag);
			ExceptionTable exceptionTable = codeAttribute.getExceptionTable();

			codeIterator.begin();

			// if (method.getName().equals("elementStarted"))
			instrumentAfterLocVarObject(method, codeIterator,
					localVariableList, lineNumberMap, lineNumberTable,
					exceptionTable);

			// calculates the time modified project uses
			addTimeToModifiedProject(method);
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
	 * @param localVariableList
	 * @param lineNumberMap
	 * @param exceptionTable
	 * @throws BadBytecode
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 */
	private void instrumentAfterLocVarObject(CtMethod method,
			CodeIterator codeIterator,
			ArrayList<LocalVariableTableEntry> localVariableList,
			HashMap<Integer, Integer> lineNumberMap,
			LineNumberAttribute lineNumberTable, ExceptionTable exceptionTable)
			throws BadBytecode, CannotCompileException, NotFoundException {

		// store current instruction and the previous instructions
		ArrayList<Integer> instrPositions = new ArrayList<Integer>();

		int methodMaxPc = lineNumberTable
				.startPc(lineNumberTable.tableLength() - 1);

		while (codeIterator.hasNext()) {
			int pos = codeIterator.next();
			instrPositions.add(pos);
			System.out.println(InstructionPrinter.instructionString(
					codeIterator, pos, method.getMethodInfo2().getConstPool()));

			int op = codeIterator.byteAt(pos);

			if (isLocVarObject(op) && pos <= methodMaxPc) {

				System.out.println(cc.getName());
				System.out.println(method.getName());
				System.out.println(javassist.bytecode.InstructionPrinter
						.instructionString(codeIterator, pos, method
								.getMethodInfo2().getConstPool()));

				int localVarTableIndex = getLocVarIndexInLocVarTable(
						codeIterator, localVariableList, pos, "astore.*");
				String localVarName = localVariableList.get(localVarTableIndex).varName;
				int localVarLineNr = getLineNumber(lineNumberMap, pos);
				String localVarType = localVariableList.get(localVarTableIndex).varType;
				int localVarSlot = localVariableList.get(localVarTableIndex).index;

				System.out.println(localVarName);
				System.out.println(localVarLineNr);
				System.out.println();

				LocalVar localVar = new LocalVar(localVarName, localVarLineNr,
						localVarType, pos, codeIterator.next(), method,
						localVarTableIndex, localVarSlot);

				adaptByteCode(localVar, "localVariable_" + localVarSlot);

				// adaptByteCode(method, localVarName, null, localVarLineNr,
				// localVarType, "localVariable_" + localVarSlot, false,
				// codeIterator.next());

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