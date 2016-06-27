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
import ch.unibe.scg.nullSpy.instrumentator.model.Variable;

public class ClassFileTranslator implements Translator {

	public static final List<Variable> BYTECODE_INSTRUMENTATION_LIST = new ArrayList<>();

	// private ClassAdapter classAdapter = ClassAdapter.getInstance();
	private HashMap<String, CtClass> analyzedClasses = new HashMap<String, CtClass>();

	@Override
	public void start(ClassPool pool) throws NotFoundException,
			CannotCompileException {

	}

	@Override
	public void onLoad(ClassPool pool, String className)
			throws NotFoundException, CannotCompileException {
		// pool.insertClassPath("C:\\Users\\Lina Tran\\Desktop\\bachelor\\jhotdraw60b1\\bin");

		ClassAdapter classAdapter = ClassAdapter.getInstance();
		if (!className
				.equalsIgnoreCase("ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer")
				&& !className
						.equalsIgnoreCase("ch.unibe.scg.nullSpy.runtimeSupporter.Field")
				&& !className
						.equalsIgnoreCase("ch.unibe.scg.nullSpy.runtimeSupporter.LocalVariable")
				&& !className
						.equalsIgnoreCase("ch.unibe.scg.nullSpy.runtimeSupporter.Variable")
				&& !className
						.equalsIgnoreCase("ch.unibe.scg.nullSpy.runtimeSupporter.IndirectFieldObject")
				&& !className
						.equalsIgnoreCase("ch.unibe.scg.nullSpy.runtimeSupporter.FieldKey")
				&& !className
						.equalsIgnoreCase("ch.unibe.scg.nullSpy.runtimeSupporter.LocalVarKey")) {

			CtClass cc;
			if (analyzedClasses.containsKey(className))
				return;
			else {
				cc = ClassPool.getDefault().get(className);
				analyzedClasses.put(cc.getName(), cc);
			}

			cc.stopPruning(true);

			try {
				// if (cc.getName().equals("org.jhotdraw.standard.QuadTree"))
				classAdapter.adaptProject(cc);
			} catch (Throwable e) {
				e.printStackTrace();
			}

		}
	}
}
