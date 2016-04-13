package ch.unibe.scg.nullSpy.testRun;

import javassist.ClassPool;
import javassist.Loader;
import javassist.Translator;

public class TestInstrumentor {
	public static String className = "isFieldOrLocalVariableNullExample.MainAssignToNull";

	public static void main(String[] args) throws Throwable {
		long startTime = System.nanoTime();
		Translator translator = new ClassFileTranslator();
		ClassPool pool = ClassPool.getDefault();
		Loader loader = new Loader();
		loader.addTranslator(pool, translator);

		loader.run(className, args);
		System.out.println("Modification + modified class time: "
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
