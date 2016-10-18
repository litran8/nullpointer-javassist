package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import ch.unibe.scg.nullSpy.instrumentator.controller.methodInvocation.MethodInvocationAnalyzer;
import ch.unibe.scg.nullSpy.instrumentator.controller.methodInvocation.ReceiverData;
import ch.unibe.scg.nullSpy.instrumentator.model.Field;
import ch.unibe.scg.nullSpy.instrumentator.model.FieldKey;
import ch.unibe.scg.nullSpy.instrumentator.model.IndirectVar;
import ch.unibe.scg.nullSpy.instrumentator.model.Variable;

/**
 * Instruments test-code after fields.
 */
public class FieldAnalyzer extends VariableAnalyzer {

	private ArrayList<Variable> fieldIsWriterList;
	private HashMap<FieldKey, Field> fieldMap;

	// private ArrayList<Variable> fieldIsReaderList;

	public FieldAnalyzer(CtClass cc, ArrayList<Variable> fieldIsWriterList,
			HashMap<FieldKey, Field> fieldMap) {
		super(cc);
		this.fieldIsWriterList = fieldIsWriterList;
		this.fieldMap = fieldMap;
		// this.fieldIsReaderList = new ArrayList<>();
	}

	/**
	 * Search all fields and store them in an arrayList. It instruments code
	 * after each field assignments.
	 * 
	 * @param this.cc
	 * @param myClass
	 * @throws CannotCompileException
	 * @throws BadBytecode
	 * @throws NotFoundException
	 */
	public void instrumentAfterFieldAssignment() throws CannotCompileException,
			BadBytecode, NotFoundException {

		// if (!cc.getName().equals("org.jhotdraw.application.DrawApplication"))
		// return;

		checkForFieldIsReader();
		checkForFielIsWriter();
	}

	private void checkForFieldIsReader() throws CannotCompileException {
		cc.instrument(new ExprEditor() {
			public void edit(FieldAccess field) throws CannotCompileException {
				CtBehavior behavior = field.where();

				// if (!behavior.getName().equals("name"))
				// return;

				try {
					if (!isFieldPrimitive(field) && field.isReader()
							&& !isInnerClassSuperCall(field)) {
						storeInfoAboutFieldIsReader(field);
					}
				} catch (BadBytecode | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	private void storeInfoAboutFieldIsReader(FieldAccess field)
			throws BadBytecode, IOException {

		CtBehavior behavior = field.where();
		// System.out.println(behavior.getName());
		CodeAttribute codeAttr = behavior.getMethodInfo2().getCodeAttribute();
		LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
				.getAttribute(LineNumberAttribute.tag);

		int pc = field.indexOfBytecode();

		int startPc = pc;
		if (!field.isStatic()) {
			startPc = getStartPc(codeAttr, pc);
		}

		// startPc would be aaload.*
		if (startPc == -1) {
			return;
		}

		int lineNr = lineNrAttr.toLineNumber(pc);

		ArrayList<String> varData = new ArrayList<>();
		ReceiverData receiverData = new ReceiverData(field.where());

		varData = receiverData.getFieldData(lineNr, pc,
				MethodInvocationAnalyzer.getCounter());

		MethodInvocationAnalyzer methodInvocationAnalyzer = new MethodInvocationAnalyzer(
				this.cc);
		methodInvocationAnalyzer.storeMethodreceiverData(behavior, startPc);
	}

	private int getStartPc(CodeAttribute codeAttr, int pc) {
		CodeIterator codeIter = codeAttr.iterator();

		ArrayList<Integer> pcListUntilParameterPc = getPcListUntilParameterPc(
				codeIter, pc);

		int op = 0;
		int checkPc = 0;
		for (int i = pcListUntilParameterPc.size() - 2; i >= 0; i--) {
			checkPc = pcListUntilParameterPc.get(i);
			op = codeIter.byteAt(checkPc);
			if (Mnemonic.OPCODE[op].matches("aload.*")) {
				return checkPc;
			}
		}

		return -1;
	}

	private ArrayList<Integer> getPcListUntilParameterPc(CodeIterator codeIter,
			int pc) {
		ArrayList<Integer> res = new ArrayList<>();

		codeIter.begin();
		int checkPc = 0;

		while (codeIter.hasNext() && checkPc < pc) {
			try {
				checkPc = codeIter.next();
				res.add(checkPc);

			} catch (BadBytecode e) {
				e.printStackTrace();
			}
		}

		return res;
	}

	private void checkForFielIsWriter() throws CannotCompileException {
		cc.instrument(new ExprEditor() {
			public void edit(FieldAccess field) throws CannotCompileException {

				CtBehavior behavior = field.where();
				// if (!behavior.getName().equals("basicDisplayBox"))
				// return;

				CodeAttribute codeAttr = behavior.getMethodInfo()
						.getCodeAttribute();
				if (codeAttr == null)
					return;

				LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
						.getAttribute(LineNumberAttribute.tag);
				if (lineNrAttr == null) {
					return;
				}

				Variable var = null;
				try {
					boolean isWriter = field.isWriter();
					boolean isPrimitive = !isFieldPrimitive(field);
					boolean isSuperCall = !isInnerClassSuperCall(field);
					if (field.isWriter() && !isFieldPrimitive(field)
							&& !isInnerClassSuperCall(field)) {

						// fieldType is an object -> starts with L.*
						if (isFieldFromCurrentCtClass(field)) {
							// direct fields
							var = storeFieldOfCurrentClass(field);
						} else {
							// indirect fields
							var = storeFieldOfAnotherClass(field);
						}
						adaptByteCode(var);
					}

				} catch (NotFoundException | BadBytecode e) {
					e.printStackTrace();
				}
			}

		});
	}

	private boolean isAnotherClassAnInnerClass(FieldAccess field)
			throws NotFoundException {
		for (CtClass c : cc.getNestedClasses()) {
			if (c.getName().equals(field.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If the field is of the current analyzed class.
	 * 
	 * @param field
	 * @throws NotFoundException
	 * @throws BadBytecode
	 */
	private Variable storeFieldOfCurrentClass(FieldAccess field)
			throws NotFoundException, BadBytecode {

		CtBehavior behavior = field.where();

		CodeAttribute codeAttribute = behavior.getMethodInfo()
				.getCodeAttribute();
		CodeIterator codeIterator = codeAttribute.iterator();

		int pos = getPos(field);
		int startPos = getStartPos(field, pos);
		codeIterator.move(pos);
		codeIterator.next();

		int posAfterAssignment = 0;
		if (codeIterator.hasNext()) {
			posAfterAssignment = codeIterator.next();
		}

		LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttribute
				.getAttribute(LineNumberAttribute.tag);
		// Printer p = new Printer();
		// p.printLineNumberAttribute(lineNrAttr);

		int fieldLineNr = lineNrAttr.toLineNumber(pos);
		String fieldName = field.getFieldName();
		String fieldType = field.getSignature();
		String fieldDeclaringClassName = cc.getName();

		Field var = new Field("field", fieldName, fieldType,
				fieldDeclaringClassName, fieldLineNr, pos, startPos,
				posAfterAssignment, cc, behavior, field.isStatic(), null);
		fieldIsWriterList.add(var);

		FieldKey fieldKey = new FieldKey(cc.getName(), fieldName, fieldType,
				fieldDeclaringClassName, field.isStatic(), "", "", "", false,
				behavior.getName(), behavior.getSignature());

		fieldMap.put(fieldKey, var);

		return var;
	}

	/**
	 * If the field is of another class than the current analyzed one. E.g. p.a:
	 * p->PersonClass
	 * 
	 * @param field
	 * @throws NotFoundException
	 * @throws BadBytecode
	 */
	private Variable storeFieldOfAnotherClass(FieldAccess field)
			throws NotFoundException, BadBytecode {

		int pos = getPos(field);
		int startPos = getStartPos(field, pos);

		CtBehavior behavior = field.where();
		CodeAttribute codeAttribute = behavior.getMethodInfo()
				.getCodeAttribute();
		CodeIterator codeIterator = codeAttribute.iterator();

		int startPosOp = codeIterator.byteAt(startPos);
		codeIterator.move(startPos);
		codeIterator.next();

		while (startPosOp == Opcode.NOP) {
			startPos = codeIterator.next();
			startPosOp = codeIterator.byteAt(startPos);
		}

		codeIterator.move(pos);
		codeIterator.next();

		int afterPos = 0;
		if (codeIterator.hasNext()) {
			afterPos = codeIterator.next();
		}

		LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttribute
				.getAttribute(LineNumberAttribute.tag);
		int fieldLineNr = lineNrAttr.toLineNumber(pos);

		String fieldName = field.getFieldName();

		// object_FIELD
		int op = codeIterator.byteAt(startPos);
		String indirectVarOpCode = Mnemonic.OPCODE[op];

		String indirectVarName = "";
		String indirectVarType = "";
		String indirectVarDeclaringClassName = "";
		boolean isIndirectVarStatic = false;

		if (!field.isStatic()) {

			if (Mnemonic.OPCODE[op].matches("aload.*")) {
				// localVar.field
				// store locVar e.g. p.a -> get p
				int localVarAttrIndex = 0;

				LocalVariableAttribute localVarAttr = (LocalVariableAttribute) codeAttribute
						.getAttribute(LocalVariableAttribute.tag);
				ArrayList<LocalVarAttrEntry> localVarAttrAsList = getLocalVarAttrAsList(localVarAttr);

				localVarAttrIndex = getLocalVarAttrIndex(codeIterator,
						localVarAttrAsList, startPos, "aload.*");
				int locVarSlot = getLocVarArraySlot(codeIterator, startPos);
				indirectVarOpCode = "aload_" + locVarSlot;

				// localVarName.field
				indirectVarName = localVarAttrAsList.get(localVarAttrIndex).varName;
				indirectVarType = localVarAttrAsList.get(localVarAttrIndex).varType;

			} else {
				// field.field
				String instruction = InstructionPrinter.instructionString(
						codeIterator, startPos, field.where().getMethodInfo2()
								.getConstPool());
				int brace = instruction.indexOf("(");

				instruction = instruction.substring(
						instruction.lastIndexOf(".") + 1, brace);
				indirectVarName = instruction;

				CtField ctField_field = cc.getField(indirectVarName);

				indirectVarDeclaringClassName = ctField_field
						.getDeclaringClass().getName();
				indirectVarType = ctField_field.getSignature();
				isIndirectVarStatic = op == Opcode.GETSTATIC;
			}
		}

		// innerclass
		String innerClassFieldName = "";

		if (isAnotherClassAnInnerClass(field) && indirectVarName.equals("this")) {
			// innerClass: this.innerClassField.field
			codeIterator.move(startPos);
			codeIterator.next();
			int innerClassGetFieldPos = codeIterator.next();
			op = codeIterator.byteAt(innerClassGetFieldPos);
			indirectVarOpCode = Mnemonic.OPCODE[op];
			int index = codeIterator.u16bitAt(innerClassGetFieldPos + 1);

			// innerClassField_field
			innerClassFieldName = behavior.getMethodInfo2().getConstPool()
					.getFieldrefName(index);

			CtField indirectVar = cc.getField(innerClassFieldName);
			indirectVarName = indirectVar.getName();
			indirectVarDeclaringClassName = indirectVar.getDeclaringClass()
					.getName();
			indirectVarType = indirectVar.getSignature();
			isIndirectVarStatic = op == Opcode.GETSTATIC;
		}

		if (indirectVarName.startsWith("[")) {
			indirectVarName = indirectVarName.substring(1);
		}

		// OBJECT_field
		IndirectVar indirectFieldObject = null;

		if (!field.isStatic()) {
			indirectFieldObject = new IndirectVar(indirectVarName,
					indirectVarType, indirectVarDeclaringClassName,
					isIndirectVarStatic, indirectVarOpCode);
		}

		String fieldType = field.getSignature();
		String fieldDeclaringClassName = field.getClassName();

		Field var = new Field("field", fieldName, fieldType,
				fieldDeclaringClassName, fieldLineNr, pos, startPos, afterPos,
				cc, behavior, field.isStatic(), indirectFieldObject);

		fieldIsWriterList.add(var);

		// hashMap

		FieldKey fieldKey = getFieldKey(field, behavior, fieldName,
				indirectVarName, indirectVarType,
				indirectVarDeclaringClassName, fieldType,
				fieldDeclaringClassName);

		fieldMap.put(fieldKey, var);

		return var;
	}

	private FieldKey getFieldKey(FieldAccess field, CtBehavior behavior,
			String fieldName, String indirectVarName, String indirectVarType,
			String indirectVarDeclaringClassName, String fieldType,
			String fieldDeclaringClassName) {
		boolean isIndirectVarStatic;
		FieldKey fieldKey = new FieldKey(cc.getName(), fieldName, fieldType,
				fieldDeclaringClassName, field.isStatic(), "", "", "", false,
				behavior.getName(), behavior.getSignature());

		if (!field.isStatic() && !indirectVarName.equals("")) {
			// indirectVar.f

			if (!indirectVarDeclaringClassName.equals("")
					&& indirectVarType.equals("")) {
				// indirectNonStaticVar.f
				fieldKey = new FieldKey(cc.getName(), fieldName, fieldType,
						fieldDeclaringClassName, field.isStatic(),
						indirectVarName, "", indirectVarDeclaringClassName,
						false, behavior.getName(), behavior.getSignature());

			} else if (!indirectVarDeclaringClassName.equals("")
					&& !indirectVarType.equals("")) {
				// indirestStaticVar.f
				isIndirectVarStatic = true;
				fieldKey = new FieldKey(cc.getName(), fieldName, fieldType,
						fieldDeclaringClassName, field.isStatic(),
						indirectVarName, indirectVarType, "",
						isIndirectVarStatic, behavior.getName(),
						behavior.getSignature());

			} else {
				// localVar.f
				fieldKey = new FieldKey(cc.getName(), fieldName, fieldType,
						fieldDeclaringClassName, field.isStatic(),
						indirectVarName, "", "", false, behavior.getName(),
						behavior.getSignature());
			}
		}
		return fieldKey;
	}

	private int getStartPos(FieldAccess field, int pos) throws BadBytecode {
		CtBehavior behavior = field.where();
		CodeAttribute codeAttr = behavior.getMethodInfo().getCodeAttribute();
		CodeIterator codeIter = codeAttr.iterator();

		LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
				.getAttribute(LineNumberAttribute.tag);

		int line = lineNrAttr.toLineNumber(pos);
		int startPc = lineNrAttr.toStartPc(line);
		int op = codeIter.byteAt(startPc);

		ArrayList<Integer> posList = new ArrayList<>();
		int i = 0;
		while (codeIter.hasNext() && i <= pos) {
			i = codeIter.next();
			posList.add(i);
		}

		int nextPosAfterLastVar = 0;
		Variable lastVar = null;
		if (fieldIsWriterList.size() != 0) {
			lastVar = fieldIsWriterList.get(fieldIsWriterList.size() - 1);

			if (isSameBehavior(field, lastVar)) {
				codeIter.move(lastVar.getAfterPos());
				nextPosAfterLastVar = codeIter.next();
			}
		}

		// get right startPc if nont-static field assignment needs more than 1
		// line
		// check if non-static field assignment starts with the right opcode
		// FIXME: for static fields ???
		if (!field.isStatic()) {
			if (nextPosAfterLastVar != startPc) {
				// field assignment starts with (this, (getfield)* || getstatic,
				// (getfield)* || aload,
				// (getfield)*) putfield
				while (op != Opcode.GETSTATIC
						&& !Mnemonic.OPCODE[op].matches("aload.*")) {
					startPc = posList.get(posList.indexOf(startPc) - 1);
					op = codeIter.byteAt(startPc);
				}
			}
		}

		// fieldAssignment after fieldAssignment:
		// wrong: startPos of later will be set to pc of first instruction of
		// inserted bytecode
		// iterate to pc after added bytecode and set startPc to it
		if (fieldIsWriterList.size() != 0 && isSameBehavior(field, lastVar)
				&& codeIter.hasNext() && nextPosAfterLastVar == startPc) {
			op = codeIter.byteAt(startPc);
			String instr = Mnemonic.OPCODE[op];

			if (instr.matches("ldc.*")) {
				while (codeIter.hasNext() && op != Opcode.INVOKESTATIC) {
					nextPosAfterLastVar = codeIter.next();
					op = codeIter.byteAt(nextPosAfterLastVar);
					instr = Mnemonic.OPCODE[op];
				}
				startPc = codeIter.next();
			}
		}

		return startPc;
	}

	private int getPos(FieldAccess field) throws BadBytecode {
		CtBehavior behavior = field.where();
		CodeAttribute codeAttr = behavior.getMethodInfo().getCodeAttribute();
		CodeIterator codeIter = codeAttr.iterator();

		// int fieldListSize = fieldIsWritterInfoList.size();

		int pos = field.indexOfBytecode();
		int fieldListSize = fieldIsWriterList.size();

		if (fieldListSize != 0) {
			// list is not empty
			Variable lastVar = fieldIsWriterList.get(fieldListSize - 1);

			if (isSameBehavior(field, lastVar)) {
				// last field is in same behavior -> set pos to last field's pos
				// because codeAttr has changed, not the original anymore
				pos = lastVar.getAfterPos();
			}
		}

		// set codeIter to pos
		codeIter.move(pos);
		int op = codeIter.byteAt(codeIter.next());

		// iterate until opcode put.*
		while (!(op == Opcode.PUTFIELD || op == Opcode.PUTSTATIC)) {
			pos = codeIter.next();
			op = codeIter.byteAt(pos);
		}

		// get signature of the field at pos
		// this is for skipping every primitive field
		// ConstPool constPool = behavior.getMethodInfo2().getConstPool();
		int index = codeIter.u16bitAt(pos + 1);
		ConstPool constPool = behavior.getMethodInfo2().getConstPool();
		String signatureOfTestPos = constPool.getFieldrefType(index);
		String signature = field.getSignature();

		// if is not the same signature
		// iterate to next field, check signature etc.
		while (!signature.equals(signatureOfTestPos)) {
			pos = codeIter.next();
			op = codeIter.byteAt(pos);

			while ((op != Opcode.PUTFIELD) && (op != Opcode.PUTSTATIC)) {
				pos = codeIter.next();
				op = codeIter.byteAt(pos);
			}

			index = codeIter.u16bitAt(pos + 1);
			signatureOfTestPos = constPool.getFieldrefType(index);
		}

		return pos;
	}

	private boolean isSameBehavior(FieldAccess field, Variable lastVar) {
		boolean inSameBehavior = false;
		CtBehavior currentBehavior = field.where();
		CtBehavior lastFieldBehavior = lastVar.getBehavior();
		if (lastFieldBehavior != null) {
			inSameBehavior = currentBehavior.getName().equals(
					lastFieldBehavior.getName())
					&& currentBehavior.getDeclaringClass().getName()
							.equals(lastVar.getClassWhereVarIsUsed().getName())
					&& currentBehavior.getSignature().equals(
							lastFieldBehavior.getSignature());
		}
		return inSameBehavior;
	}

	private boolean isFieldFromCurrentCtClass(FieldAccess field)
			throws NotFoundException, BadBytecode {
		CtBehavior behavior = field.where();
		CodeAttribute codeAttr = behavior.getMethodInfo().getCodeAttribute();
		CodeIterator codeIter = codeAttr.iterator();

		int pos = getPos(field);
		int startPos = getStartPos(field, pos);
		int startPosOp = codeIter.byteAt(startPos);

		boolean isStatic = field.isStatic();

		if (field.getClassName().equals(cc.getName())) {
			if (!isStatic && startPosOp != Opcode.ALOAD_0) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	private boolean isFieldPrimitive(FieldAccess field) {
		if (field.getSignature().matches("L.*")) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Makes sure that nothing is entered before the super() call.
	 * 
	 * @param field
	 * @return
	 * @throws BadBytecode
	 */
	private boolean isInnerClassSuperCall(FieldAccess field) throws BadBytecode {

		CtBehavior behavior = field.where();
		CodeAttribute codeAttr = behavior.getMethodInfo().getCodeAttribute();

		CodeIterator codeIter = codeAttr.iterator();
		int i = codeIter.skipConstructor();

		if (i == -1) {
			return false;
		} else {
			return true;
		}

		// if (behavior.getMethodInfo().isMethod() || codeAttr == null)
		// return false;
		//
		// int storePc = getPos(field);
		// int startPc = getStartPos(field, storePc);
		//
		// int checkSuperCallPc = startPc;
		// int checkSuperCallOp = codeIter.byteAt(startPc);
		//
		// codeIter.move(checkSuperCallPc);
		// codeIter.next();
		//
		// boolean isThereAnInvokeSpecial = false;
		//
		// while (checkSuperCallOp != Opcode.INVOKESPECIAL &&
		// codeIter.hasNext()) {
		// checkSuperCallPc = codeIter.next();
		// checkSuperCallOp = codeIter.byteAt(checkSuperCallPc);
		// if (checkSuperCallOp == Opcode.INVOKESPECIAL) {
		// isThereAnInvokeSpecial = true;
		// break;
		// }
		// }
		//
		// if (isThereAnInvokeSpecial) {
		// return true;
		// } else {
		// return false;
		// }
	}

}
