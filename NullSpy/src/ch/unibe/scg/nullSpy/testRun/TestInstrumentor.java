package ch.unibe.scg.nullSpy.testRun;

import javassist.ClassPool;
import javassist.Loader;
import javassist.Translator;
import ch.unibe.scg.nullSpy.instrumentator.controller.CsvFileCreator;

public class TestInstrumentor {
	// public static String className =
	// "isFieldOrLocalVariableNullExample.MainAssignToNull";

	private static String className = "isFieldOrLocalVariableNullExample.testMethodCall.FooTest";
	private static String path = "C:\\Users\\Lina Tran\\Desktop\\VarData.csv";
	public static CsvFileCreator csv;

	// public static String className =
	// "org.jhotdraw.samples.javadraw.JavaDrawApp";

	// public static String className = "org.jhotdraw.samples.net.NetApp";

	public static void main(String[] args) throws Throwable {
		long startTime = System.nanoTime();
		csv = new CsvFileCreator(path);
		Translator translator = new ClassFileTranslator();
		ClassPool pool = ClassPool.getDefault();
		Loader loader = new Loader();
		loader.addTranslator(pool, translator);

		loader.run(className, args);
		csv.closeCsvFile();
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
