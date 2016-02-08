package isFieldOrLocalVariableNull;

import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;

public class Field {

	private CtClass cc;

	public Field(CtClass cc) {
		this.cc = cc;
	}

	public void insertFieldNullLink(CtMethod[] methods,
			ArrayList<Integer> fieldLineNumbers, ArrayList<String> fieldNames)
			throws CannotCompileException {

		CtMethod methodToModify = methods[0];
		int lineNrMethod1 = 0;
		int lineNrMethod2 = 0;

		for (int i = 0; i < fieldLineNumbers.size(); i++) {
			for (int j = 0; j < methods.length; j++) {
				lineNrMethod1 = methods[j].getMethodInfo().getLineNumber(0) - 1;
				if (j < methods.length - 1)
					lineNrMethod2 = methods[j + 1].getMethodInfo()
							.getLineNumber(0) - 1;
				else {
					lineNrMethod2 = lineNrMethod1;
					methodToModify = methods[j];
				}
				if (fieldLineNumbers.get(i) > lineNrMethod1
						&& fieldLineNumbers.get(i) < lineNrMethod2) {
					methodToModify = methods[j];
					j = methods.length;
				}
			}

			String nullLink = setNullLink(fieldLineNumbers, i);
			int insertLineNumber = fieldLineNumbers.get(i) + 1;

			if (insertLineNumber >= methods[0].getMethodInfo().getLineNumber(0)) {
				methodToModify.insertAt(
						insertLineNumber,
						"if(isFieldValueNull(" + fieldNames.get(i)
								+ ")) System.out.println(\"Field "
								+ fieldNames.get(i) + " is null: (" + nullLink
								+ ")\");");
			}
		}
	}

	private String setNullLink(ArrayList<Integer> fieldLineNumbers, int i) {
		String nullLink;
		nullLink = this.cc.getName() + ".java:" + fieldLineNumbers.get(i);
		return nullLink;
	}
}
