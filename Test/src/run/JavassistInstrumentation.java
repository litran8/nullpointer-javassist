package javassistPackage;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.Opcode;
import Controller.Iteration;

public class RegexNullAssignment implements Opcode {

	public static String className = "isFieldOrLocalVariableNull.AssignToNull";
	// public static String className =
	// "isFieldOrLocalVariableNull.Javassist_AssignToNull";

	// public static String className =
	// "isFieldOrLocalVariableNull.NullPointerException";

	// public static String className = "isFieldOrLocalVariableNull.TestClass";

	static long startTime;
	static long endTime;

	public static void main(String[] args) throws Exception {
		CtClass cc = ClassPool.getDefault().get(className);
		// cc.setName("isFieldOrLocalVariableNull.Javassist_AssignToNull");
		cc.stopPruning(true);

		for (CtMethod m : cc.getDeclaredMethods()) {
			if (m.getName().equals("main")) {
				CtField f = CtField.make("public static long startTime;", cc);
				cc.addField(f);
				m.insertBefore("startTime = System.nanoTime();");
				m.insertAfter("System.out.println(\"\\nOriginal class time: \" +((System.nanoTime() - startTime)/1000000) + \" ms\");");
			}
		}

		Iteration iter = Iteration.getInstance();

		iter.goThrough(cc);

		// String destination = "bin/isFieldOrLocalVariableNull/" + className
		// + ".class";
		// (cc.getClassFile()).write(new DataOutputStream(new FileOutputStream(
		// destination)));

		// run
		cc.writeFile();
		// cc = ClassPool.getDefault().get(className);
		Class<?> c = cc.toClass();
		cc.defrost();

		if (hasMainMethod(cc.getDeclaredMethods())) {
			startTime = System.nanoTime();
			c.getDeclaredMethod("main", new Class[] { String[].class }).invoke(
					null, new Object[] { args });
			System.out.println("Changed class time: "
					+ ((System.nanoTime() - startTime) / 1000000) + " ms");
		}

	}

	private static boolean hasMainMethod(CtMethod[] methods)
			throws CannotCompileException, NotFoundException {
		for (CtMethod m : methods) {
			if (m.getName().equals("main")) {
				CtClass etype = ClassPool.getDefault().get(
						"java.lang.NullPointerException");
				m.addCatch("{  System.out.println(\"HIIII\");throw $e; }",
						etype);
				return true;
			}
		}
		return false;

	}

}
