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

		cc.instrument(new ExprEditor() {
			public void edit(FieldAccess field) throws CannotCompileException {
				CtBehavior behavior = field.where();
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

						// Printer p = new Printer();
						// System.out.println("\nBefore:");
						// p.printMethod(field.where(), 0);

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

							// System.out.println("Method: "
							// + var.getBehavior().getName());

							// insert code after assignment
							adaptByteCode(var);
						}
					} catch (NotFoundException | BadBytecode e) {
						e.printStackTrace();
					}
				}
			}

		});

		// Printer p = new Printer();
		//
		// CtBehavior classInit = cc.getClassInitializer();
		// if (classInit != null) {
		// System.out.println("\n" + classInit.getName());
		// p.printMethod(classInit, 0);
		// }
		//
		// for (CtBehavior b : cc.getDeclaredConstructors()) {
		//
		// if (b.getMethodInfo().getCodeAttribute() != null) {
		// System.out.println("\n" + b.getName());
		// System.out.println(b.getSignature());
		// p.printMethod(b, 0);
		// }
		// }
		//
		// for (CtBehavior b : cc.getDeclaredMethods()) {
		// if (b.getMethodInfo().getCodeAttribute() != null) {
		// System.out.println("\n" + b.getName());
		// p.printMethod(b, 0);
		// }
		// }
		//
		// System.out.println();
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

		CtBehavior behavior = field.where();
		boolean isAnotherClassAnInnerClass = isAnotherClassAnInnerClass(field);

		CodeAttribute codeAttribute = behavior.getMethodInfo()
				.getCodeAttribute();
		CodeIterator codeIterator = codeAttribute.iterator();

		LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttribute
				.getAttribute(LineNumberAttribute.tag);

		LocalVariableAttribute localVarAttr = (LocalVariableAttribute) codeAttribute
				.getAttribute(LocalVariableAttribute.tag);
		ArrayList<LocalVarAttrEntry> localVarAttrAsList = getLocalVarAttrAsList(localVarAttr);

		int pos = getPos(field);
		int startPos = getStartPos(field, pos);
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

		int fieldLineNr = lineNrAttr.toLineNumber(pos);

		// innerclass
		String innerClassFieldName = "";

		String fieldName = field.getFieldName();

		// object_FIELD
		int op = codeIterator.byteAt(startPos);
		String indirectVarOpCode = Mnemonic.OPCODE[op];

		// OBJECT_field
		IndirectVar indirectFieldObject = null;

		String indirectVarName = "";
		String indirectVarType = "";
		String indirectVarDeclaringClassName = "";
		boolean isIndirectVarStatic = false;

		if (!field.isStatic()) {

			if (Mnemonic.OPCODE[op].matches("aload.*")) {
				// localVar.field
				// store locVar e.g. p.a -> get p
				int localVarAttrIndex = 0;

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
				// testMethodByteCode.addGetfield(belongedClassNameOfVariable,
				// variableName, variableType);
			}

		}

		if (isAnotherClassAnInnerClass && indirectVarName.equals("this")) {
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

		FieldKey fieldKey = new FieldKey(cc.getName(), fieldName, fieldType,
				fieldDeclaringClassName, field.isStatic(), "", "", "", false,
				behavior.getName(), behavior.getSignature());

		// FieldKey fieldKey = new FieldKey(fieldName, fieldDeclaringClassName);

		if (field.isStatic()) {
			// class.f
			// fieldKey = new FieldKey(fieldName, fieldType, field.isStatic());
			// fieldKey = new FieldKey(cc.getName(), fieldName, fieldType,
			// fieldDeclaringClassName, field.isStatic(), "", "", "", false,
			// behavior.getName(), behavior.getSignature());

		} else if (!field.isStatic() && !indirectVarName.equals("")) {
			// indirectVar.f

			if (!indirectVarDeclaringClassName.equals("")
					&& indirectVarType.equals("")) {
				// indirectNonStaticVar.f
				// fieldKey = new FieldKey(fieldName, fieldDeclaringClassName,
				// indirectVarName, indirectVarDeclaringClassName);
				fieldKey = new FieldKey(cc.getName(), fieldName, fieldType,
						fieldDeclaringClassName, field.isStatic(),
						indirectVarName, "", indirectVarDeclaringClassName,
						false, behavior.getName(), behavior.getSignature());

			} else if (!indirectVarDeclaringClassName.equals("")
					&& !indirectVarType.equals("")) {
				// indirestStaticVar.f
				isIndirectVarStatic = true;
				// fieldKey = new FieldKey(fieldName, fieldDeclaringClassName,
				// indirectVarName, indirectVarType, isIndirectVarStatic);
				fieldKey = new FieldKey(cc.getName(), fieldName, fieldType,
						fieldDeclaringClassName, field.isStatic(),
						indirectVarName, indirectVarType, "",
						isIndirectVarStatic, behavior.getName(),
						behavior.getSignature());

			} else {
				// localVar.f
				// fieldKey = new FieldKey(fieldName, fieldDeclaringClassName,
				// indirectVarName,
				// behavior.getDeclaringClass().getName(),
				// behavior.getName(), behavior.getSignature());
				fieldKey = new FieldKey(cc.getName(), fieldName, fieldType,
						fieldDeclaringClassName, field.isStatic(),
						indirectVarName, "", "", false, behavior.getName(),
						behavior.getSignature());
			}
		}

		fieldMap.put(fieldKey, var);

		return var;
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
		int fieldLineNr = field.getLineNumber();
		int pos = 0;
		int startPos = 0;
		int posAfterAssignment = 0;

		CodeAttribute codeAttribute = behavior.getMethodInfo()
				.getCodeAttribute();
		CodeIterator codeIterator = codeAttribute.iterator();

		pos = getPos(field);
		startPos = getStartPos(field, pos);
		codeIterator.move(pos);
		codeIterator.next();

		if (codeIterator.hasNext()) {
			posAfterAssignment = codeIterator.next();
		}

		LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttribute
				.getAttribute(LineNumberAttribute.tag);
		fieldLineNr = lineNrAttr.toLineNumber(pos);

		String fieldName = field.getFieldName();
		String fieldType = field.getSignature();
		String fieldDeclaringClassName = cc.getName();

		Field var = new Field("field", fieldName, fieldType,
				fieldDeclaringClassName, fieldLineNr, pos, startPos,
				posAfterAssignment, cc, behavior, field.isStatic(), null);
		fieldIsWritterInfoList.add(var);

		// hashMap
		// FieldKey fieldKey;
		// if (field.isStatic()) {
		// class.f
		// fieldKey = new FieldKey(fieldName, fieldType, field.isStatic());
		FieldKey fieldKey = new FieldKey(cc.getName(), fieldName, fieldType,
				fieldDeclaringClassName, field.isStatic(), "", "", "", false,
				behavior.getName(), behavior.getSignature());
		// } else {
		// this.f
		// fieldKey = new FieldKey(fieldName, fieldDeclaringClassName);
		// }

		fieldMap.put(fieldKey, var);

		return var;
	}

	private int getStartPos(FieldAccess field, int pos) throws BadBytecode {
		int startPos = 0;
		CtBehavior behavior = field.where();
		CodeAttribute codeAttr = behavior.getMethodInfo().getCodeAttribute();
		CodeIterator codeIter = codeAttr.iterator();
		ArrayList<Integer> posList = new ArrayList<>();

		int i = 0;
		while (codeIter.hasNext() && i <= pos) {
			i = codeIter.next();
			posList.add(i);
		}

		LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttr
				.getAttribute(LineNumberAttribute.tag);

		int line = lineNrAttr.toLineNumber(pos);
		startPos = lineNrAttr.toStartPc(line);

		int op = codeIter.byteAt(startPos);

		int nextPosAfterLastVar = 0;

		if (fieldIsWritterInfoList.size() != 0) {
			Variable lastVar = fieldIsWritterInfoList
					.get(fieldIsWritterInfoList.size() - 1);

			if (isSameBehavior(field, lastVar)) {

				codeIter.move(lastVar.getAfterPos());
				nextPosAfterLastVar = codeIter.next();
			}
		}

		// get right startPos if field assignment needs more than 1 line
		if (!field.isStatic()) {

			if (nextPosAfterLastVar != startPos) {
				while (op != Opcode.GETSTATIC
						&& !Mnemonic.OPCODE[op].matches("aload.*")
						&& op != Opcode.ALOAD_0) {
					startPos = posList.get(posList.indexOf(startPos) - 1);
					op = codeIter.byteAt(startPos);
				}
			}

		}

		// fieldAssignment after fieldAssignment:
		// startPos of later will be set to pos of inserted byte code of the
		// first one
		// iterate to pos after byte code and set startPos to it
		if (fieldIsWritterInfoList.size() != 0) {
			Variable lastVar = fieldIsWritterInfoList
					.get(fieldIsWritterInfoList.size() - 1);

			if (isSameBehavior(field, lastVar)) {

				codeIter.move(lastVar.getStorePos());
				codeIter.next();
				nextPosAfterLastVar = codeIter.next();

				if (codeIter.hasNext() && nextPosAfterLastVar == startPos) {
					op = codeIter.byteAt(startPos);
					String instr = Mnemonic.OPCODE[op];
					if (instr.matches("ldc.*")) {

						while (codeIter.hasNext()
								&& !instr.matches("invokestatic.*")) {
							nextPosAfterLastVar = codeIter.next();
							op = codeIter.byteAt(nextPosAfterLastVar);
							instr = Mnemonic.OPCODE[op];
						}
						startPos = codeIter.next();
					}
				}
			}
		}

		return startPos;
	}

	private int getPos(FieldAccess field) throws BadBytecode {
		CtBehavior behavior = field.where();
		CodeAttribute codeAttr = behavior.getMethodInfo().getCodeAttribute();
		CodeIterator codeIter = codeAttr.iterator();

		ConstPool constPool = behavior.getMethodInfo2().getConstPool();

		int fieldListSize = fieldIsWritterInfoList.size();

		int pos = 0;

		if (fieldListSize == 0) {
			// list is empty, read pos from original codeAttr
			pos = field.indexOfBytecode();
		} else if (fieldListSize != 0) {
			// list is not empty
			Variable lastVar = fieldIsWritterInfoList.get(fieldListSize - 1);
			CtBehavior lastFieldBehavior = lastVar.getBehavior();

			if (isSameBehavior(behavior, lastFieldBehavior)) {
				// last field is in same behavior -> set pos to last field's pos
				// because codeAttr has changed, not the original anymore
				pos = lastVar.getAfterPos();
			} else {
				// last field not from same behavior -> codeAttr of the current
				// behavior is still the original one
				field.indexOfBytecode();
			}
		}

		// set codeIter to pos
		codeIter.move(pos);
		int op = codeIter.byteAt(codeIter.next());

		// iterate until opcode put.*
		while ((op != Opcode.PUTFIELD) && (op != Opcode.PUTSTATIC)) {
			pos = codeIter.next();
			op = codeIter.byteAt(pos);
		}

		// get signature of the field at pos
		// this is for skipping every primitive field
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

	private boolean isSameBehavior(CtBehavior behavior,
			CtBehavior lastFieldBehavior) {
		return behavior.getName().equals(lastFieldBehavior.getName())
				&& behavior.getSignature().equals(
						lastFieldBehavior.getSignature())
				&& behavior
						.getDeclaringClass()
						.getName()
						.equals(lastFieldBehavior.getDeclaringClass().getName());
	}

	private boolean isSameBehavior(FieldAccess field, Variable lastVar) {
		boolean inSameBehavior = false;
		CtBehavior currentBehavior = field.where();
		CtBehavior lastBehavior = lastVar.getBehavior();
		if (!(lastBehavior == null)) {
			inSameBehavior = currentBehavior.getName().equals(
					lastBehavior.getName())
					&& currentBehavior.getDeclaringClass().getName()
							.equals(lastVar.getClassWhereVarIsUsed().getName())
					&& currentBehavior.getSignature().equals(
							lastBehavior.getSignature());
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

		int checkSuperCallPos = 0;
		int checkSuperCallOp = codeIter.byteAt(0);

		codeIter.move(checkSuperCallPos);
		codeIter.next();

		boolean isThereAnInvokeSpecial = false;

		while (checkSuperCallOp != Opcode.INVOKESPECIAL && codeIter.hasNext()) {
			checkSuperCallPos = codeIter.next();
			checkSuperCallOp = codeIter.byteAt(checkSuperCallPos);
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
