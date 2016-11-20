package ch.unibe.scg.nullSpy.run;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class ExecutableJarCreator {

	private static String packageName;
	private static String modifiedProjectBinSrcPath;

	public static void main(String[] args) {
		String srcPath = args[0]; // "C:\\\\Users\\\\Lina Tran\\\\Desktop\\\\modifiedProject"
		String jarDestPath = args[1]; // "C:\\\\Users\\\\Lina Tran\\\\Desktop\\\\modifiedProject"
		String mainClassName = args[2]; // "testExample.Ideone"
		createExecJar(srcPath, jarDestPath, mainClassName);
	}

	private static void createExecJar(String modifiedProjectBinSrcPath,
			String jarDestPath, String mainClassName) {

		// JarCreator.modifiedProjectBinSrcPath = modifiedProjectBinSrcPath;

		File modifiedProject = new File(modifiedProjectBinSrcPath);
		File jarDest = new File(jarDestPath);

		try {
			// create manifest for executable jar
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,
					"1.0");
			manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,
					mainClassName);

			JarOutputStream target = new JarOutputStream(new FileOutputStream(
					jarDestPath + "\\test.jar"), manifest);

			srcToJar(modifiedProject, jarDest, target);
			target.close();

		} catch (IOException e) {
			e.printStackTrace();
			// error, just exit
			System.exit(0);
		}

		System.out.println("Eexecutable jar creation done.");
	}

	/**
	 * Takes the modifiedProject and create an executable jar with it.
	 * 
	 * @param src
	 * @param dest
	 * @param target
	 * @throws IOException
	 */
	private static void srcToJar(File src, File dest, JarOutputStream target)
			throws IOException {

		if (src.isDirectory()) {

			String dirName = src.getName();
			if (dirName.equals("model") || dirName.equals("testRun")
					|| dirName.equals("tests") || dirName.equals("test.jar")) {
				return;
			}
			// to make sub-folders in jar
			if (!src.getName().equals("bin")
					&& !src.getName().equals("modifiedProject")) {
				if (packageName == null)
					packageName = (src.getName() + "/");
				else
					packageName += (src.getName() + "/");
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {

				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);

				// recursive copy
				srcToJar(srcFile, destFile, target);

				// only if package is changed; for sub-folders in jar
				if (srcFile.isDirectory()) {
					setPackageNameToParentPackageName();
				}

			}
		} else {
			// if file, then copy it
			InputStream in = new FileInputStream(src);

			if (src.getParent().equals(modifiedProjectBinSrcPath))
				packageName = "";

			target.putNextEntry(new JarEntry(packageName + src.getName()));

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				target.write(buffer, 0, length);
			}

			in.close();
			target.closeEntry();
		}
	}

	private static void setPackageNameToParentPackageName() {
		packageName = packageName.substring(0, packageName.lastIndexOf("/"));
		packageName = packageName
				.substring(0, packageName.lastIndexOf("/") + 1);

	}
}
