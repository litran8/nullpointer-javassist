package ch.unibe.scg.nullSpy.run;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import ch.unibe.scg.nullSpy.instrumentator.controller.ClassAdapter;
import ch.unibe.scg.nullSpy.instrumentator.controller.methodInvocation.CsvFileCreator;

public class MainProjectModifier {

	private static String originalProjectBinPath;
	private static String modifiedProjectPath;
	public static String csvPath;
	public static CsvFileCreator csv;

	public static void main(String[] args) throws Throwable {

		if (args.length >= 2) {
			originalProjectBinPath = args[0]; // "C:\Users\Lina
												// Tran\Desktop\bachelor\jhotdraw60b1\bin"
			modifiedProjectPath = args[1]; // "C:\\Users\\Lina Tran\\Desktop\\modifiedProject"
		} else {
			System.out.println("Amount of args is not enough or too big.");
			System.exit(0);
		}

		File srcDir = new File(originalProjectBinPath);
		File modifiedProject = new File(modifiedProjectPath);
		File modifiedProjectRunTimeSupporter = new File(modifiedProjectPath);

		if (!isDirectoryEmpty(modifiedProject)) {
			deleteDirectoryContent(modifiedProject);
		}

		// get path to get the runtimeSupportFile
		String currentWorkingDirPath = new java.io.File(".").getCanonicalPath();
		File nullSpyRuntimeSupporter = new File(currentWorkingDirPath + "\\bin");

		MainProjectModifier.csvPath = modifiedProjectPath + "\\\\VarData.csv";
		csv = new CsvFileCreator(MainProjectModifier.csvPath);

		// make sure source exists
		if (!srcDir.exists()) {

			System.out.println("Directory does not exist.");
			// just exit
			System.exit(0);

		} else {

			try {
				// modifies project by instrumenting code
				modifyProjectAndStoreToDestDir(srcDir, modifiedProject, false);
				csv.closeCsvFile();
				// modifies project by adding runtime supporter class file
				modifyProjectAndStoreToDestDir(nullSpyRuntimeSupporter,
						modifiedProjectRunTimeSupporter, true);
			} catch (IOException e) {
				e.printStackTrace();
				// error, just exit
				System.exit(0);
			}
		}

		System.out.println("Project modification done.");

		// create executable jar out of modified project
		// optional
	}

	private static void deleteDirectoryContent(File file) {
		File[] files = file.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteDirectoryContent(f);
				} else {
					f.delete();
				}
			}
		}
		if (!file.getName().equals("modifiedProject")) {
			file.delete();
		}
	}

	private static boolean isDirectoryEmpty(File file) {
		if (file.isDirectory()) {
			if (file.list().length > 0) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Modifies the classFile of the given project and stores the modified
	 * project in the destination folder.
	 */
	public static void modifyProjectAndStoreToDestDir(File src, File dest,
			boolean isOwnProject) throws NotFoundException,
			FileNotFoundException, IOException {

		// only copy package ch.unibe.scg.nullSpy.runtimeSupporter
		String srcName = src.getName();
		if (isOwnProject
				&& (srcName.equals("run") || srcName.equals("instrumentator")
						|| srcName.equals("model") || srcName.equals("testRun")
						|| srcName.equals("tests")
						|| srcName.equals("isFieldOrLocalVariableNullExample") || srcName
							.equals("timeMeasurement"))) {
			return;
		}

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
	 * Modifies a class file of a project with Javassist, by instrumenting a
	 * check after each assignments to a variable.
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

		try {
			// modify class
			ClassAdapter classAdapter = ClassAdapter.getInstance();
			if (!cc.isInterface()) {
				classAdapter.adaptProject(cc);
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