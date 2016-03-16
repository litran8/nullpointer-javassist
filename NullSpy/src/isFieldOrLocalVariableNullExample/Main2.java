package isFieldOrLocalVariableNullExample;

import javassist.expr.NewArray;

public class Main2 {

	public void testStackMapTable() {
		Object o;
		Object o2;
		int i = 1;

		if (i == 0)
			o = new Object();
		else
			o2 = new Object();
	}
}
