package ch.unibe.scg.nullSpy.instrumentator;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import ch.unibe.scg.nullSpy.instrumentator.controller.Iteration;

public class LoadAndModifyProject {

	private static String projectBinSourcePath;
	private static String modifiedProjectDestDirWithoutBinPath;
	private static String jarDestPath;
	private static String mainClassNameOfProject;

	public static void main(String[] args) throws NotFoundException,
			IOException {

		// many arguments because i have a space in the path: Lina Tran
		projectBinSourcePath = args[0] + " " + args[1];
		jarDestPath = args[2] + " " + args[3];
		modifiedProjectDestDirWithoutBinPath = jarDestPath + "\\bin";

		File srcFolder = new File(projectBinSourcePath);
		File destFolder = new File(modifiedProjectDestDirWithoutBinPath);

		// get path to get the runtimeSupportFile
		String currentWorkingDirPath = new java.io.File(".").getCanonicalPath();
		File runtimeSupporterFile = new File(currentWorkingDirPath + "\\bin");

		// make sure source exists
		if (!srcFolder.exists()) {

			System.out.println("Directory does not exist.");
			// just exit
			System.exit(0);

		} else {

			try {

				modifyProjectAndStoreToDestFolder(srcFolder, destFolder);
				copyRuntimeSupporterClassFileToModifiedProjectFolder(
						runtimeSupporterFile, destFolder);
			} catch (IOException e) {
				e.printStackTrace();
				// error, just exit
				System.exit(0);
			}
		}

		System.out.println("Done");

		ExecutableJarCreator jar = new ExecutableJarCreator();

		jar.jar(modifiedProjectDestDirWithoutBinPath, jarDestPath,
				mainClassNameOfProject);
	}

	/**
	 * Modifies the classFile of the given project and stores the modified
	 * project in the destination folder.
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 * @throws NotFoundException
	 * @throws FileNotFoundException
	 */
	public static void modifyProjectAndStoreToDestFolder(File src, File dest)
			throws NotFoundException, FileNotFoundException, IOException {

		if (src.isDirectory()) {

			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				modifyProjectAndStoreToDestFolder(srcFile, destFile);
			}
		} else {
			// set up the search path of the class pool for modifications
			ClassPool pool = ClassPool.getDefault();
			pool.insertClassPath(projectBinSourcePath);

			// create ctclass to represent the to be modified class
			String packageName_ClassName = src
					.getAbsolutePath()
					.substring(projectBinSourcePath.length() + 1,
							src.getAbsolutePath().length()).replace("\\", ".")
					.replace(".class", "");

			CtClass cc = pool.get(packageName_ClassName);
			cc.stopPruning(true);

			// get the main-className
			for (CtMethod m : cc.getDeclaredMethods()) {
				if (m.getName().equals("main"))
					mainClassNameOfProject = m.getDeclaringClass().getName();
			}

			try {
				// modify class
				Iteration iter = Iteration.getInstance();
				iter.goThrough(cc);
			} catch (Throwable e) {
				e.printStackTrace();
			}

			// write modified project to the destination
			ClassFile classFile = cc.getClassFile();
			classFile.write(new DataOutputStream(new FileOutputStream(dest
					.getAbsolutePath())));
		}
	}

	/**
	 * Copies the run-time supporter class file to the bin folder in the
	 * destination folder of the modified project.
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 * @throws NotFoundException
	 */
	public static void copyRuntimeSupporterClassFileToModifiedProjectFolder(
			File src, File dest) throws IOException, NotFoundException {

		// only copy package ch.unibe.scg.nullSpy.runtimeSupporter
		if (src.getName().equals("instrumentator"))
			return;

		if (src.isDirectory()) {

			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyRuntimeSupporterClassFileToModifiedProjectFolder(srcFile,
						destFile);
			}

		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest.getAbsolutePath());

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
		}
	}
}