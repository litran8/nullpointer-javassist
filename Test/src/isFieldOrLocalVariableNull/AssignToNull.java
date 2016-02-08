package isFieldOrLocalVariableNull;

public class AssignToNull {
	public static Object a;
	public static Object b;
	public static Object c;
	public static Integer i;

	// Object a, b, c, e, f
	// Integer i
	// Double d
	// int j, k, l

	public static void main(String[] args) {
		Person p = new Person();
		a = null;
		i = 9;
		x(5);
		y();
		c = p.say();
		b = setDouble();

	}

	public static void x(int j) {
		int k = j;
		Double h = null;
		i = new Integer(j);
		System.out.println("Method: x was called");
	}

	private static void y() {
		Object e = null;
		int l = 9;
		double d = 9.0;
		i = null;
		Object f = setDouble();
		System.out.println("Method: y was called");
	}

	private static Object setDouble() {
		return new Object();
	}

}