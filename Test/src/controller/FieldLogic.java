package Controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.Mnemonic;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import Modell.Field;
import Modell.MyClass;

public class FieldLogic {

	private CtClass cc;
	private MyClass myClass;

	public FieldLogic(CtClass cc, MyClass myClass) {
		this.cc = cc;
		this.myClass = myClass;
	}

	/**
	 * Search all fields; store <(name, lineNr, method), fieldAccess>
	 * 
	 * @param this.cc
	 * @param myClass
	 * @throws CannotCompileException
	 */
	public void searchAndStoreField() throws CannotCompileException {
		CtClass cc = this.cc;
		MyClass myClass = this.myClass;

		this.cc.instrument(new ExprEditor() {
			public void edit(FieldAccess arg) throws CannotCompileException {
				if (arg.isWriter()) {

					if (arg.getLineNumber() > cc.getDeclaredMethods()[0]
							.getMethodInfo().getLineNumber(0)) {
						try {

							// System.out.println(arg.indexOfBytecode());
							myClass.storeField(
									arg.getFieldName(),
									arg.getLineNumber(),
									cc.getDeclaredMethod(arg.where()
											.getMethodInfo().getName()), arg);
						} catch (NotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}

	public void isFieldMethodCallOfSameClass() throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			NotFoundException, CannotCompileException, BadBytecode {
		System.out.println("\n--- INVOKE OF FIELDS STARTS ---");

		HashMap<Field, FieldAccess> fields = this.myClass.getFieldMap();

		for (Field f : fields.keySet()) {

			LineNumberAttribute lnNrTable = (LineNumberAttribute) f.getMethod()
					.getMethodInfo().getCodeAttribute()
					.getAttribute(LineNumberAttribute.tag);
			ArrayList<Integer> lnNrTableList = new ArrayList<Integer>();
			ArrayList<Integer> lnNrTableValue = new ArrayList<Integer>();
			CodeIterator codeIter;

			for (int k = 0; k < lnNrTable.tableLength(); k++) {
				lnNrTableList.add(lnNrTable.startPc(k));
				lnNrTableValue.add(lnNrTable.lineNumber(k));
			}

			codeIter = f.getMethod().getMethodInfo().getCodeAttribute()
					.iterator();
			for (int i = 0; i < lnNrTableValue.size(); i++) {
				if (f.getFieldLineNumber() == lnNrTableValue.get(i)) {
					int line = lnNrTableValue.get(i);
					// System.out.println(lnNrTable.toNearPc(line - 1).index);
					int startPcOfField = lnNrTable.toNearPc(line - 1).index;

					int op = codeIter.byteAt(startPcOfField);
					if (Mnemonic.OPCODE[op].matches("invoke.*")) {
						// print(f.getMethod());
						System.out.println("Field name: " + f.getFieldName()
								+ "\tField LineNr: " + f.getFieldLineNumber()
								+ "\tField Class: " + this.cc.getName());
						printInstrAtPos(f.getMethod(), codeIter, startPcOfField);
					}
					i = lnNrTableValue.size();
				}
			}
		}

		System.out.println("--- INVOKE OF FIELDS ENDS ---\n");
	}

	private void printInstrAtPos(CtMethod method, CodeIterator codeIterator,
			int instrCounter) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			NotFoundException, CannotCompileException, BadBytecode {
		System.out.println(instrCounter
				+ ": "
				+ InstructionPrinter.instructionString(codeIterator,
						instrCounter, method.getMethodInfo2().getConstPool()));
		System.out.println("MethodRefClassName: "
				+ method.getMethodInfo2()
						.getConstPool()
						.getMethodrefClassName(
								codeIterator.u16bitAt(instrCounter + 1)));
		System.out.println("Method: "
				+ method.getMethodInfo2()
						.getConstPool()
						.getMethodrefName(
								codeIterator.u16bitAt(instrCounter + 1)));
		System.out.println();

	}
}
