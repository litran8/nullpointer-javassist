package controller;

import model.MyClass;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

public class FieldLogic {

	private CtClass cc;
	private MyClass myClass;

	// private Printer printer;

	public FieldLogic(CtClass cc, MyClass myClass) {
		this.cc = cc;
		this.myClass = myClass;
		// this.printer = new Printer();
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

	/*
	 * public void isFieldMethodCallOfSameClass() throws IllegalAccessException,
	 * InvocationTargetException, NoSuchMethodException, NotFoundException,
	 * CannotCompileException, BadBytecode {
	 * System.out.println("\n--- INVOKE OF FIELDS STARTS ---");
	 * 
	 * HashMap<Field, FieldAccess> fields = this.myClass.getFieldMap();
	 * 
	 * for (Field f : fields.keySet()) {
	 * 
	 * LineNumberAttribute lnNrTable = (LineNumberAttribute) f.getMethod()
	 * .getMethodInfo().getCodeAttribute()
	 * .getAttribute(LineNumberAttribute.tag); ArrayList<Integer> lnNrTableList
	 * = new ArrayList<Integer>(); ArrayList<Integer> lnNrTableValue = new
	 * ArrayList<Integer>(); CodeIterator codeIter;
	 * 
	 * for (int k = 0; k < lnNrTable.tableLength(); k++) {
	 * lnNrTableList.add(lnNrTable.startPc(k));
	 * lnNrTableValue.add(lnNrTable.lineNumber(k)); }
	 * 
	 * codeIter = f.getMethod().getMethodInfo().getCodeAttribute() .iterator();
	 * codeIter.begin();
	 * 
	 * for (int i = 0; i < lnNrTableValue.size(); i++) { if
	 * (f.getFieldLineNumber() == lnNrTableValue.get(i)) { int line =
	 * lnNrTableValue.get(i); // System.out.println(lnNrTable.toNearPc(line -
	 * 1).index); int startPcOfField = lnNrTable.toNearPc(line - 1).index;
	 * 
	 * int op = codeIter.byteAt(startPcOfField); if
	 * (Mnemonic.OPCODE[op].matches("invoke.*")) { // print(f.getMethod());
	 * System.out.println("\nField name: " + f.getFieldName() +
	 * "\tField LineNr: " + f.getFieldLineNumber() + "\tField Class: " +
	 * this.cc.getName()); printer.printInstrAtPos(f.getMethod(), codeIter,
	 * startPcOfField); printer.print(f.getMethod()); } i =
	 * lnNrTableValue.size(); } } }
	 * 
	 * System.out.println("--- INVOKE OF FIELDS ENDS ---\n"); }
	 */
}