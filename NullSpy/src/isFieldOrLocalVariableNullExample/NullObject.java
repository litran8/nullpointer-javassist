package isFieldOrLocalVariableNullExample;

public class NullObject {
	private static String str1 = null;
	private static String str2 = null;
	private Object o = null;

	public static Object setToNullMethod(Object a, Object p) {
		NullObject obj = new NullObject();
		try {
			String str = "string 1";
			obj.setStr1(str);
			setToNull();
			obj = setToNull();
			// int i = 2;
			// String s = obj.getStr1().substring(0, i);
			// s = s.substring(0, 1);
		} catch (Throwable t) {
			// System.out.println("Java ERROR: " + t);
			// t.printStackTrace();
		}
		int i = 1;
		// try {
		// String str = "string 1";
		// obj.setStr1(str);
		// setToNull();
		// obj = setToNull();
		// // int i = 2;
		// // String s = obj.getStr1().substring(0, i);
		// // s = s.substring(0, 1);
		// } catch (Throwable t) {
		// System.out.println("Java ERROR: " + t);
		// t.printStackTrace();
		// }
		i = 0;
		return obj;
	}

	public Object toNull(Object i) {
		o = i;
		return null;
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

	private static NullObject setToNull() {
		return null;
	}
}
