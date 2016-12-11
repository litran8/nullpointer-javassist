package ch.unibe.scg.nullSpy.instrumentor.controller;

import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Descriptor;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import ch.unibe.scg.nullSpy.instrumentor.model.LocalVar;
import ch.unibe.scg.nullSpy.instrumentor.model.Variable;

/**
 * Looks for local variable assignments, extracts information about the
 * variable, insert check method bytecode after assignment.
 * 
 * @author Lina Tran
 *
 */
public class LocalVariableAnalyzer extends Analyzer implements Opcode {

	private ArrayList<Variable> localVarList = new ArrayList<>();

	public LocalVariableAnalyzer(CtClass cc) {
		super(cc);
	}

	/**
	 * Checks all locVars in a class and instrument test-code after their
	 * assignments.
	 */
	public void instrumentAfterLocVarAssignment() throws BadBytecode,
			CannotCompileException, NotFoundException {
		instrumentAfterLocVarObject(getDeclaredBehaviors());
	}

	/**
	 * Searches only locVar which are objects and directly instrument test-code.
	 */
	private void instrumentAfterLocVarObject(CtBehavior[] behaviorList)
			throws BadBytecode, CannotCompileException, NotFoundException {

		for (CtBehavior behavior : behaviorList) {
			CodeAttribute codeAttr = getCodeAttribute(behavior);

			if (codeAttr == null) {
				continue;
			}

			storeParameterData(behavior);

			LocalVariableAttribute localVarAttr = getLocalVariableAttribute(behavior);

			int localVarAttrLength = localVarAttr.tableLength();
			if (localVarAttrLength == 0) {
				continue;
			}

			ArrayList<LocalVarAttrEntry> localVarAttrAsList = getLocalVarAttrAsList(localVarAttr);

			LineNumberAttribute lineNrAttr = getLineNumberAttribute(behavior);

			CodeIterator codeIter = codeAttr.iterator();
			codeIter.begin();

			ArrayList<Integer> instrPositions = new ArrayList<Integer>();

			int methodMaxPc = lineNrAttr.startPc(lineNrAttr.tableLength() - 1);

			while (codeIter.hasNext()) {

				int pos = codeIter.next();
				instrPositions.add(pos);

				int op = codeIter.byteAt(pos);

				if (isLocVarObject(op) && pos <= methodMaxPc) {

					int startPos = getStartPos(behavior, pos);
					int afterPos = codeIter.next();

					int localVarAttrIndex = getLocalVarAttrIndex(codeIter,
							localVarAttrAsList, pos, "astore.*");
					int localVarSlot = localVarAttrAsList
							.get(localVarAttrIndex).index;

					String localVarName = localVarAttrAsList
							.get(localVarAttrIndex).varName;
					String localVarType = localVarAttrAsList
							.get(localVarAttrIndex).varType;
					String varID = "astore_" + localVarSlot;
					int localVarLineNr = lineNrAttr.toLineNumber(pos);

					// create localVar
					LocalVar localVar = new LocalVar(varID, localVarName,
							localVarLineNr, localVarType, pos, startPos,
							afterPos, cc, behavior, localVarAttrIndex,
							localVarSlot);

					// save localVar into list
					localVarList.add(localVar);

					// change byteCode
					adaptByteCode(localVar);

					// update codeAttr, codeIter;
					// set codeIter to last checked
					// pos and iterate ...
					codeAttr = behavior.getMethodInfo().getCodeAttribute();
					codeIter = codeAttr.iterator();
					codeIter.move(afterPos);

					// update statement for if() and othe stuffs
					methodMaxPc = lineNrAttr
							.startPc(lineNrAttr.tableLength() - 1);
					lineNrAttr = getLineNumberAttribute(codeAttr);
					localVarAttrAsList = getLocalVarAttrAsList(localVarAttr);
				}
			}
		}
	}

	private void storeParameterData(CtBehavior behavior)
			throws NotFoundException, BadBytecode, CannotCompileException {

		LocalVariableAttribute localVarAttr = getLocalVariableAttribute(behavior);

		String behaviorSignature = behavior.getSignature();
		int behaviorParamAmount = Descriptor.numOfParameters(behaviorSignature);
		String behaviorName = behavior.getName();

		if (localVarAttr.tableLength() == 0 || behaviorParamAmount == 0
				|| behaviorName.equals("<clinit>")
				|| behaviorName.contains("$")) {
			return;
		}

		boolean isBehaviorStatic = Modifier.isStatic(behavior.getModifiers());

		if (!isBehaviorStatic) {
			behaviorParamAmount += 1;
		}

		int startIndex = isBehaviorStatic ? 0 : 1;
		for (int i = startIndex; i < behaviorParamAmount; i++) {

			int varSlot = localVarAttr.index(i);
			String varName = localVarAttr.variableName(i);
			String varType = localVarAttr.signature(i);
			if (!(varType.startsWith("[L") || varType.startsWith("L")))
				return;
			String varID = "parameter_" + varSlot;
			int varLineNr = behavior.getMethodInfo().getLineNumber(0);

			// create localVar
			LocalVar localVar = new LocalVar(varID, varName, varLineNr,
					varType, 0, 0, 0, cc, behavior, varSlot, varSlot);

			// save localVar into list
			localVarList.add(localVar);

			// change byteCode
			adaptByteCode(localVar);
		}
	}

	/**
	 * Checks if the locVar is an object.
	 */
	private static boolean isLocVarObject(int op) {
		return Mnemonic.OPCODE[op].matches("astore.*");
	}

	private int getStartPos(CtBehavior behavior, int pos) throws BadBytecode {

		CodeIterator iter = getCodeIterator(behavior);
		LineNumberAttribute lineNrAttr = getLineNumberAttribute(behavior);

		int line = lineNrAttr.toLineNumber(pos);
		int res = lineNrAttr.toStartPc(line);

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

}