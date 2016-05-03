package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import ch.unibe.scg.nullSpy.model.LocalVar;
import ch.unibe.scg.nullSpy.model.Variable;

/**
 * Instruments test-code after locVars.
 * 
 * @author Lina Tran
 *
 */
public class LocalVariableAnalyzer extends VariableAnalyzer implements Opcode {

	private ArrayList<Variable> localVarList;

	public LocalVariableAnalyzer(CtClass cc, ArrayList<Variable> localVarList) {
		super(cc);
		this.localVarList = localVarList;
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
			try {
				// if (method.getName().equals("initManager")) {
				CodeAttribute codeAttribute = method.getMethodInfo()
						.getCodeAttribute();

				if (codeAttribute == null) {
					continue;
				}

				CodeIterator codeIterator = codeAttribute.iterator();

				LocalVariableAttribute localVariableTable = (LocalVariableAttribute) codeAttribute
						.getAttribute(LocalVariableAttribute.tag);
				int localVarTableLength = localVariableTable.tableLength();

				if (localVarTableLength == 0) {
					continue;
				}

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

						int startPos = getStartPos(method, pos);
						int afterPos = codeIterator.next();

						int localVarTableIndex = getLocalVarTableIndex(
								codeIterator, localVariableList, pos,
								"astore.*");

						// Printer p = new Printer();
						// p.printMethod(method, 0);

						int localVarSlot = localVariableList
								.get(localVarTableIndex).index;

						String instr = InstructionPrinter
								.instructionString(codeIterator, pos,
										codeAttribute.getConstPool());

						if (instr.contains(" ")) {
							instr = instr.substring(instr.indexOf(" ") + 1);
						} else {
							instr = instr.substring(instr.indexOf("_") + 1);
						}

						int instrSlot = Integer.parseInt(instr);

						String localVarName = "";
						String localVarType = "";
						String varID = "localVariable_";

						// lineNr
						int localVarLineNr = lineNumberTable.toLineNumber(pos);

						if (localVarSlot != instrSlot) {
							varID += instrSlot;
						} else {
							localVarName = localVariableList
									.get(localVarTableIndex).varName;
							localVarType = localVariableList
									.get(localVarTableIndex).varType;
							varID += localVarSlot;
						}

						// create localVar
						LocalVar localVar = new LocalVar(varID, localVarName,
								localVarLineNr, localVarType, pos, startPos,
								afterPos, cc, method, localVarTableIndex,
								localVarSlot);

						// save localVar into list
						localVarList.add(localVar);

						// change byteCode
						adaptByteCode(localVar);

						// update codeAttr, codeIter; set codeIter to last
						// checked
						// pos and iterate further
						codeAttribute = method.getMethodInfo()
								.getCodeAttribute();
						codeIterator = codeAttribute.iterator();
						codeIterator.move(afterPos);

						// update statement for if() and othe stuffs
						methodMaxPc = lineNumberTable.startPc(lineNumberTable
								.tableLength() - 1);
						lineNumberTable = (LineNumberAttribute) codeAttribute
								.getAttribute(LineNumberAttribute.tag);
						localVariableList = getStableLocalVariableTableAsList(localVariableTable);

						// Printer p = new Printer();
						// System.out.println("Method: " +
						// method.getName());
						// System.out
						// .println("MethodParams: " +
						// method.getSignature());
						// p.printMethod(method, 0);
						// System.out.println();
					}
				}

				// calculates the time modified project uses
				addTimeToModifiedProject(method);

				// Printer p = new Printer();
				// System.out.println("Method: " + method.getName());
				// System.out.println("MethodParams: " +
				// method.getSignature());
				// p.printMethod(method, 0);
				// System.out.println();

				// }
			} catch (Throwable t) {
				System.out.println();
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
		return Mnemonic.OPCODE[op].matches("astore.*");
		// return Mnemonic.OPCODE[op].matches("a{1,2}store.*");

	}

	private int getStartPos(CtBehavior behavior, int pos) throws BadBytecode {
		int res = 0;

		CodeAttribute codeAttr = behavior.getMethodInfo().getCodeAttribute();
		CodeIterator iter = codeAttr.iterator();

		LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
				.getAttribute(LineNumberAttribute.tag);
		int line = lineNrAttr.toLineNumber(pos);
		res = lineNrAttr.toStartPc(line);

		if (localVarList.size() != 0) {
			Variable lastVar = localVarList.get(localVarList.size() - 1);

			if (isSameBehavior(behavior, lastVar)) {

				iter.move(lastVar.getStorePos());
				iter.next();
				int nextPosAfterLastVar = iter.next();

				if (iter.hasNext() && nextPosAfterLastVar == res) {
					int op = iter.byteAt(res);
					String instr = Mnemonic.OPCODE[op];
					if (instr.matches("ldc.*")) {

						while (iter.hasNext()
								&& !instr.matches("invokestatic.*")) {
							nextPosAfterLastVar = iter.next();
							op = iter.byteAt(nextPosAfterLastVar);
							instr = Mnemonic.OPCODE[op];
						}
						res = iter.next();
					}
				}
			}
		}

		return res;
	}

	private boolean isSameBehavior(CtBehavior currentBehavior, Variable lastVar) {
		boolean inSameBehavior = false;

		CtBehavior lastBehavior = lastVar.getBehavior();

		if (!(lastBehavior == null)) {

			// check class, methodName, methodParams
			inSameBehavior = currentBehavior.getName().equals(
					lastBehavior.getName())
					&& currentBehavior.getDeclaringClass().getName()
							.equals(lastVar.getClassWhereVarIsUsed().getName())
					&& currentBehavior.getSignature().equals(
							lastBehavior.getSignature());
		}
		return inSameBehavior;
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