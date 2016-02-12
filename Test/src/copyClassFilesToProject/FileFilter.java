package copyClassFilesToProject;

import java.io.File;

public class FileFilter {

	private static boolean findFolder = false;
	private static String path = null;
	private static String dirName = "ProjectToEnterPackage";

	FileFilter() {
		File[] driveList = File.listRoots();
		outer: for (File drives : driveList) {
			// System.out.println(drives);
			File[] dirsInDrive = drives.listFiles();
			for (File dir : dirsInDrive) {
				if (!findFolder) {
					findMyFolder(dir);
				} else {
					break outer;
				}
			}
		}
	}

	public static void main(String[] args) {
		new FileFilter();
		System.out.println(path);
	}

	public void findMyFolder(File fileObject) {
		if (fileObject.isDirectory()) {
			String dirPath = fileObject.getAbsolutePath();
			// System.out.println(dirPath.substring(dirPath.lastIndexOf(System
			// .getProperty("file.separator")) + 1));
			if (dirPath
					.substring(
							dirPath.lastIndexOf(System
									.getProperty("file.separator")) + 1)
					.equals(dirName)) {
				System.out.println("OK");
				findFolder = true;
				path = fileObject.getAbsolutePath();
				return;
			}

			File allFiles[] = fileObject.listFiles();
			if (allFiles != null && allFiles.length != 0) {
				for (File aFile : allFiles) {
					if (!findFolder)
						findMyFolder(aFile);
				}
			}

		}
	}
}
