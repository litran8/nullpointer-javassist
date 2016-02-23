import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyDirectory {
	public static void main(String[] args) {
		File srcFolder = new File(
				"C:\\Users\\Lina Tran\\Desktop\\bachelor\\Javassist\\bin");
		File destFolder = new File(
				"C:\\Users\\Lina Tran\\Desktop\\bachelor\\Empty\\bin");

		File runJavassistSrc = new File(
				"C:\\Users\\Lina Tran\\Desktop\\bachelor\\Javassist\\src\\run");

		File runJavassistSrcDest = new File(
				"C:\\Users\\Lina Tran\\Desktop\\bachelor\\Empty\\src\\run");

		File controllerJavassistSrc = new File(
				"C:\\Users\\Lina Tran\\Desktop\\bachelor\\Javassist\\src\\controller\\Iteration.java");
		File controllerJavassistDest = new File(
				"C:\\Users\\Lina Tran\\Desktop\\bachelor\\Empty\\src\\controller");

		runJavassistSrcDest.mkdir();
		controllerJavassistDest.mkdir();

		controllerJavassistDest = new File(
				"C:\\Users\\Lina Tran\\Desktop\\bachelor\\Empty\\src\\controller\\Iteration.java");

		// make sure source exists
		if (!srcFolder.exists()) {

			System.out.println("Directory does not exist.");
			// just exit
			System.exit(0);

		} else {

			try {
				copyFolder(srcFolder, destFolder);
				copyFolder(runJavassistSrc, runJavassistSrcDest);
				copyFolder(controllerJavassistSrc, controllerJavassistDest);
			} catch (IOException e) {
				e.printStackTrace();
				// error, just exit
				System.exit(0);
			}
		}

		System.out.println("Done");
	}

	public static void copyFolder(File src, File dest) throws IOException {

		if (src.isDirectory()) {

			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
				// System.out.println("Directory copied from " + src + "  to "
				// + dest);
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyFolder(srcFile, destFile);
			}

		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
			// System.out.println("File copied from " + src + " to " + dest);
		}
	}
}
