package isFieldOrLocalVariableNullExample;

public class TestMethodCall {

	// private TestMethodCall classItself = new TestMethodCall();
	private static TestMethodCall classItselfStatic = new TestMethodCall();

	private Object objectNonStatic = new Object();
	private static Object objectStatic = new Object();

	private TestMethodCallPerson personNonStatic = new TestMethodCallPerson();
	private static TestMethodCallPerson personStatic = new TestMethodCallPerson();

	public static void main(String[] args) {
		TestMethodCallPerson personLocalVar = new TestMethodCallPerson();
		personLocalVar
				.methodCallSetFieldOfIndirectVarToNullWithoutReturnWithoutParams();
		System.out.println("finish");
	}

	public void testMehodOnNonStaticVars() {

		Object paramLocalVar = new Object();
		TestMethodCallPerson personLocalVar = new TestMethodCallPerson();
		personLocalVar
				.methodCallSetFieldOfIndirectVarToNullWithoutReturnWithoutParams();
		// 16 aload_2 [personLocalVar]
		// 17 invokevirtual
		// isFieldOrLocalVariableNullExample.TestMethodCallPerson.methodCallSetFieldOfIndirectVarToNullWithoutReturnWithoutParams()
		// : void [40]

		// personLocalVar
		// .methodCallSetFieldOfIndirectVarToNullWithoutReturnWithParams(personLocalVar);
		// // 20 aload_2 [personLocalVar]
		// // 21 aload_2 [personLocalVar]
		// // 22 invokevirtual
		// //
		// isFieldOrLocalVariableNullExample.TestMethodCallPerson.methodCallSetFieldOfIndirectVarToNullWithoutReturnWithParams(java.lang.Object)
		// // : void [43]
		// // 25 aload_2 [personLocalVar]
		//
		// personLocalVar
		// .methodCallSetFieldOfIndirectVarToNullWithoutReturnWithParams(this.objectNonStatic);
		// // 25 aload_2 [personLocalVar]
		// // 26 aload_0 [this]
		// // 27 getfield
		// // isFieldOrLocalVariableNullExample.TestMethodCall.objectNonStatic :
		// // java.lang.Object [34]
		// // 30 invokevirtual
		// //
		// isFieldOrLocalVariableNullExample.TestMethodCallPerson.methodCallSetFieldOfIndirectVarToNullWithoutReturnWithParams(java.lang.Object)
		// // : void [43]
		//
		// personLocalVar
		// .methodCallSetFieldOfIndirectVarToNullWithoutReturnWithParams(TestMethodCall.objectStatic);
		// // 33 aload_2 [personLocalVar]
		// // 34 getstatic
		// // isFieldOrLocalVariableNullExample.TestMethodCall.objectStatic :
		// // java.lang.Object [23]
		// // 37 invokevirtual
		// //
		// isFieldOrLocalVariableNullExample.TestMethodCallPerson.methodCallSetFieldOfIndirectVarToNullWithoutReturnWithParams(java.lang.Object)
		// // : void [43]
		//
		// personLocalVar
		// .methodCallSetFieldOfIndirectVarToNullWithoutReturnWithParams(personLocalVar.car);
		//
		// // 40 aload_2 [personLocalVar]
		// // 41 aload_2 [personLocalVar]
		// // 42 getfield
		// // isFieldOrLocalVariableNullExample.TestMethodCallPerson.car :
		// // java.lang.Object [47]
		// // 45 invokevirtual
		// //
		// isFieldOrLocalVariableNullExample.TestMethodCallPerson.methodCallSetFieldOfIndirectVarToNullWithoutReturnWithParams(java.lang.Object)
		// // : void [43]
		//
		// personLocalVar
		// .methodCallSetFieldOfIndirectVarToNullWithoutReturnWithParams(TestMethodCallPerson.head);
		// // 48 aload_2 [personLocalVar]
		// // 49 getstatic
		// // isFieldOrLocalVariableNullExample.TestMethodCallPerson.head :
		// // java.lang.Object [50]
		// // 52 invokevirtual
		// //
		// isFieldOrLocalVariableNullExample.TestMethodCallPerson.methodCallSetFieldOfIndirectVarToNullWithoutReturnWithParams(java.lang.Object)
		// // : void [43]
		//
		// TestMethodCall classItselfLocalVar = new TestMethodCall();

	}

	// nonStatic

	public Object methodCallSetToNullWithReturnWithoutParams() {
		return null;
	}

	public Object methodCallSetToNullWithReturnWithParams(Object obj) {
		return null;
	}

	public void methodCallSetFieldToNullWithoutReturnWithoutParams() {
		this.objectNonStatic = null;
	}

	public void methodCallSetFieldToNullWithoutReturnWithParams(Object obj) {
		this.objectNonStatic = null;
	}

	// static

	public static Object methodCallStaticWithReturnWithoutParams() {
		return null;
	}

	public static Object methodCallStaticWithReturnWithParams(Object obj) {
		return null;
	}

	public static void methodCallStaticWithoutReturnWithoutParams() {
		TestMethodCall.objectStatic = null;
	}

	public static void methodCallStaticWithoutReturnWithParams(Object obj) {
		TestMethodCall.objectStatic = null;
	}

}
