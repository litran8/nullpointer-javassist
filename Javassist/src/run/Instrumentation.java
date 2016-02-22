package run;

import javassist.ClassPool;
import javassist.Loader;
import javassist.Translator;
import javassist.bytecode.Opcode;

public class Instrumentation implements Opcode {

	public static String className = "isFieldOrLocalVariableNullExample.AssignToNull";

	// public static String className =
	// "isFieldOrLocalVariableNulExamplel.NullPointerException";

	// public static String className =
	// "isFieldOrLocalVariableNullExample.TestClass";

	public static void main(String[] args) throws Throwable {

		Translator translator = new MyTranslator();
		ClassPool pool = ClassPool.getDefault();
		Loader loader = new Loader();
		loader.addTranslator(pool, translator);
		long startTime = System.nanoTime();
		loader.run(className, args);
		System.out.println("Modified class time: "
				+ ((System.nanoTime() - startTime) / 1000000) + "ms");

	}

	// private static boolean hasMainMethod(CtMethod[] methods)
	// throws CannotCompileException, NotFoundException {
	// for (CtMethod m : methods) {
	// if (m.getName().equals("main")) {
	// CtClass etype = ClassPool.getDefault().get(
	// "java.lang.NullPointerException");
	// m.addCatch("{  System.out.println(\"HIIII\");throw $e; }",
	// etype);
	// return true;
	// }
	// }
	// return false;
	// }

}