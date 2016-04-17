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

	private String packageName;
	private String modifiedProjectBinSrcPath;

	public void createExecJar(String modifiedProjectBinSrcPath,
			String jarDestPath, String mainClassName) {

		this.modifiedProjectBinSrcPath = modifiedProjectBinSrcPath;

		File modifiedProject = new File(this.modifiedProjectBinSrcPath);
		File jarDest = new File(jarDestPath);

		try {
			// create manifest for executable jar
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,
					"1.0");
			manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,
					mainClassName);

			JarOutputStream target = new JarOutputStream(new FileOutputStream(
					jarDestPath + "\\" + mainClassName + ".jar"), manifest);

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
	public void srcToJar(File src, File dest, JarOutputStream target)
			throws IOException {

		if (src.isDirectory()) {

			String dirName = src.getName();
			if (dirName.equals("model") || dirName.equals("testRun")
					|| dirName.equals("tests")) {
				return;
			}
			// to make sub-folders in jar
			if (!src.getName().equals("bin")) {
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
				if (srcFile.isDirectory())
					packageName = null;
			}
		} else {
			// if file, then copy it
			InputStream in = new FileInputStream(src);

			if (src.getParent().equals(this.modifiedProjectBinSrcPath))
				packageName = "";

			System.out.println(src.getName());

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
}
