package ch.unibe.scg.nullSpy.instrumentator.controller;

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
import ch.unibe.scg.nullSpy.instrumentator.model.Field;
import ch.unibe.scg.nullSpy.instrumentator.model.FieldKey;
import ch.unibe.scg.nullSpy.instrumentator.model.IndirectVar;
import ch.unibe.scg.nullSpy.instrumentator.model.Variable;

/**
 * Instruments test-code after fields.
 */
public class FieldAnalyzer extends VariableAnalyzer {

	private ArrayList<Variable> fieldIsWritterInfoList;
	private HashMap<FieldKey, Field> fieldMap;

	public FieldAnalyzer(CtClass cc,
			ArrayList<Variable> fieldIsWritterInfoList,
			HashMap<FieldKey, Field> fieldMap) {
		super(cc);
		this.fieldIsWritterInfoList = fieldIsWritterInfoList;
		this.fieldMap = fieldMap;
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

		cc.instrument(new ExprEditor() {
			public void edit(FieldAccess field) throws CannotCompileException {
				CtBehavior behavior = field.where();
				// if (!behavior.getName().equals("DrawApplication"))
				// return;
				// String sign = behavior.getSignature();
				CodeAttribute codeAttr = behavior.getMethodInfo()
						.getCodeAttribute();
				LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
						.getAttribute(LineNumberAttribute.tag);
				if (lineNrAttr == null) {
					return;
				}
				if (field.isWriter() && codeAttr != null) {
					try {
						Variable var = null;
						int pc = field.indexOfBytecode();
						// fieldType is an object -> starts with L.*
						if (isFieldNotPrimitive(field)
								&& !isInnerClassSuperCall(field)) {

							if (isFieldFromCurrentCtClass(field)) {
								// direct fields
								var = storeFieldOfCurrentClass(field);

							} else {
								// indirect fields
								var = storeFieldOfAnotherClass(field);
							}

							// insert code after assignment
							adaptByteCode(var);
						}
					} catch (NotFoundException | BadBytecode e) {
						e.printStackTrace();
					}
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

		int fieldLineNr = field.getLineNumber();
		String fieldName = field.getFieldName();
		String fieldType = field.getSignature();
		String fieldDeclaringClassName = cc.getName();

		Field var = new Field("field", fieldName, fieldType,
				fieldDeclaringClassName, fieldLineNr, pos, startPos,
				posAfterAssignment, cc, behavior, field.isStatic(), null);
		fieldIsWritterInfoList.add(var);

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

		fieldIsWritterInfoList.add(var);

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
		if (fieldIsWritterInfoList.size() != 0) {
			lastVar = fieldIsWritterInfoList
					.get(fieldIsWritterInfoList.size() - 1);

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
		if (fieldIsWritterInfoList.size() != 0
				&& isSameBehavior(field, lastVar) && codeIter.hasNext()
				&& nextPosAfterLastVar == startPc) {
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

		int pos = field.indexOfBytecode();
		int fieldListSize = fieldIsWritterInfoList.size();

		if (fieldListSize != 0) {
			// list is not empty
			Variable lastVar = fieldIsWritterInfoList.get(fieldListSize - 1);

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
		ConstPool constPool = behavior.getMethodInfo2().getConstPool();
		int index = codeIter.u16bitAt(pos + 1);
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

	private boolean isFieldNotPrimitive(FieldAccess field) {
		if (field.getSignature().matches("L.*"))
			return true;
		else
			return false;
	}

	private boolean isInnerClassSuperCall(FieldAccess field) throws BadBytecode {

		CtBehavior behavior = field.where();
		CodeAttribute codeAttr = behavior.getMethodInfo().getCodeAttribute();

		if (behavior.getMethodInfo().isMethod() || codeAttr == null)
			return false;

		CodeIterator codeIter = codeAttr.iterator();

		int storePc = getPos(field);
		int startPc = getStartPos(field, storePc);

		int checkSuperCallPc = startPc;
		int checkSuperCallOp = codeIter.byteAt(startPc);

		codeIter.move(checkSuperCallPc);
		codeIter.next();

		boolean isThereAnInvokeSpecial = false;

		while (checkSuperCallOp != Opcode.INVOKESPECIAL
				&& checkSuperCallPc < storePc) {
			checkSuperCallPc = codeIter.next();
			checkSuperCallOp = codeIter.byteAt(checkSuperCallPc);
			if (checkSuperCallOp == Opcode.INVOKESPECIAL) {
				isThereAnInvokeSpecial = true;
				break;
			}
		}

		if (isThereAnInvokeSpecial) {
			return true;
		} else {
			return false;
		}
	}

}
