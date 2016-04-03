package isFieldOrLocalVariableNullExample;

public class Main2 {
	private Object obj;
	private privateClass thisPrivateClassField;

	public void testStackMapTable() {
		Object o;
		Object o2;
		int i = 1;

		if (i == 0)
			o = new Object();
		else
			o2 = new Object();

		// var
		o = null;
		ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test("Main2",
				"testStackMapTable", o, 18, "o", "localVariable",
				"localVariable_1");
		System.out.println();
		// 27 ldc <String "Main2"> [19]
		// 29 ldc <String "testStackMapTable"> [21]
		// 31 aload_1 [o]
		// 32 bipush 18
		// 34 ldc <String "o"> [22]
		// 36 ldc <String "localVariable"> [24]
		// 38 ldc <String "localVariable_1"> [26]
		// 40 invokestatic
		// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test(java.lang.String,
		// java.lang.String, java.lang.Object, int, java.lang.String,
		// java.lang.String, java.lang.String) : void [28]

		// field
		obj = null;
		ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test("Main2",
				"testStackMapTable", obj, 36, "obj", "object", "field");
		// obj -> aload_0 [this], getfield...
		System.out.println();
		// 54 ldc <String "Main2"> [19]
		// 56 ldc <String "testStackMapTable"> [21]
		// 58 aload_0 [this]
		// 59 getfield isFieldOrLocalVariableNullExample.Main2.obj :
		// java.lang.Object [45]
		// 62 bipush 36
		// 64 ldc <String "obj"> [47]
		// 66 ldc <String "object"> [48]
		// 68 ldc <String "field"> [50]
		// 70 invokestatic
		// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test(java.lang.String,
		// java.lang.String, java.lang.Object, int, java.lang.String,
		// java.lang.String, java.lang.String) : void [28]

		// var private obj
		privateClass privateClassObject = new privateClass();
		privateClassObject.privateClassField = null;
		ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test("Main2",
				"testStackMapTable", privateClassObject.privateClassField, 57,
				"privateClassObject.privateClassField", "object", "field");
		// aload 4, getfield !!!!!
		System.out.println();
		// 95 ldc <String "Main2"> [19]
		// 97 ldc <String "testStackMapTable"> [21]
		// 99 aload 4 [privateClassObject]
		// 101 getfield
		// isFieldOrLocalVariableNullExample.Main2$privateClass.privateClassField
		// : java.lang.Object [57]
		// 104 bipush 57
		// 106 ldc <String "privateClassObject.privateClassField"> [60]
		// 108 ldc <String "object"> [48]
		// 110 ldc <String "field"> [50]
		// 112 invokestatic
		// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test(java.lang.String,
		// java.lang.String, java.lang.Object, int, java.lang.String,
		// java.lang.String, java.lang.String) : void [28]

		// field private class
		thisPrivateClassField = new privateClass();
		thisPrivateClassField.privateClassField = null;
		ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test("Main2",
				"testStackMapTable", thisPrivateClassField.privateClassField,
				80, "thisPrivateClassField.privateClassField", "object",
				"field");
		// aload_0 [this], getfield, getfield
		System.out.println();
		// 141 ldc <String "Main2"> [19]
		// 143 ldc <String "testStackMapTable"> [21]
		// 145 aload_0 [this]
		// 146 getfield
		// isFieldOrLocalVariableNullExample.Main2.thisPrivateClassField :
		// isFieldOrLocalVariableNullExample.Main2.privateClass [62]
		// 149 getfield
		// isFieldOrLocalVariableNullExample.Main2$privateClass.privateClassField
		// : java.lang.Object [57]
		// 152 bipush 80
		// 154 ldc <String "thisPrivateClassField.privateClassField"> [64]
		// 156 ldc <String "object"> [48]
		// 158 ldc <String "field"> [50]
		// 160 invokestatic
		// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test(java.lang.String,
		// java.lang.String, java.lang.Object, int, java.lang.String,
		// java.lang.String, java.lang.String) : void [28]

		Main2 m2 = new Main2();
		m2.obj = null;
	}

	private class privateClass {
		public Object privateClassField;

		public privateClass() {
			this.privateClassField = new Object();
		}
	}
}
