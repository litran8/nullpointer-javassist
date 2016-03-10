package isFieldOrLocalVariableNullExample;

public class TestMethodCall {
	private static String str1 = null;
	private static String str2 = null;

	public static void main(String[] args) {
		TestMethodCall o = (TestMethodCall) TestMethodCall.setToNullMethod();
	}

	public static Object setToNullMethod() {
		TestMethodCall obj = new TestMethodCall();
		try {
			String str = "string 1";
			obj.setStr1(str);
			int i = 2;
			String s = obj.getStr1().substring(0, i).toString();

			setToNull();
			int r = 5;
			bla(r);
			obj = setToNull();

			// s = obj.getStr1().substring(0, i);
			// s = s.substring(0, 1);
		} catch (Throwable t) {
			// System.out.println("Java ERROR: " + t);
			// t.printStackTrace();
		}
		return obj;
	}

	private static void bla(int r) {
		int i = 5;

	}

	private void setStr2(String string) {
		str2 = string;
	}

	private void setStr1(String string) {
		str1 = string;
	}

	private String getStr1() {
		return str1;
	}

	private static TestMethodCall setToNull() {
		return null;
	}
}
