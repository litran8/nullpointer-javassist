package run;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.Translator;
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

			cc.stopPruning(true);

			try {
				iter.goThrough(cc);
			} catch (Throwable e) {
				e.printStackTrace();
			}

		}
	}
}
