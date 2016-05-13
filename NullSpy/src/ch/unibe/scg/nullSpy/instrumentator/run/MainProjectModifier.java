package ch.unibe.scg.nullSpy.instrumentator.run;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import ch.unibe.scg.nullSpy.instrumentator.controller.ClassAdapter;

public class MainProjectModifier {

	private static String originalProjectBinPath;
	private static String modifiedProjectDestDirPath;
	private static String mainClassNameOfProject;

	public static void main(String[] args) throws NotFoundException,
			IOException {

		if (args.length == 2) {
			originalProjectBinPath = args[0];
			modifiedProjectDestDirPath = args[1];
		} else {
			System.out.println("Amount of args is not enough or too big.");
			System.exit(0);
		}

		// String modifiedProjectDestPath = modifiedProjectDestDirPath +
		// "\\org"; // same
		// as
		// destDirPath,
		// but used for
		// automatically
		// add a bin dir
		// in destDir
		String modifiedProjectDestPath = modifiedProjectDestDirPath;

		File srcDir = new File(originalProjectBinPath);
		File modifiedProjectDestDir = new File(modifiedProjectDestPath);
		File runTimeSupporterDestDir = new File(modifiedProjectDestDirPath);

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
				// modifies project by instrument code
				modifyProjectAndStoreToDestDir(srcDir, modifiedProjectDestDir,
						false);
				// modifies project by adding runtime supporter class file
				modifyProjectAndStoreToDestDir(runtimeSupporterFile,
						runTimeSupporterDestDir, true);
			} catch (IOException e) {
				e.printStackTrace();
				// error, just exit
				System.exit(0);
			}
		}

		System.out.println("Project modification done.");

		// create executable jar out of modified project
		// ExecutableJarCreator jar = new ExecutableJarCreator();
		// jar.createExecJar(modifiedProjectDestBinDirPath,
		// modifiedProjectDestDirPath, mainClassNameOfProject);
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
			boolean isOwnProject) throws NotFoundException,
			FileNotFoundException, IOException {

		// only copy package ch.unibe.scg.nullSpy.runtimeSupporter
		String srcName = src.getName();
		if (isOwnProject
				&& (srcName.equals("instrumentator") || srcName.equals("model")
						|| srcName.equals("testRun") || srcName.equals("tests") || srcName
							.equals("isFieldOrLocalVariableNullExample")))
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
				modifyProjectAndStoreToDestDir(srcFile, destFile, isOwnProject);
			}
		} else {

			// if file is a class file and not a file of own project, modify and
			// store to destination folder
			// else just copy it to destination folder
			boolean isClassFile = src.getAbsolutePath().endsWith(".class");

			if (isClassFile && !isOwnProject) {
				analyzeFileAndStoreToDestinationFolder(src, dest);
			} else {
				Files.copy(src.toPath(), dest.toPath());
			}
		}
	}

	/**
	 * Modify a class file of a project with Javassist, by instrumenting a check
	 * after each assignments of fields and locVars.
	 * 
	 * @param src
	 * @param dest
	 *            modified class file is stored here
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static void analyzeFileAndStoreToDestinationFolder(File src,
			File dest) throws NotFoundException, IOException,
			FileNotFoundException {
		// set up the search path of class pool
		ClassPool pool = ClassPool.getDefault();
		pool.insertClassPath(originalProjectBinPath);

		// create ctclass to represent the to be modified class
		String packageName_ClassName = src
				.getAbsolutePath()
				.substring(originalProjectBinPath.length() + 1,
						src.getAbsolutePath().length()).replace(".class", "")
				.replace("\\", ".");

		CtClass cc = pool.get(packageName_ClassName);
		cc.stopPruning(true);

		// get the main-className for manifest of executable jar
		for (CtMethod m : cc.getDeclaredMethods()) {
			if (m.getName().equals("main")) {
				mainClassNameOfProject = m.getDeclaringClass().getName();

			}

		}

		try {
			// modify class
			ClassAdapter classAdapter = ClassAdapter.getInstance();
			// if (!cc.isInterface()
			// && cc.getName()
			// .equals("org.jhotdraw.contrib.html.DisposableResourceManagerFactory"))
			// {
			if (!cc.isInterface()) {
				classAdapter.instrumentCodeAfterFieldLocVarAssignment(cc);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// write modified project to the destination
		ClassFile classFile = cc.getClassFile();
		classFile.write(new DataOutputStream(new FileOutputStream(dest
				.getAbsolutePath())));
	}

}