package ch.unibe.scg.nullSpy.testRun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.Translator;
import ch.unibe.scg.nullSpy.instrumentator.controller.ClassAdapter;
import ch.unibe.scg.nullSpy.model.Variable;

public class ClassFileTranslator implements Translator {

	public static final List<Variable> BYTECODE_INSTRUMENTATION_LIST = new ArrayList<>();

	private ClassAdapter classAdapter = ClassAdapter.getInstance();
	private HashMap<String, CtClass> analyzedClasses = new HashMap<String, CtClass>();

	@Override
	public void start(ClassPool pool) throws NotFoundException,
			CannotCompileException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoad(ClassPool pool, String className)
			throws NotFoundException, CannotCompileException {
		// pool.insertClassPath("C:\\Users\\Lina Tran\\Desktop\\bachelor\\JHotDraw\\bin");

		if (!className
				.equalsIgnoreCase("ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer")
				&& !className
						.equalsIgnoreCase("ch.unibe.scg.nullSpy.runtimeSupporter.Field")
				&& !className
						.equalsIgnoreCase("ch.unibe.scg.nullSpy.runtimeSupporter.LocalVariable")) {

			CtClass cc;
			if (analyzedClasses.containsKey(className))
				return;
			else {
				cc = ClassPool.getDefault().get(className);
				analyzedClasses.put(cc.getName(), cc);
			}

			cc.stopPruning(true);

			try {
				// if (cc.getName().equals("org.jhotdraw.beans.AbstractBean"))
				classAdapter.instrumentCodeAfterFieldLocVarAssignment(cc);
			} catch (Throwable e) {
				e.printStackTrace();
			}

		}
	}
}
