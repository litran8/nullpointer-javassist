package isFieldOrLocalVariableNullExample;

public class MainAssignToNull {
	public static Object a;
	public static Object b;
	public static Object c;
	public static Integer i;
	public static NullObject nullObject;

	// Object a, b, c, e, f
	// Integer i
	// Double d
	// int j, k,

	public static void main(String[] args) {
		// long startTime = System.nanoTime();

		System.out.println("\nMethod main starts.");
		Person p = (Person) isFieldOrLocalVariableNullExample.Person.say();
		a = null;
		// i = 9;
		// int h = 0;
		// nullObject = (NullObject) a;
		nullObject = (NullObject) NullObject.setToNullMethod();
		y();
		c = p.say();
		b = setNull();
		NullObject nO = (NullObject) NullObject.setToNullMethod();
		nO = (NullObject) p.say();
		Object u = nullObject.setToNullMethod();
		System.out.println("Method main ends.\n");

		// System.out.println("Original time: "
		// + ((System.nanoTime() - startTime) / 1000000) + "ms");
	}

	private static void y() {
		System.out.println("\nMethod y starts.");
		int u;
		Object e = null;
		// double d = 9.0;
		// double dd = d;
		// i = null;
		Object f = setNull();
		// int v = getInt();
		// getInt();
		Object h = e;
		System.out.println("Method y ends.\n");
	}

	private static Object setNull() {
		return null;
	}

	// public static void x(int j) {
	// System.out.println("\nMethod x starts.");
	// int k = j;
	// int kk = k;
	// Double h = null;
	// i = new Integer(j);
	// System.out.println("Method x ends.\n");
	// // System.out.println(Thread.currentThread().getStackTrace()[1]
	// // .getClassName());
	// }
	//
	// private static int getInt() {
	// return 8;
	// }
}