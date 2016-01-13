import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class JavassistTest {

	public static void main(String[] args) throws Exception {

		ClassPool pool = ClassPool.getDefault();

		CtClass cc = pool.get("HelloWorld");

		CtMethod m = cc.getDeclaredMethod("say");
		m.insertBefore("{ System.out.println(\"Hello.say():\"); }");
		Class c = cc.toClass();
		HelloWorld h = (HelloWorld) c.newInstance();
		h.say();

		System.out.println("test prog:");
	}

}
