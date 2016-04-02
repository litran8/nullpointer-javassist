package isFieldOrLocalVariableNullExample;

public class Main2 {
	private Object obj;

	public void testStackMapTable() {
		Object o;
		Object o2;
		int i = 1;

		if (i == 0)
			o = new Object();
		else
			o2 = new Object();

		o = null;
		ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test("Main2",
				"testStackMapTable", o, 16, "o", "localVariable",
				"localVariable");
		System.out.println();
		obj = null;
		ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test("Main2",
				"testStackMapTable", this.obj, 21, "obj", "object", "field");
		System.out.println();
	}
}
