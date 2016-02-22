package isFieldOrLocalVariableNullExample;

public class NullObject {
	private static String str1 = null;
	private static String str2 = null;

	public static Object setToNullMethod() {
		NullObject obj = new NullObject();
		try {

			obj.setStr1("string 1");
			obj = setToNull();
			obj.setStr2("string 2");
		} catch (Throwable t) {

		}
		return obj;
	}

	private void setStr2(String string) {
		str2 = string;

	}

	private void setStr1(String string) {
		str1 = string;

	}

	private static NullObject setToNull() {
		return null;
	}
}
