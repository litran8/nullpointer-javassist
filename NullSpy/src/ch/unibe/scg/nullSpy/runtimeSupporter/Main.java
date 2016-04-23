package ch.unibe.scg.nullSpy.runtimeSupporter;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

	private static ArrayList<Class<?>> mainClassList;
	private String[] arv = null;

	public static void main(String[] args) {
		HashMap<String, Integer> classIntegerMap = new HashMap<>();
		int i = 1;
		for (Class<?> mainClass : mainClassList) {
			String mainClassName = mainClass.getName();
			if (mainClassName.equals(args[0])) {

			}
			// classIntegerMap.put(mainClass, i++);
		}

		// for (String className : classIntegerMap.keySet()) {
		// if (className.equals(args[0])) {
		//
		// }
		// }

	}

	public static void setMainClassList(ArrayList<Class<?>> mainClassList) {
		Main.mainClassList = mainClassList;
	}

}
