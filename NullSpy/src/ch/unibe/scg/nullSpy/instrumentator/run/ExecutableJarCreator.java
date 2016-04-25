package ch.unibe.scg.nullSpy.instrumentator.run;

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

	public void createExecJar(String modifiedProjectBinSrcPath,
			String jarDestPath, String mainClassName) {

		File modifiedProject = new File(modifiedProjectBinSrcPath);

		try {
			// create manifest for executable jar
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,
					"1.0");
			// manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,
			// mainClassName);
			//
			// JarOutputStream target = new JarOutputStream(new
			// FileOutputStream(
			// jarDestPath + "\\" + mainClassName + ".jar"), manifest);

			// manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,
			// "org.jhotdraw.samples.javadraw.JavaDrawApp");
			// manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,
			// "org.jhotdraw.samples.net.NetApp");
			manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,
					"org.jhotdraw.samples.nothing.NothingApp");

			JarOutputStream target = new JarOutputStream(new FileOutputStream(
					jarDestPath + "\\Main.jar"), manifest);

			srcToJar(modifiedProject, "", target);
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
	public void srcToJar(File src, String packagePath, JarOutputStream target)
			throws IOException {

		if (src.isDirectory()) {

			String dirName = src.getName();

			// to make sub-folders in jar
			if (!dirName.equals("bin"))
				packagePath += (dirName + "/");

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {

				// construct the src and dest file structure
				File srcFile = new File(src, file);

				// recursive copy
				srcToJar(srcFile, packagePath, target);
			}
		} else {
			// if file, then copy it
			InputStream in = new FileInputStream(src);

			System.out.println(packagePath + src.getName());

			target.putNextEntry(new JarEntry(packagePath + src.getName()));

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
}
