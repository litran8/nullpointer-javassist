package ch.unibe.scg.nullSpy.instrumentator.controller;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import ch.unibe.scg.nullSpy.run.MainProjectModifier;

public class MainBehaviorModifier {

	public static void addTryCatchToMainMethod(CtClass cc)
			throws NotFoundException, CannotCompileException {
		CtBehavior[] behaviors = cc.getDeclaredBehaviors();

		for (CtBehavior behavior : behaviors) {
			if (behavior.getName().equals("main")) {
				StringBuilder sb = new StringBuilder();

				sb.append("{");

				sb.append("StackTraceElement[] stElem = $e.getStackTrace();");
				sb.append("ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.printLocationOnMatch");
				sb.append("(");
				sb.append("\"" + MainProjectModifier.csvPath + "\"");
				// sb.append("\"" +
				// "C:\\\\Users\\\\Lina Tran\\\\Desktop\\\\VarData.csv"
				// + "\""); // testLine
				sb.append(",");
				sb.append("ch.unibe.scg.nullSpy.runtimeSupporter.VariableTester.getLocalVarMap()");
				sb.append(",");
				sb.append("ch.unibe.scg.nullSpy.runtimeSupporter.VariableTester.getFieldMap()");
				sb.append(",");
				sb.append("stElem[0].getClassName()");
				sb.append(",");
				sb.append("stElem[0].getLineNumber()");
				sb.append(",");
				sb.append("stElem[0].getMethodName()");
				sb.append(");");

				sb.append("throw $e;");

				sb.append("}");

				CtClass etype = ClassPool.getDefault().get(
						"java.lang.Throwable");
				behavior.addCatch(sb.toString(), etype);
			}
		}
	}
}
