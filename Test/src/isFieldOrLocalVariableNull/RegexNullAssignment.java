package isFieldOrLocalVariableNull;

import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

public class RegexNullAssignment {

	public static String className = "isFieldOrLocalVariableNull.AssignToNull";

	// public static String className = "isFieldNull.TestClass";

	public static void main(String[] args) throws Exception {
		CtClass cc = ClassPool.getDefault().get(className);
		cc.stopPruning(true);

		CtMethod[] methods = cc.getDeclaredMethods();

		LocalVar localVar = new LocalVar(cc);
		Field field = new Field(cc);

		// add methodLineNumbers; checks whether
		// local variables of the method is null
		int methodLine;
		ArrayList<Integer> methodLineNumbers = new ArrayList<Integer>();
		for (CtMethod m : methods) {
			methodLine = m.getMethodInfo().getLineNumber(0) - 1;
			methodLineNumbers.add(methodLine);
			localVar.isLocalVarNull(m);
		}

		// adds method for checking whether a field is null
		CtMethod m = CtNewMethod
				.make("public static boolean isFieldValueNull(Object o) {return o == null;}",
						cc);
		cc.setModifiers(cc.getModifiers() & ~Modifier.ABSTRACT);
		cc.addMethod(m);

		// search all fields; store fieldLineNumbers and fieldNames
		ArrayList<Integer> fieldLineNumbers = new ArrayList<Integer>();
		ArrayList<String> fieldNames = new ArrayList<String>();
		cc.instrument(new ExprEditor() {
			public void edit(FieldAccess arg) throws CannotCompileException {
				if (arg.isWriter()) {
					fieldLineNumbers.add(arg.getLineNumber());
					fieldNames.add(arg.getFieldName());
				}
			}
		});

		// inserts isFieldValueNull method at specific line
		field.insertFieldNullLink(methods, fieldLineNumbers, fieldNames);

		Class<?> c = cc.toClass();

		c.getDeclaredMethod("main", new Class[] { String[].class }).invoke(
				null, new Object[] { args });

	}

}