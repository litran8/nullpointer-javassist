import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

public class JavassistTest {

	public static void main(String[] args) throws Exception {

		ClassPool pool = ClassPool.getDefault();

		CtClass cc = pool.get("HelloWorld");

		// add new method in HelloWorld.java
		CtMethod cm = CtNewMethod.make(
				"public void a(){System.out.println(\"Hi\");}", cc);
		cc.addMethod(cm);

		CtMethod m = cc.getDeclaredMethod("say");
		m.insertBefore("{ System.out.println(\"Hello.say():\");a(); }");

		Class c = cc.toClass();
		HelloWorld h = (HelloWorld) c.newInstance();
		h.say();

		System.out.println("test prog:");
	}

}
