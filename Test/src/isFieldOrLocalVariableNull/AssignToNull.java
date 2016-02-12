package isFieldOrLocalVariableNull;

public class AssignToNull {
	public static Object a;
	public static Object b;
	public static Object c;
	public static Integer i;

	// Object a, b, c, e, f
	// Integer i
	// Double d
	// int j, k,

	public static void main(String[] args) {
		Person p = new Person();
		a = null;
		i = 9;
		int h = 0;
		x(5);
		y();
		c = p.say();
		b = setNull();
	}

	public static void x(int j) {
		int k = j;
		int kk = k;
		Double h = null;
		i = new Integer(j);
		System.out.println("Method: x was called");
	}

	private static void y() {
		int u;
		Object e = null;
		double d = 9.0;
		double dd = d;
		i = null;
		Object f = setNull(); // not shown yet: local var indirectly set to null
		Object h = null;
		int v = getInt();
		getInt();
		Object l = h;
		System.out.println("Method: y was called");
	}

	private static int getInt() {
		return 8;
	}

	private static Object setNull() {
		return null;
	}
}