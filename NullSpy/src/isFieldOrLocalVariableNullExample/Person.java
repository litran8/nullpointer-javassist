package isFieldOrLocalVariableNullExample;

public class Person {

	public Object a;
	public static Object o;

	public static Object say() {
		Object o = null;
		return o;
	}

	public Object print2() {
		System.out.println("Hi2");
		return null;
	}

	public static void print() {
		System.out.println("Hi");
	}
}
