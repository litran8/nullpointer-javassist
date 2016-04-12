package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import ch.unibe.scg.nullSpy.model.Field;
import ch.unibe.scg.nullSpy.model.IndirectFieldObject;
import ch.unibe.scg.nullSpy.model.PcLine;
import ch.unibe.scg.nullSpy.model.Variable;

/**
 * Instruments test-code after fields.
 */
public class FieldAnalyzer extends VariableAnalyzer {

	private ArrayList<Variable> fieldIsWritterInfoList = new ArrayList<Variable>();

	public FieldAnalyzer(CtClass cc) {
		super(cc);
	}

	/**
	 * Search all fields and store them in an arrayList. The reason for storing
	 * is because directly instrument code in this method does not work... It
	 * instruments test-code after each field assignments.
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

				if (field.isWriter()) {
					try {
						Variable var = null;
						// CtConstructor[] constructorList = cc
						// .getDeclaredConstructors();
						// for (CtConstructor c : constructorList) {
						// Printer p = new Printer();
						// p.printMethod(c, 0);
						// }
						if (fieldIsNotPrimitive(field)) {

							// check if field is instantiated outside, not in
							// method nor constructor
							if (!isFieldInstantiatedInMethod(field)) {

								// not instantiated in method, so check if is
								// instantiated in a constructor
								if (isFieldInstantiatedInConstructor(field)) {

									// instantiated inside constructor
									// check if it's field itself, or a field of
									// the field (direct or indirect)

									CtConstructor constructor = getConstructor(field);
									constructor = (CtConstructor) field.where();

									if (isFieldFromCurrentCtClass(field)) {

										var = storeFieldOfCurrentClass(field,
												constructor);

										// int behaviorLineNr =
										// var.getBehavior()
										// .getMethodInfo()
										// .getLineNumber(0);
										// if (var.getVarLineNr() <
										// behaviorLineNr) {
										// var.setBehavior(null);
										// }
									} else {
										// field of an object/field in current
										// class
										var = storeFieldOfAnotherClass(field,
												constructor);
									}
								} else {

									// not instantiated inside constructor (and
									// not in method)
									var = storeFieldInitiatedOutsideMethod(field);
									System.out.println();
								}
							} else {
								CtMethod method = cc.getDeclaredMethod(field
										.where().getMethodInfo().getName());

								if (isFieldFromCurrentCtClass(field)) {
									var = storeFieldOfCurrentClass(field,
											method);
								} else {
									// field of an object in current class
									var = storeFieldOfAnotherClass(field,
											method);
								}
							}
						}

						adaptByteCode(var);

						// see if inserted at right pos
						if (field.where().getMethodInfo().isMethod()) {
							CtMethod method = cc.getDeclaredMethod(field
									.where().getMethodInfo().getName());
							Printer p = new Printer();
							p.printMethod(method, 0);
						}

					} catch (NotFoundException | BadBytecode e) {
						e.printStackTrace();
					}
				}
			}

		});

		Printer p = new Printer();

		for (CtBehavior behavior : cc.getDeclaredBehaviors()) {
			System.out.println();
			System.out.println(behavior.getName());
			p.printMethod(behavior, 0);
		}

		for (CtBehavior behavior : cc.getDeclaredConstructors()) {
			System.out.println();
			System.out.println(behavior.getName());
			p.printMethod(behavior, 0);
		}

		System.out.println();

	}

	private CtConstructor getConstructor(FieldAccess field) throws BadBytecode {
		int fieldLineNr = field.getLineNumber();
		CtConstructor aimConstructor = null;
		for (CtConstructor constructor : cc.getDeclaredConstructors()) {
			CodeAttribute codeAttribute = constructor.getMethodInfo()
					.getCodeAttribute();

			if (codeAttribute == null) {
				continue;
			}

			if (fieldLineNr >= constructor.getMethodInfo().getLineNumber(0)
					&& fieldLineNr <= getLastLineNrOfBehavior(constructor)) {
				aimConstructor = constructor;
				break;
			}
		}
		return aimConstructor;
	}

	private boolean isFieldInstantiatedInConstructor(FieldAccess field)
			throws BadBytecode {
		boolean inConstructor = isInBehavior(field,
				cc.getDeclaredConstructors());

		return inConstructor;
	}

	/**
	 * Check if field is instantiated in a method or a constructor. If not, it
	 * returns false.
	 * 
	 * @param field
	 * @return
	 * @throws NotFoundException
	 * @throws BadBytecode
	 */
	private boolean isFieldInstantiatedInMethod(FieldAccess field)
			throws NotFoundException, BadBytecode {
		boolean inMethod = isInBehavior(field, cc.getDeclaredMethods());

		return inMethod;
	}

	private boolean isInBehavior(FieldAccess field, CtBehavior[] ctBehaviorList)
			throws BadBytecode {
		boolean inBehavior = false;

		for (CtBehavior behavior : ctBehaviorList) {
			CodeAttribute codeAttribute = behavior.getMethodInfo()
					.getCodeAttribute();

			if (codeAttribute == null) {
				continue;
			}

			CtBehavior fieldbeh = field.where();
			String m1 = behavior.getSignature();
			String m2 = fieldbeh.getSignature();

			if (behavior.getMethodInfo().isConstructor()) {
				inBehavior = behavior.getMethodInfo() == fieldbeh
						.getMethodInfo();
			} else {
				inBehavior = field.getLineNumber() >= behavior.getMethodInfo()
						.getLineNumber(0)
						&& field.getLineNumber() <= getLastLineNrOfBehavior(behavior);
			}
			if (inBehavior) {
				break;
			}

		}
		return inBehavior;
	}

	private int getLastLineNrOfBehavior(CtBehavior behavior) throws BadBytecode {
		int pos = 0;
		CodeIterator codeIterator = behavior.getMethodInfo().getCodeAttribute()
				.iterator();
		// get last pc of method (end of method)
		while (codeIterator.hasNext()) {
			pos = codeIterator.next();
		}

		// int methodEnd = behavior.getMethodInfo().getLineNumber(pos);
		return behavior.getMethodInfo().getLineNumber(pos);
	}

	private Variable storeFieldInitiatedOutsideMethod(FieldAccess field)
			throws NotFoundException, BadBytecode {
		return storeFieldOfCurrentClass(field, null);
	}

	/**
	 * If the field is of another class than the current analyzed one. E.g. p.a:
	 * p->PersonClass
	 * 
	 * @param field
	 * @throws NotFoundException
	 * @throws BadBytecode
	 */
	private Variable storeFieldOfAnotherClass(FieldAccess field,
			CtBehavior behavior) throws NotFoundException, BadBytecode {

		boolean isAnotherClassAnInnerClass = isAnotherClassAnInnerClass(field);

		CodeAttribute codeAttribute = behavior.getMethodInfo()
				.getCodeAttribute();
		CodeIterator codeIterator = codeAttribute.iterator();

		LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttribute
				.getAttribute(LineNumberAttribute.tag);
		HashMap<Integer, Integer> lineNumberMap = getLineNumberMap(behavior);
		ArrayList<PcLine> sortedLineNrMap = getSortedLineNrMapAsList(lineNumberMap);

		LocalVariableAttribute localVariableTable = (LocalVariableAttribute) codeAttribute
				.getAttribute(LocalVariableAttribute.tag);
		ArrayList<LocalVariableTableEntry> localVariableTableList = getStableLocalVariableTableAsList(localVariableTable);

		// lineNr
		// int fieldLineNumber = field.getLineNumber();
		int pos = getPos(field);
		int startPos = getInstrStartPos(field, pos);

		codeIterator.move(pos);
		codeIterator.next();
		int afterPos = 0;
		if (codeIterator.hasNext()) {
			afterPos = codeIterator.next();
		}

		int fieldLineNr = lineNrAttr.toLineNumber(pos); // --------------------HEREEE
														// startPos

		// pos and posAfterAssignment
		// int pos = getPc(lineNumberMap, fieldLineNumber);
		// int posAfterAssignment = getPosAfterAssignment(pos, codeIterator,
		// "put.*");

		// innerclass
		String innerClassFieldName = "";

		String fieldName = field.getFieldName();
		String fullName = "";

		// object_FIELD
		int op = codeIterator.byteAt(startPos);
		String opCode_field = Mnemonic.OPCODE[op];

		// OBJECT_field
		IndirectFieldObject indirectFieldObject;

		String objectName_field = "";
		String objectType_field = "";
		String objectBelongedClassName_field = "";
		boolean isfieldStatic_field = false;

		if (!field.isStatic()) {

			if (Mnemonic.OPCODE[op].matches("aload.*")) {
				// localVar.field
				// store locVar e.g. p.a -> get p
				int localVarTableIndex = 0;

				localVarTableIndex = getLocalVarTableIndex(codeIterator,
						localVariableTableList, startPos, "aload.*");
				int locVarSlot = getLocVarArraySlot(codeIterator, startPos);
				opCode_field = "aload_" + locVarSlot;

				// localVarName.field
				objectName_field = localVariableTableList
						.get(localVarTableIndex).varName;
				objectType_field = localVariableTableList
						.get(localVarTableIndex).varType;

			} else {
				// field.field
				String instruction = InstructionPrinter.instructionString(
						codeIterator, startPos, field.where().getMethodInfo2()
								.getConstPool());
				int brace = instruction.indexOf("(");
				instruction = instruction.substring(
						instruction.lastIndexOf(".") + 1, brace);
				objectName_field = instruction;

				CtField ctField_field = cc.getField(objectName_field);

				objectBelongedClassName_field = ctField_field
						.getDeclaringClass().getName();
				objectType_field = ctField_field.getSignature();
				isfieldStatic_field = op == Opcode.GETSTATIC;
				// testMethodByteCode.addGetfield(belongedClassNameOfVariable,
				// variableName, variableType);
			}

			// if (isAnotherClassAnInnerClass &&
			// objectName_field.equals("this")) {
			// // innerClass: this.innerClassField.field
			// codeIterator.move(pos);
			// codeIterator.next();
			// int innerClassGetFieldPos = codeIterator.next();
			// int index = codeIterator.u16bitAt(innerClassGetFieldPos + 1);
			//
			// // innerClassField_field
			// innerClassFieldName = behavior.getMethodInfo2().getConstPool()
			// .getFieldrefName(index);
			// fieldName = objectName_field + "." + innerClassFieldName + "."
			// + field.getFieldName();
			// } else {
			// fieldName = objectName_field + "." + field.getFieldName();
			// }
			//
			// if (opCode_field.matches("get.*") && !isfieldStatic_field) {
			// fieldName = "this." + fieldName;
			// }

		}
		// else {
		// fieldName = field.getClassName() + "." + field.getFieldName();
		// }

		indirectFieldObject = new IndirectFieldObject(objectName_field,
				objectType_field, objectBelongedClassName_field,
				isfieldStatic_field, opCode_field);

		String fieldType = field.getSignature();
		String fieldBelongedClassName = field.getClassName();

		Field var = new Field("field", fieldName, fieldType,
				fieldBelongedClassName, fieldLineNr, pos, startPos, afterPos,
				cc, behavior, field.isStatic(), indirectFieldObject);

		fieldIsWritterInfoList.add(var);

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
	private Variable storeFieldOfCurrentClass(FieldAccess field,
			CtBehavior behavior) throws NotFoundException, BadBytecode {

		if (field.where().getMethodInfo().isMethod()) {
			CtMethod method = cc.getDeclaredMethod(field.where()
					.getMethodInfo().getName());
			Printer p = new Printer();
			// p.printMethod(method, 0);
		}

		// behavior = field.where();
		// CodeIterator iter = behavior.getMethodInfo().getCodeAttribute()
		// .iterator();
		// HashMap<Integer, Integer> lineNrMap = getLineNumberMap(behavior);
		// ArrayList<PcLine> sortedLineNrMapAsList =
		// getSortedLineNrMapAsList(lineNrMap);
		String fieldName2 = field.getFieldName();
		int fieldLineNr = field.getLineNumber();
		String objectName_Field = "";
		int pos = 0;
		int startPos = 0;
		int posAfterAssignment = 0;

		// if field is initiated outside a method -> method is null
		if (behavior != null) {
			CodeAttribute codeAttribute = behavior.getMethodInfo()
					.getCodeAttribute();
			CodeIterator codeIterator = codeAttribute.iterator();

			// HashMap<Integer, Integer> lineNumberMap =
			// getLineNumberMap(behavior);
			// ArrayList<PcLine> sortedLineNrMapAsList =
			// getSortedLineNrMapAsList(lineNumberMap);
			Printer p = new Printer();
			p.printMethod(behavior, 0);
			pos = getPos(field);
			startPos = getInstrStartPos(field, pos);
			codeIterator.move(pos);
			codeIterator.next();

			if (codeIterator.hasNext()) {
				posAfterAssignment = codeIterator.next();
			}

			LineNumberAttribute lineNrAttr = (LineNumberAttribute) codeAttribute
					.getAttribute(LineNumberAttribute.tag);
			fieldLineNr = lineNrAttr.toLineNumber(pos);

			// posAfterAssignment = getPosAfterAssignment(pos, codeIterator,
			// "put.*");

			LocalVariableAttribute localVariableTable = (LocalVariableAttribute) codeAttribute
					.getAttribute(LocalVariableAttribute.tag);
			ArrayList<LocalVariableTableEntry> localVariableTableList = getStableLocalVariableTableAsList(localVariableTable);
			int localVarTableIndex = getLocalVarTableIndex(codeIterator,
					localVariableTableList, pos, "aload.*");
			objectName_Field = localVariableTableList.get(localVarTableIndex).varName
					+ ".";
		} else {

			// field instantiated outside behavior, that means insert in every
			// constructor

			CtBehavior voidConstructor = cc.getDeclaredConstructors()[0];
			CodeAttribute codeAttr = voidConstructor.getMethodInfo()
					.getCodeAttribute();
			CodeIterator codeIter = codeAttr.iterator();

			if (fieldIsWritterInfoList.size() != 0) {

				if (field.isStatic()) {
					int fieldListSize = fieldIsWritterInfoList.size();

					Variable lastOutsideInstatiatedStaticField = null;

					// gets the last static outside field
					for (int i = fieldListSize - 1; i >= 0; i--) {
						lastOutsideInstatiatedStaticField = fieldIsWritterInfoList
								.get(i);

						if (lastOutsideInstatiatedStaticField.getBehavior() == null
								&& lastOutsideInstatiatedStaticField.isStatic()
								&& cc.getName().equals(
										lastOutsideInstatiatedStaticField
												.getBelongedClass().getName())) {
							break;
						}
					}

					if (lastOutsideInstatiatedStaticField == null) {
						// first static outside field
						pos = 1;
					} else {
						// set pos to last static outside field afterPos
						// iterate until inovke.* -> pos = invoke.*-pc
						codeIter.move(lastOutsideInstatiatedStaticField
								.getAfterPos());
						pos = codeIter.next();
						int op = codeIter.byteAt(pos);

						while (!Mnemonic.OPCODE[op].matches("invoke.*")) {
							pos = codeIter.next();
							op = codeIter.byteAt(pos);
						}
					}
				} else {
					pos = field.indexOfBytecode();
				}

			} else {

				// first field (static or nonStatic)
				// should be pc 1
				pos = field.indexOfBytecode();
			}

			codeIter.move(pos);
			codeIter.next();
			posAfterAssignment = codeIter.next();

		}

		// String fieldName = (field.isStatic() ? "" : objectName_Field)
		// + field.getFieldName();
		String fieldName = field.getFieldName();
		String fieldType = field.getSignature();
		String fieldBelongedClassName = cc.getName();

		Field var = new Field("field", fieldName, fieldType,
				fieldBelongedClassName, fieldLineNr, pos, startPos,
				posAfterAssignment, cc, behavior, field.isStatic(), null);
		fieldIsWritterInfoList.add(var);
		return var;
	}

	private int getInstrStartPos(FieldAccess field, int pos) throws BadBytecode {
		int res = 0;
		CtBehavior behavior = field.where();
		CodeIterator iter = behavior.getMethodInfo().getCodeAttribute()
				.iterator();
		HashMap<Integer, Integer> lineNrMap = getLineNumberMap(behavior);
		ArrayList<PcLine> sortedLineNrMapAsList = getSortedLineNrMapAsList(lineNrMap);

		Object[] keys = lineNrMap.keySet().toArray();
		Arrays.sort(keys);

		for (int i = 0; i < keys.length; i++) {
			if ((int) keys[i] > pos) {
				res = (int) keys[i - 1];

				if (fieldIsWritterInfoList.size() != 0) {
					Variable lastVar = fieldIsWritterInfoList
							.get(fieldIsWritterInfoList.size() - 1);

					if (isSameBehavior(field, lastVar)) {

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

		return res;
	}

	private int getPos(FieldAccess field) throws BadBytecode {
		CtBehavior behavior = field.where();
		CodeAttribute attr = behavior.getMethodInfo().getCodeAttribute();
		CodeIterator iter = attr.iterator();
		int pos = 0;
		if (behavior.getMethodInfo().isConstructor()) {
			pos = field.indexOfBytecode();
		}
		int op = iter.byteAt(pos);
		if (Mnemonic.OPCODE[op].matches("put.*"))
			return pos;

		if (fieldIsWritterInfoList.size() != 0) {
			Variable lastVar = fieldIsWritterInfoList
					.get(fieldIsWritterInfoList.size() - 1);

			if (isSameBehavior(field, lastVar)) {
				pos = lastVar.getStorePos();
			}
		}

		iter.move(pos);
		iter.next();
		pos = iter.next();

		op = iter.byteAt(pos);
		String s = Mnemonic.OPCODE[op];

		while (!s.matches("put.*") && iter.hasNext()) {
			pos = iter.next();
			op = iter.byteAt(pos);
			s = Mnemonic.OPCODE[op];
		}

		// System.out.println(InstructionPrinter.instructionString(iter, pos,
		// behavior.getMethodInfo2().getConstPool()));

		return pos;
	}

	private boolean isSameBehavior(FieldAccess field, Variable lastVar) {
		boolean inSameBehavior = false;
		CtBehavior currentBehavior = field.where();
		CtBehavior lastBehavior = lastVar.getBehavior();
		if (!(lastBehavior == null)) {
			inSameBehavior = currentBehavior.getName().equals(
					lastBehavior.getName())
					&& currentBehavior.getDeclaringClass().getName()
							.equals(lastVar.getBelongedClass().getName())
					&& currentBehavior.getSignature().equals(
							lastBehavior.getSignature());
		}
		return inSameBehavior;
	}

	private boolean isFieldFromCurrentCtClass(FieldAccess field)
			throws NotFoundException {
		if (field.getClassName().equals(cc.getName()))
			return true;
		else
			return false;
	}

	private boolean fieldIsNotPrimitive(FieldAccess field) {
		if (field.getSignature().matches("L.*"))
			return true;
		else
			return false;
	}

}
