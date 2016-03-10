package ch.unibe.scg.nullSpy.tests;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.junit.Test;

public class TestLocVarLogic {

	public static final String packageName = "ch.unibe.scg.nullSpy.tests.useCases.";

	@Test
	public void test() throws NotFoundException {

		CtClass classToTest = ClassPool.getDefault().get(
				packageName + "LocalVariableIsNull");

	}
}
