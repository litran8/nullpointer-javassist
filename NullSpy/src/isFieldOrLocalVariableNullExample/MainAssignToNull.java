package isFieldOrLocalVariableNullExample;

public class MainAssignToNull {
	
	public static Object a = null;
	public static Object b;
	public static Object c;
	public static Integer i;
	public static NullObject nullObject;
	public static Person o;

	public static void main(String[] args) {
		// long startTime = System.nanoTime();

		System.out.println("\nMethod main starts.");

		a = null;
		Object d = null;
		d = null;

		b = a;
		b = d;
		d = a;
		Object d2 = null;
		d = d2;

		Person p = new Person();
		o = new Person();
		Person p2 = (Person) Person.say();
		p.a = null; // aload, aconst, putfield Person.a
		p.o = null;
		Person.o = null;// putstatic Person.o
		a = Person.say();
		d = Person.say();
		Object d3 = Person.say();
		o = (Person) Person.say();
		o = (Person) p.a;
		o = (Person) p.o;

		a = p.o;
		d = p.a;
		d = p.o;
		Object d4 = p.a;
		Object d5 = p.o;

		NullObject nO = new NullObject();
		NullObject nO2 = (NullObject) NullObject.setToNullMethod(a, d);
		a = NullObject.setToNullMethod(a, d);
		a = (NullObject) NullObject.setToNullMethod(a, d);
		nullObject = (NullObject) NullObject.setToNullMethod(a, d);
		d = NullObject.setToNullMethod(a, d);
		d = (NullObject) NullObject.setToNullMethod(a, d);
		Object d6 = NullObject.setToNullMethod(a, d);
		Object d7 = (NullObject) NullObject.setToNullMethod(a, d);

		a = nO.toNull(a);
		a = nO.toNull(d);
		d = nO.toNull(a);
		d = nO.toNull(d);
		p = (Person) nO.toNull(a);
		p = (Person) nO.toNull(d);
		p = (Person) NullObject.setToNullMethod(a, d);
		nO = (NullObject) Person.say();
		// nO = (NullObject) p.a;
		nO = (NullObject) p.o;

		y();

		c = Person.say(); // p.say() works too, but not a good way
		b = setNull();
		// Person p2 = new Person();
		// p = null;
		// p.print();
		nullObject = new NullObject();
		p2 = (Person) nullObject.toNull(null);
		// p2.print2();
		// NullObject nO = (NullObject) NullObject.setToNullMethod(a, p);
		nO = (NullObject) Person.say(); // p.say() works too, but not a good way
		Object u = nullObject.setToNullMethod(a, p);
		System.out.println("Method main ends.\n");

		// System.out.println("Original time: "
		// + ((System.nanoTime() - startTime) / 1000000) + "ms");
	}

	private static void y() {
		System.out.println("\nMethod y starts.");
		int u;
		Object e = null;
		Object f = setNull();
		Object h = e;
		System.out.println("Method y ends.\n");
	}

	private static Object setNull() {
		return null;
	}
}