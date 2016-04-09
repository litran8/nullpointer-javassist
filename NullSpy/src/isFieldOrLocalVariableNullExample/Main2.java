package isFieldOrLocalVariableNullExample;

public class Main2 {
	private Object obj = null;
	private PrivateClass thisPrivateClassField;
	private Person pers;
	private String s;

	public void setString(String s, String s2) {
		String t = "gla" + "bla";
		this.s = "gla";
	}

	public Object getObject(Object param) {
		return obj;
	}

	public void testStackMapTable() {
		setString("bla", "bla");
		Object o = null;
		Object o2;
		int i = 1;

		if (i == 0)
			o = new Object();
		else
			o2 = new Object();

		Person p = new Person();
		p.a = null;
		pers.a = null;

		Object r = p.a; // localVar_field
		// 40 aload 4 [p]
		// 42 getfield isFieldOrLocalVariableNullExample.Person.a :
		// java.lang.Object [22]
		// 45 astore 5 [r]

		r = pers.a; // this.field_field
		// 55 aload_0 [this]
		// 56 getfield isFieldOrLocalVariableNullExample.Main2.pers :
		// isFieldOrLocalVariableNullExample.Person [27]
		// 59 getfield isFieldOrLocalVariableNullExample.Person.a :
		// java.lang.Object [24]

		r = obj; // field
		// 66 aload_0 [this]
		// 67 getfield isFieldOrLocalVariableNullExample.Main2.obj :
		// java.lang.Object [16]
		// 70 astore 5 [r]

		r = p; // localVar
		// 72 aload 4 [p]
		// 74 astore 5 [r]

		PrivateClass privateClassObject = new PrivateClass();
		privateClassObject.privateClassField = null;
		r = privateClassObject.privateClassField; // localVar_privateField
		// 92 aload 6 [privateClassObject]
		// 94 getfield
		// isFieldOrLocalVariableNullExample.Main2$PrivateClass.privateClassField
		// : java.lang.Object [36]
		// 97 astore 5 [r]

		thisPrivateClassField = new PrivateClass();
		thisPrivateClassField.privateClassField = null;
		r = thisPrivateClassField.privateClassField; // this.field_privateField
		// 119 aload_0 [this]
		// 120 getfield
		// isFieldOrLocalVariableNullExample.Main2.thisPrivateClassField :
		// isFieldOrLocalVariableNullExample.Main2.PrivateClass [39]
		// 123 getfield
		// isFieldOrLocalVariableNullExample.Main2$PrivateClass.privateClassField
		// : java.lang.Object [36]
		// 126 astore 5 [r]

		// // var
		// o = null;
		// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test("Main2",
		// "testStackMapTable", o, 18, "o", "localVariable",
		// "localVariable_1");
		//
		// // 31 aload_1 [o] <---
		//
		// // 32 bipush 18
		// // 34 ldc <String "o"> [22]
		// // 36 ldc <String "localVariable"> [24]
		// // 38 ldc <String "localVariable_1"> [26]
		// // 40 invokestatic
		// //
		// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test(java.lang.String,
		// // java.lang.String, java.lang.Object, int, java.lang.String,
		// // java.lang.String, java.lang.String) : void [28]
		//
		// // field
		// obj = null;
		// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test("Main2",
		// "testStackMapTable", obj, 36, "obj", "object", "field");
		// // obj -> aload_0 [this], getfield...
		//
		// // 58 aload_0 [this]
		// // 59 getfield isFieldOrLocalVariableNullExample.Main2.obj :
		// // java.lang.Object [45]
		//
		// // 62 bipush 36
		// // 64 ldc <String "obj"> [47]
		// // 66 ldc <String "object"> [48]
		// // 68 ldc <String "field"> [50]
		// // 70 invokestatic
		// //
		// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test(java.lang.String,
		// // java.lang.String, java.lang.Object, int, java.lang.String,
		// // java.lang.String, java.lang.String) : void [28]
		//
		// // var private obj
		// privateClass privateClassObject = new privateClass();
		// privateClassObject.privateClassField = null;
		// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test("Main2",
		// "testStackMapTable", privateClassObject.privateClassField, 57,
		// "privateClassObject.privateClassField", "object", "field");
		// // aload 4, getfield !!!!!
		//
		// // 99 aload 4 [privateClassObject]
		// // 101 getfield
		// //
		// isFieldOrLocalVariableNullExample.Main2$privateClass.privateClassField
		// // : java.lang.Object [57]
		//
		// // 104 bipush 57
		// // 106 ldc <String "privateClassObject.privateClassField"> [60]
		// // 108 ldc <String "object"> [48]
		// // 110 ldc <String "field"> [50]
		// // 112 invokestatic
		// //
		// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test(java.lang.String,
		// // java.lang.String, java.lang.Object, int, java.lang.String,
		// // java.lang.String, java.lang.String) : void [28]
		//
		// // field private class
		// thisPrivateClassField = new privateClass();
		// thisPrivateClassField.privateClassField = null;
		// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test("Main2",
		// "testStackMapTable", thisPrivateClassField.privateClassField,
		// 80, "thisPrivateClassField.privateClassField", "object",
		// "field");
		// // aload_0 [this], getfield, getfield
		//
		// // 145 aload_0 [this]
		// // 146 getfield
		// // isFieldOrLocalVariableNullExample.Main2.thisPrivateClassField :
		// // isFieldOrLocalVariableNullExample.Main2.privateClass [62]
		// // 149 getfield
		// //
		// isFieldOrLocalVariableNullExample.Main2$privateClass.privateClassField
		// // : java.lang.Object [57]
		//
		// // 152 bipush 80
		// // 154 ldc <String "thisPrivateClassField.privateClassField"> [64]
		// // 156 ldc <String "object"> [48]
		// // 158 ldc <String "field"> [50]
		// // 160 invokestatic
		// //
		// ch.unibe.scg.nullSpy.runtimeSupporter.NullDisplayer.test(java.lang.String,
		// // java.lang.String, java.lang.Object, int, java.lang.String,
		// // java.lang.String, java.lang.String) : void [28]
		//
		// Main2 m2 = new Main2();
		// m2.obj = null;
	}

	private class PrivateClass {
		public Object privateClassField;

		public PrivateClass() {
			this.privateClassField = new Object();
		}
	}
}
