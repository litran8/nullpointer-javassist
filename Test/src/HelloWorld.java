public class HelloWorld {

	private static int i = 9;

	public static void say() {
		System.out.println("Hello World");

		Object a = new Object();

		System.out.println("" + a);
		System.out.println(i);
		int i = 0;
		String s = "";
		String s2 = "checkcast";

		System.out.println(s2.matches(".*cast.*"));

	}

	public static void main(String[] args) {
		say();
	}
}
