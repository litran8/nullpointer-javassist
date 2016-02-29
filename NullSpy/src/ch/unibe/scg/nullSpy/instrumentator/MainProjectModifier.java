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

public class MainProjectModifier {

	private static String srcPath;
	private static String destBinDirPath;
	private static String modifiedProjectDestDirPath;
	private static String mainClassNameOfProject;

	public static void main(String[] args) throws NotFoundException,
			IOException {

		// many arguments because i have a space in the path: Lina Tran
		srcPath = args[0] + " " + args[1]; // bin path of the to be modified
											// project
		modifiedProjectDestDirPath = args[2] + " " + args[3]; // path where the
																// modified
																// project
																// should be
																// stored
																// (without bin)
		destBinDirPath = modifiedProjectDestDirPath + "\\bin"; // same as
																// destDirPath,
																// but used for
																// automatically
																// add a bin dir
																// in destDir

		File srcDir = new File(srcPath);
		File destDir = new File(destBinDirPath);

		// get path to get the runtimeSupportFile
		String currentWorkingDirPath = new java.io.File(".").getCanonicalPath();
		File runtimeSupporterFile = new File(currentWorkingDirPath + "\\bin");

		// make sure source exists
		if (!srcDir.exists()) {

			System.out.println("Directory does not exist.");
			// just exit
			System.exit(0);

		} else {

			try {
				modifyProjectAndStoreToDestDir(srcDir, destDir, 0);
				modifyProjectAndStoreToDestDir(runtimeSupporterFile, destDir, 1);
			} catch (IOException e) {
				e.printStackTrace();
				// error, just exit
				System.exit(0);
			}
		}

		System.out.println("Project modification done.");

		ExecutableJarCreator jar = new ExecutableJarCreator();

		jar.jar(destBinDirPath, modifiedProjectDestDirPath,
				mainClassNameOfProject);
	}

	/**
	 * Modifies the classFile of the given project and stores the modified
	 * project in the destination directory.
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 * @throws NotFoundException
	 * @throws FileNotFoundException
	 */
	public static void modifyProjectAndStoreToDestDir(File src, File dest,
			int modifyID) throws NotFoundException, FileNotFoundException,
			IOException {

		// only copy package ch.unibe.scg.nullSpy.runtimeSupporter
		if (src.getName().equals("instrumentator") && modifyID == 1)
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
				modifyProjectAndStoreToDestDir(srcFile, destFile, modifyID);
			}
		} else {

			switch (modifyID) {
			case 0:
				modifyProject(src, dest);
				break;
			case 1:
				addRuntimeSupporterToModifiedProject(src, dest);
				break;
			}

		}
	}

	/**
	 * Modifies all class files of a project with Javassist.
	 * 
	 * @param src
	 * @param dest
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static void modifyProject(File src, File dest)
			throws NotFoundException, IOException, FileNotFoundException {
		// set up the search path of class pool
		ClassPool pool = ClassPool.getDefault();
		pool.insertClassPath(srcPath);

		// create ctclass to represent the to be modified class
		String packageName_ClassName = src
				.getAbsolutePath()
				.substring(srcPath.length() + 1, src.getAbsolutePath().length())
				.replace(".class", "").replace("\\", ".");

		CtClass cc = pool.get(packageName_ClassName);
		cc.stopPruning(true);

		// get the main-className for manifest of executable jar
		for (CtMethod m : cc.getDeclaredMethods()) {
			if (m.getName().equals("main"))
				mainClassNameOfProject = m.getDeclaringClass().getName();
		}

		try {
			// modify class
			Iteration iter = Iteration.getInstance();
			iter.instrumentCodeAfterFieldLocVarAssignment(cc);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// write modified project to the destination
		ClassFile classFile = cc.getClassFile();
		classFile.write(new DataOutputStream(new FileOutputStream(dest
				.getAbsolutePath())));
	}

	/**
	 * Adds the run-time supporter class file to the bin directory of the
	 * modified project in the destination directory.
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 * @throws NotFoundException
	 */
	private static void addRuntimeSupporterToModifiedProject(File src, File dest)
			throws FileNotFoundException, IOException {
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