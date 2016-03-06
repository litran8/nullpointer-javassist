package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.HashMap;

import javassist.CtClass;

public class MethodCallAnalyzer extends Analyzer {

	private HashMap<String, Integer> methodCallOnLocVarObject = new HashMap<>();

	public MethodCallAnalyzer(CtClass cc) {
		super(cc);
		// TODO Auto-generated constructor stub
	}

	// private void checkMethodCall(CtMethod method, CodeIterator codeIterator,
	// LocalVariableAttribute locVarTable,
	// HashMap<Integer, Integer> lineNumberMap,
	// LineNumberAttribute lineNumberTable) throws BadBytecode,
	// CannotCompileException {
	//
	// // store current instruction and the previous instructions
	// ArrayList<Integer> instrPositions = new ArrayList<Integer>();
	//
	// int instrCounter = 0;
	// int prevInstrOp = 0;
	//
	// int methodMaxPc = lineNumberTable
	// .startPc(lineNumberTable.tableLength() - 1);
	//
	// while (codeIterator.hasNext()) {
	// int pos = codeIterator.next();
	// instrPositions.add(pos);
	//
	// int op = codeIterator.byteAt(pos);
	//
	// if (instrCounter > 0)
	// prevInstrOp = codeIterator.byteAt(instrPositions
	// .get(instrCounter - 1));
	// instrCounter++;
	//
	// // check if it's NOT a primitive one and the iterator iterates only
	// // through the "direct" byte code of the method
	// String opString = Mnemonic.OPCODE[op].toString();
	// int pos2;
	// ArrayList<Integer> aloadsInInstructionsOfOneLine = new ArrayList<>();
	//
	// if (opString.matches("aload.*")
	// && (!Mnemonic.OPCODE[prevInstrOp].matches("goto.*") && pos <=
	// methodMaxPc)) {
	// aloadsInInstructionsOfOneLine.add(pos);
	// int locVarIndexInLocVarTable = getLocVarIndexInLocVarTable(
	// codeIterator, locVarTable, pos, "aload.*");
	//
	// // store locVar
	// String locVarName = locVarTable
	// .variableName(locVarIndexInLocVarTable);
	//
	// int locVarSourceLineNr = getLineNumber(lineNumberMap, pos);
	//
	// int indexLineNrTable = lineNrTableLine
	// .indexOf(locVarSourceLineNr);
	//
	// pos2 = codeIterator.next();
	// op = codeIterator.byteAt(pos2);
	//
	// ArrayList<Object> methodCallInfoList = isMethodCallNotForStoringToVar(
	// method, codeIterator, lineNrTablePc, op, pos2,
	// aloadsInInstructionsOfOneLine, indexLineNrTable);
	//
	// if ((boolean) methodCallInfoList.get(0)) {
	//
	// }
	//
	// // method.insertAt(locVarSourceLineNr - 1,
	// //
	// "ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.isAlreadyNull( \""
	// // + method.getDeclaringClass().getName()
	// // + "\",\"" + method.getName() + "\","
	// // + locVarName + "," + locVarSourceLineNr + ",\""
	// // + locVarName + "\", \"locVar\");");
	//
	// // pos2 = codeIterator.next();
	// // op = codeIterator.byteAt(pos2);
	// // opString = Mnemonic.OPCODE[op];
	// //
	// // System.out.println("HEEELLLOO");
	// //
	// // if (Mnemonic.OPCODE[op].matches("athrow"))
	// // System.out.println("ERROOOOOOR");
	// //
	// // while (opString.matches(".*load.*")
	// // || opString.matches(".const.")) {
	// // pos2 = codeIterator.next();
	// // op = codeIterator.byteAt(pos2);
	// // opString = Mnemonic.OPCODE[op];
	// // }
	// //
	// // if (opString.matches("invoke.*")) {
	// //
	// // int locVarIndexInLocVarTable = getLocVarIndexInLocVarTable(
	// // codeIterator, locVarTable, pos);
	// //
	// // // store locVar
	// // String locVarName = locVarTable
	// // .variableName(locVarIndexInLocVarTable);
	// //
	// // int locVarSourceLineNr = getLocVarLineNrInSourceCode(
	// // lineNrTablePc, lineNrTableLine, pos);
	// //
	// // method.insertAt(locVarSourceLineNr - 1,
	// //
	// "ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.isAlreadyNull( \""
	// // + method.getDeclaringClass().getName()
	// // + "\",\"" + method.getName() + "\","
	// // + locVarName + "," + locVarSourceLineNr
	// // + ",\"" + locVarName + "\", \"locVar\");");
	// //
	// // System.out.println();
	// // }
	// }
	// }
	// }
	//
	// private ArrayList<Object> isMethodCallNotForStoringToVar(CtMethod method,
	// CodeIterator codeIterator, ArrayList<Integer> lineNrTablePc,
	// int op, int pos2, ArrayList<Integer> aloadsInInstructionsOfOneLine,
	// int indexLineNrTable) throws BadBytecode {
	// ArrayList<Object> resList = new ArrayList<>();
	// int paramAmount = 0;
	// int posOfObjectWithMehodCall;
	//
	// while (!Mnemonic.OPCODE[op].equals("areturn")
	// && pos2 < lineNrTablePc.get(indexLineNrTable + 1)) {
	// boolean b;
	// String check = Mnemonic.OPCODE[op];
	// op = codeIterator.byteAt(pos2);
	// if (Mnemonic.OPCODE[op].matches("aload.*")) {
	// aloadsInInstructionsOfOneLine.add(pos2);
	// }
	//
	// if (Mnemonic.OPCODE[op].matches("invoke.*")) {
	//
	// ConstPool pool = method.getMethodInfo2().getConstPool();
	//
	// String instr = javassist.bytecode.InstructionPrinter
	// .instructionString(codeIterator, pos2, pool);
	// System.out.println(instr);
	//
	// System.out.println("method ref class name: "
	// + pool.getMethodrefClassName(codeIterator
	// .u16bitAt(pos2 + 1)));
	// System.out
	// .println("method ref name: "
	// + pool.getMethodrefName(codeIterator
	// .u16bitAt(pos2 + 1)));
	//
	// String methodParametersAndReturn = pool
	// .getMethodrefType(codeIterator.u16bitAt(pos2 + 1));
	//
	// System.out.println("method para and return type: "
	// + methodParametersAndReturn);
	//
	// String parameters = methodParametersAndReturn.substring(1,
	// methodParametersAndReturn.indexOf(")"));
	//
	// for (int i = 0; i < parameters.length(); i++) {
	// if (parameters.charAt(i) == ';') {
	// paramAmount++;
	// }
	// }
	//
	// if (aloadsInInstructionsOfOneLine.size() > paramAmount) {
	// posOfObjectWithMehodCall = aloadsInInstructionsOfOneLine
	// .get(0);
	// resList.add(posOfObjectWithMehodCall);
	// } else {
	// resList.add(null);
	// }
	//
	// }
	//
	// if (Mnemonic.OPCODE[op].matches("astore.*")) {
	// resList.add(0, false);
	// } else {
	// resList.add(0, true);
	// }
	// pos2 = codeIterator.next();
	//
	// }
	// return resList;
	// }
}
