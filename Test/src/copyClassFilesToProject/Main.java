package copyClassFilesToProject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javassist.NotFoundException;

public class Main {
	public static void main(String[] args) throws ClassNotFoundException,
			IOException, NotFoundException {

		File file = new File("");
		String path = file.getParent();
		String parent = file.getAbsolutePath();
		System.out.println(parent);
		// System.out.println(path.indexOf("\\"));

		File folder = new File("C:/Users/Lina Tran/Desktop/bachelor");
		File[] listOfFiles = folder.listFiles();
		System.out.println(listOfFiles[2]);

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				System.out.println("File " + listOfFiles[i].getName());
			} else if (listOfFiles[i].isDirectory()) {
				System.out.println("Directory " + listOfFiles[i].getName());
			}
		}

		List<String> classes = ClassFinder
				.getClassesFromPackage("javassistPackage");
		System.out.println(classes.size());

		for (String c : classes) {
			// System.out.println(c);

			// String classPathToBeAdded = "ProjectToEnterPackage/bin/"
			// + c.replaceAll("\\.", "/") + ".class";
			//
			// String destination = "bin/isFieldOrLocalVariableNull/"
			// + c.substring(c.indexOf(".") + 1, c.length()) + ".class";
			//
			// BufferedInputStream fin = new BufferedInputStream(
			// new FileInputStream(classPathToBeAdded));
			//
			// ClassFile cf = new ClassFile(new DataInputStream(fin));
			//
			// cf.write(new DataOutputStream(new
			// FileOutputStream(destination)));
			//
		}
	}
}
