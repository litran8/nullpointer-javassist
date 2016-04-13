package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import javassist.expr.FieldAccess;
import ch.unibe.scg.nullSpy.model.LocalVar;
import ch.unibe.scg.nullSpy.model.Variable;

/**
 * Instruments test-code after locVars.
 * 
 * @author Lina Tran
 *
 */
public class LocalVariableAnalyzer extends VariableAnalyzer implements Opcode {

	private ArrayList<Variable> localVarList = new ArrayList<>();

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

		// if (method.getName().equals("elementStarted"))
		instrumentAfterLocVarObject(cc.getDeclaredConstructors());
		instrumentAfterLocVarObject(cc.getDeclaredMethods());
		// }
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
	private void instrumentAfterLocVarObject(CtBehavior[] behaviorList)
			throws BadBytecode, CannotCompileException, NotFoundException {

		for (CtBehavior method : behaviorList) {

			CodeAttribute codeAttribute = method.getMethodInfo()
					.getCodeAttribute();

			// if (codeAttribute != null) {
			CodeIterator codeIterator = codeAttribute.iterator();

			LocalVariableAttribute localVariableTable = (LocalVariableAttribute) codeAttribute
					.getAttribute(LocalVariableAttribute.tag);
			ArrayList<LocalVariableTableEntry> localVariableList = getStableLocalVariableTableAsList(localVariableTable);

			LineNumberAttribute lineNumberTable = (LineNumberAttribute) codeAttribute
					.getAttribute(LineNumberAttribute.tag);

			codeIterator.begin();

			ArrayList<Integer> instrPositions = new ArrayList<Integer>();

			int methodMaxPc = lineNumberTable.startPc(lineNumberTable
					.tableLength() - 1);

			while (codeIterator.hasNext()) {

				int pos = codeIterator.next();
				instrPositions.add(pos);

				int op = codeIterator.byteAt(pos);

				if (isLocVarObject(op) && pos <= methodMaxPc) {

					int localVarTableIndex = getLocalVarTableIndex(
							codeIterator, localVariableList, pos, "astore.*");

					String localVarName = localVariableList
							.get(localVarTableIndex).varName;
					int localVarLineNr = lineNumberTable.toLineNumber(pos);

					int startPos = localVariableList.get(localVarTableIndex).startPc; // MUST
																						// BE
																						// CHANGED
																						// !!!
					int afterPos = codeIterator.next();

					String localVarType = localVariableList
							.get(localVarTableIndex).varType;

					int localVarSlot = localVariableList
							.get(localVarTableIndex).index;
					String varID = "localVariable_" + localVarSlot;

					LocalVar localVar = new LocalVar(varID, localVarName,
							localVarLineNr, localVarType, pos, startPos,
							afterPos, cc, method, localVarTableIndex,
							localVarSlot);

					localVarList.add(localVar);

					adaptByteCode(localVar);

					// update codeAttr, codeIter; set codeIter to last checked
					// pos and iterate further
					codeAttribute = method.getMethodInfo().getCodeAttribute();
					codeIterator = codeAttribute.iterator();
					codeIterator.move(afterPos);

					// update statement for if() and othe stuffs
					methodMaxPc = lineNumberTable.startPc(lineNumberTable
							.tableLength() - 1);
					lineNumberTable = (LineNumberAttribute) codeAttribute
							.getAttribute(LineNumberAttribute.tag);
					localVariableList = getStableLocalVariableTableAsList(localVariableTable);

				}
			}

			// calculates the time modified project uses
			addTimeToModifiedProject(method);
			// }
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

	private int getStartPos(FieldAccess field, int pos) throws BadBytecode {
		int res = 0;
		CtBehavior behavior = field.where();
		CodeIterator iter = behavior.getMethodInfo().getCodeAttribute()
				.iterator();
		HashMap<Integer, Integer> lineNrMap = getLineNumberMap(behavior);

		Object[] keys = lineNrMap.keySet().toArray();
		Arrays.sort(keys);

		for (int i = 0; i < keys.length; i++) {
			if ((int) keys[i] > pos) {
				res = (int) keys[i - 1];

				// if (fieldIsWritterInfoList.size() != 0) {
				// Variable lastVar = fieldIsWritterInfoList
				// .get(fieldIsWritterInfoList.size() - 1);
				//
				// if (isSameBehavior(field, lastVar)) {
				//
				// iter.move(lastVar.getStorePos());
				// iter.next();
				// int nextPosAfterLastVar = iter.next();
				//
				// if (iter.hasNext() && nextPosAfterLastVar == res) {
				// int op = iter.byteAt(res);
				// String instr = Mnemonic.OPCODE[op];
				// if (instr.matches("ldc.*")) {
				//
				// while (iter.hasNext()
				// && !instr.matches("invokestatic.*")) {
				// nextPosAfterLastVar = iter.next();
				// op = iter.byteAt(nextPosAfterLastVar);
				// instr = Mnemonic.OPCODE[op];
				// }
				// res = iter.next();
				// }
				// }
				// }
				// }
				//
				// return res;
			}
		}

		return res;
	}

	private void addTimeToModifiedProject(CtBehavior method)
			throws CannotCompileException {
		if (method.getName().equals("main")) {
			CtField f = CtField.make("public static long startTime;", cc);
			cc.addField(f);
			method.insertBefore("startTime = System.nanoTime();");
			method.insertAfter("System.out.println(\"\\nModified class time: \" +((System.nanoTime() - startTime)/1000000) + \" ms\");");

			// CtClass etype = ClassPool.getDefault().get(
			// "java.io.IOException");
			// method.addCatch("{ System.out.println($e); throw $e; }",
			// etype);
		}
	}

}