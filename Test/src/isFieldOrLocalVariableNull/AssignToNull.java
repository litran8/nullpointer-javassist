package isFieldOrLocalVariableNull;

public class AssignToNull {
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
		System.out.println("\nMethod main starts.");
		Person p = (Person) isFieldOrLocalVariableNull.Person.say();
		// a = null;
		// i = 9;
		// int h = 0;
		// System.out.println(Thread.currentThread().getStackTrace()[1]
		// .getClassName());
		// nullObject = (NullObject) a;
		nullObject = (NullObject) NullObject.setToNullMethod();
		// x(5);
		y();
		c = p.say();
		b = setNull();
		NullObject nO = (NullObject) NullObject.setToNullMethod();
		System.out.println("Method mein ends.\n");
	}

	public static void x(int j) {
		System.out.println("\nMethod x starts.");
		int k = j;
		int kk = k;
		Double h = null;
		i = new Integer(j);
		System.out.println("Method x ends.\n");
	}

	private static void y() {
		System.out.println("\nMethod y starts.");
		int u;
		Object e = null;
		// double d = 9.0;
		// double dd = d;
		// i = null;
		Object f = setNull();
		Object h = null;
		// int v = getInt();
		// getInt();
		Object l = h;
		System.out.println("Method y ends.\n");
	}

	private static int getInt() {
		return 8;
	}

	private static Object setNull() {
		return null;
	}
}