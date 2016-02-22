package run;

import java.lang.reflect.InvocationTargetException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.Translator;
import javassist.bytecode.BadBytecode;
import controller.Iteration;

public class MyTranslator implements Translator {

	private Iteration iter = Iteration.getInstance();

	@Override
	public void start(ClassPool pool) throws NotFoundException,
			CannotCompileException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoad(ClassPool pool, String className)
			throws NotFoundException, CannotCompileException {
		if (!className.equalsIgnoreCase("controller.Iteration")) {
			CtClass cc = ClassPool.getDefault().get(className);
			// cc.setName("isFieldOrLocalVariableNull.Javassist_AssignToNull");

			cc.stopPruning(true);

			for (CtMethod m : cc.getDeclaredMethods()) {
				if (m.getName().equals("main")) {
					CtField f = CtField.make("public static long startTime;",
							cc);
					cc.addField(f);
					m.insertBefore("startTime = System.nanoTime();");
					m.insertAfter("System.out.println(\"\\nOriginal class time: \" +((System.nanoTime() - startTime)/1000000) + \" ms\");");
				}
			}

			try {
				iter.goThrough(cc);
			} catch (IllegalAccessException | InvocationTargetException
					| NoSuchMethodException | BadBytecode e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
