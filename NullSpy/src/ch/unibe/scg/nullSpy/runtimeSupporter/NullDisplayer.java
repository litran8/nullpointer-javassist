package ch.unibe.scg.nullSpy.runtimeSupporter;

public class NullDisplayer {
	public static void test(String className, Object varValue, int lineNr,
			String varName) {
		if (varValue == null) {

			// System.out.print(isField(lineNr) ? "Field " : "Local variable ");
			System.out.print(varName + " at line " + lineNr + " is null: ");
			System.out.println(getNullLink(className, lineNr));
		}
	}

	private static String getNullLink(String className, int lineNumber) {
		String nullLink;
		nullLink = "(" + className + ".java:" + lineNumber + ")";
		return nullLink;
	}
}
