package isFieldOrLocalVariableNullExample;

public class NullPointerException {

	private static String str1 = null;
	private static String str2 = null;

	public NullPointerException() {

	}

	public static void main(String[] args) {
		// try {
		NullPointerException obj = new NullPointerException();

		obj.setStr1("string 1");
		obj = setToNull();
		obj.setStr2("string 2");

		// } catch (Throwable t) {
		// System.out.println("Java ERROR: " + t);
		// t.printStackTrace();
		// }
	}

	private static NullPointerException setToNull() {
		return null;
	}

	public String getStr1() {
		return this.str1;
	}

	public void setStr1(String str1) {
		this.str1 = str1;
	}

	public String getStr2() {
		return str2;
	}

	public void setStr2(String str2) {
		this.str2 = str2;
	}

}
