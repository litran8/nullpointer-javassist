package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import ch.unibe.scg.nullSpy.instrumentator.model.Variable;

public class MethodCallAnalyzer extends Analyzer {

	private CtClass cc;
	private ArrayList<Variable> fieldInfoList;
	private ArrayList<Variable> localVarInfoList;

	public MethodCallAnalyzer(CtClass cc, ArrayList<Variable> fieldInfoList,
			ArrayList<Variable> localVarInfoList) {
		super(cc);
		this.cc = super.cc;
		this.fieldInfoList = fieldInfoList;
		this.localVarInfoList = localVarInfoList;
	}

	public void checkInvokes() throws BadBytecode, CannotCompileException {

		checkInvokesInBehavior(new CtBehavior[] { cc.getClassInitializer() });
		checkInvokesInBehavior(cc.getDeclaredConstructors());
		checkInvokesInBehavior(cc.getDeclaredMethods());

	}

	private void checkInvokesInBehavior(CtBehavior[] declaredMethods)
			throws BadBytecode, CannotCompileException {

		for (CtBehavior behavior : declaredMethods) {

			if (behavior == null) {
				return;
			}

			// behavior.instrument(new ExprEditor() {
			// public void edit(MethodCall m) throws CannotCompileException {
			// // if (m.getClassName().equals("Point")
			// // && m.getMethodName().equals("move"))
			// // m.replace("{ $1 = 0; $_ = $proceed($$); }");
			// }
			// });
			// new MethodCall(pos, iterator, clazz, minfo);

			MethodInfo mInfo = behavior.getMethodInfo();
			CodeAttribute codeAttr = behavior.getMethodInfo()
					.getCodeAttribute();

			if (codeAttr == null) {
				continue;
			}

			LocalVariableAttribute localVarTable = (LocalVariableAttribute) codeAttr
					.getAttribute(LocalVariableAttribute.tag);

			LineNumberAttribute lineNrTable = (LineNumberAttribute) codeAttr
					.getAttribute(LineNumberAttribute.tag);

			CodeIterator codeIter = codeAttr.iterator();
			codeIter.begin();

			while (codeIter.hasNext()) {
				int pos = codeIter.next();
				int op = codeIter.byteAt(pos);

				Printer p = new Printer();
				p.printMethod(behavior, pos);

				if (isInvokeByteCode(op)) {

					int lineNr = lineNrTable.toLineNumber(pos);
					int startPos = lineNrTable.toStartPc(lineNr);

					// System.out.println();

				}
			}

		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cc == null) ? 0 : cc.hashCode());
		result = prime * result
				+ ((fieldInfoList == null) ? 0 : fieldInfoList.hashCode());
		result = prime
				* result
				+ ((localVarInfoList == null) ? 0 : localVarInfoList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodCallAnalyzer other = (MethodCallAnalyzer) obj;
		if (cc == null) {
			if (other.cc != null)
				return false;
		} else if (!cc.equals(other.cc))
			return false;
		if (fieldInfoList == null) {
			if (other.fieldInfoList != null)
				return false;
		} else if (!fieldInfoList.equals(other.fieldInfoList))
			return false;
		if (localVarInfoList == null) {
			if (other.localVarInfoList != null)
				return false;
		} else if (!localVarInfoList.equals(other.localVarInfoList))
			return false;
		return true;
	}

	private boolean isInvokeByteCode(int op) {
		// TODO Auto-generated method stub
		return op == Opcode.INVOKESTATIC || op == Opcode.INVOKEINTERFACE
				|| op == Opcode.INVOKEVIRTUAL;
	}

	private void checkMethodCall(CtMethod method, CodeIterator codeIterator,
			ArrayList<LocalVariableTableEntry> localVariableTableAsList,
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

			// check if it's NOT a primitive one and the iterator iterates only
			// through the "direct" byte code of the method
			String opString = Mnemonic.OPCODE[op].toString();
			int pos2;
			ArrayList<Integer> aloadsInInstructionsOfOneLine = new ArrayList<>();

			if (opString.matches("aload.*")
					&& (!Mnemonic.OPCODE[prevInstrOp].matches("goto.*") && pos <= methodMaxPc)) {
				aloadsInInstructionsOfOneLine.add(pos);
				int locVarIndexInLocVarTable = getLocalVarTableIndex(
						codeIterator, localVariableTableAsList, pos, "aload.*");

				// store locVar
				// String locVarName = locVarTable
				// .variableName(locVarIndexInLocVarTable);
				String localVariableName = localVariableTableAsList
						.get(locVarIndexInLocVarTable).varName;

				// int locVarSourceLineNr = getLineNumber(lineNumberMap, pos);

				// int indexLineNrTable = lineNrTableLine
				// .indexOf(locVarSourceLineNr);
				//
				// pos2 = codeIterator.next();
				// op = codeIterator.byteAt(pos2);
				//
				// ArrayList<Object> methodCallInfoList =
				// isMethodCallNotForStoringToVar(
				// method, codeIterator, lineNrTablePc, op, pos2,
				// aloadsInInstructionsOfOneLine, indexLineNrTable);
				//
				// if ((boolean) methodCallInfoList.get(0)) {

			}
		}

		// method.insertAt(locVarSourceLineNr - 1,
		//
		// "ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.isAlreadyNull( \""
		// + method.getDeclaringClass().getName()
		// + "\",\"" + method.getName() + "\","
		// + locVarName + "," + locVarSourceLineNr + ",\""
		// + locVarName + "\", \"locVar\");");

		// pos2 = codeIterator.next();
		// op = codeIterator.byteAt(pos2);
		// opString = Mnemonic.OPCODE[op];
		//
		// System.out.println("HEEELLLOO");
		//
		// if (Mnemonic.OPCODE[op].matches("athrow"))
		// System.out.println("ERROOOOOOR");
		//
		// while (opString.matches(".*load.*")
		// || opString.matches(".const.")) {
		// pos2 = codeIterator.next();
		// op = codeIterator.byteAt(pos2);
		// opString = Mnemonic.OPCODE[op];
		// }
		//
		// if (opString.matches("invoke.*")) {
		//
		// int locVarIndexInLocVarTable = getLocVarIndexInLocVarTable(
		// codeIterator, locVarTable, pos);
		//
		// // store locVar
		// String locVarName = locVarTable
		// .variableName(locVarIndexInLocVarTable);
		//
		// int locVarSourceLineNr = getLocVarLineNrInSourceCode(
		// lineNrTablePc, lineNrTableLine, pos);
		//
		// method.insertAt(locVarSourceLineNr - 1,
		//
		// "ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.isAlreadyNull( \""
		// + method.getDeclaringClass().getName()
		// + "\",\"" + method.getName() + "\","
		// + locVarName + "," + locVarSourceLineNr
		// + ",\"" + locVarName + "\", \"locVar\");");
		//
		// System.out.println();
		// }
	}

	private ArrayList<Object> isMethodCallNotForStoringToVar(CtMethod method,
			CodeIterator codeIterator, ArrayList<Integer> lineNrTablePc,
			int op, int pos2, ArrayList<Integer> aloadsInInstructionsOfOneLine,
			int indexLineNrTable) throws BadBytecode {
		ArrayList<Object> resList = new ArrayList<>();
		int paramAmount = 0;
		int posOfObjectWithMehodCall;

		while (!Mnemonic.OPCODE[op].equals("areturn")
				&& pos2 < lineNrTablePc.get(indexLineNrTable + 1)) {
			boolean b;
			String check = Mnemonic.OPCODE[op];
			op = codeIterator.byteAt(pos2);
			if (Mnemonic.OPCODE[op].matches("aload.*")) {
				aloadsInInstructionsOfOneLine.add(pos2);
			}

			if (Mnemonic.OPCODE[op].matches("invoke.*")) {

				ConstPool pool = method.getMethodInfo2().getConstPool();

				String instr = javassist.bytecode.InstructionPrinter
						.instructionString(codeIterator, pos2, pool);
				System.out.println(instr);

				System.out.println("method ref class name: "
						+ pool.getMethodrefClassName(codeIterator
								.u16bitAt(pos2 + 1)));
				System.out
						.println("method ref name: "
								+ pool.getMethodrefName(codeIterator
										.u16bitAt(pos2 + 1)));

				String methodParametersAndReturn = pool
						.getMethodrefType(codeIterator.u16bitAt(pos2 + 1));

				System.out.println("method para and return type: "
						+ methodParametersAndReturn);

				String parameters = methodParametersAndReturn.substring(1,
						methodParametersAndReturn.indexOf(")"));

				for (int i = 0; i < parameters.length(); i++) {
					if (parameters.charAt(i) == ';') {
						paramAmount++;
					}
				}

				if (aloadsInInstructionsOfOneLine.size() > paramAmount) {
					posOfObjectWithMehodCall = aloadsInInstructionsOfOneLine
							.get(0);
					resList.add(posOfObjectWithMehodCall);
				} else {
					resList.add(null);
				}

			}

			if (Mnemonic.OPCODE[op].matches("astore.*")) {
				resList.add(0, false);
			} else {
				resList.add(0, true);
			}
			pos2 = codeIterator.next();

		}
		return resList;
	}
}
