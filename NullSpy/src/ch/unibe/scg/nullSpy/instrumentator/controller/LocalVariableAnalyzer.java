package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Descriptor;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import ch.unibe.scg.nullSpy.instrumentator.model.LocalVar;
import ch.unibe.scg.nullSpy.instrumentator.model.LocalVarKey;
import ch.unibe.scg.nullSpy.instrumentator.model.Variable;

/**
 * Instruments test-code after locVars.
 * 
 * @author Lina Tran
 *
 */
public class LocalVariableAnalyzer extends VariableAnalyzer implements Opcode {

	private ArrayList<Variable> localVarList;
	private HashMap<LocalVarKey, LocalVar> localVarMap;

	public LocalVariableAnalyzer(CtClass cc, ArrayList<Variable> localVarList,
			HashMap<LocalVarKey, LocalVar> localVarMap) {
		super(cc);
		this.localVarList = localVarList;
		this.localVarMap = localVarMap;
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

		instrumentAfterLocVarObject(cc.getDeclaredBehaviors());
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

		for (CtBehavior behavior : behaviorList) {
			CodeAttribute codeAttr = behavior.getMethodInfo()
					.getCodeAttribute();

			if (codeAttr == null) {
				continue;
			}

			storeParameterData(behavior);

			LocalVariableAttribute localVarAttr = (LocalVariableAttribute) codeAttr
					.getAttribute(LocalVariableAttribute.tag);

			int localVarAttrLength = localVarAttr.tableLength();
			if (localVarAttrLength == 0) {
				continue;
			}

			ArrayList<LocalVarAttrEntry> localVarAttrAsList = getLocalVarAttrAsList(localVarAttr);

			LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
					.getAttribute(LineNumberAttribute.tag);

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

					String instr = InstructionPrinter.instructionString(
							codeIter, pos, codeAttr.getConstPool());

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
					int localVarLineNr = lineNrAttr.toLineNumber(pos);

					if (localVarSlot != instrSlot) {
						varID += instrSlot;
					} else {
						localVarName = localVarAttrAsList
								.get(localVarAttrIndex).varName;
						localVarType = localVarAttrAsList
								.get(localVarAttrIndex).varType;
						varID += localVarSlot;
					}

					// create localVar
					LocalVar localVar = new LocalVar(varID, localVarName,
							localVarLineNr, localVarType, pos, startPos,
							afterPos, cc, behavior, localVarAttrIndex,
							localVarSlot);

					// save localVar into list
					localVarList.add(localVar);

					// hashMap
					localVarMap.put(new LocalVarKey(localVarName, cc.getName(),
							behavior.getName(), behavior.getSignature()),
							localVar);

					// change byteCode
					adaptByteCode(localVar);

					// update codeAttr, codeIter; set codeIter to last
					// checked
					// pos and iterate further
					codeAttr = behavior.getMethodInfo().getCodeAttribute();
					codeIter = codeAttr.iterator();
					codeIter.move(afterPos);

					// update statement for if() and othe stuffs
					methodMaxPc = lineNrAttr
							.startPc(lineNrAttr.tableLength() - 1);
					lineNrAttr = (LineNumberAttribute) codeAttr
							.getAttribute(LineNumberAttribute.tag);
					localVarAttrAsList = getLocalVarAttrAsList(localVarAttr);
				}
			}
		}

	}

	private void storeParameterData(CtBehavior behavior)
			throws NotFoundException, BadBytecode, CannotCompileException {

		CodeAttribute codeAttr = behavior.getMethodInfo().getCodeAttribute();
		LocalVariableAttribute localVarAttr = (LocalVariableAttribute) codeAttr
				.getAttribute(LocalVariableAttribute.tag);
		String behaviorName = behavior.getName();

		String behaviorSignature = behavior.getSignature();
		int behaviorParamAmount = Descriptor.numOfParameters(behaviorSignature);
		if (localVarAttr.tableLength() == 0 || behaviorParamAmount == 0
				|| behaviorName.equals("<clinit>")
				|| behaviorName.contains("$"))
			return;

		boolean isBehaviorStatic = Modifier.isStatic(behavior.getModifiers());

		int startIndex = isBehaviorStatic ? 0 : 1;
		if (!isBehaviorStatic)
			behaviorParamAmount += 1;

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

			// hashMap
			localVarMap.put(
					new LocalVarKey(varName, cc.getName(), behavior.getName(),
							behavior.getSignature()), localVar);

			// change byteCode
			adaptByteCode(localVar);
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

}