package copyClassFilesToProject;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

public class ClassFinder {

	/**
	 * Attempts to list all the classes in the specified package as determined *
	 * by the context class loader…
	 * 
	 * @param pckgname
	 *            the package name to search
	 * @return a list of classes that exist within that package
	 * @throws ClassNotFoundException
	 *             if something went wrong
	 */
	public static List<String> getClassesFromPackage(String pckgname)
			throws ClassNotFoundException {

		// ArrayList<Class> result = new ArrayList<Class>();
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<File> directories = new ArrayList<File>();

		HashMap<File, String> packageNames = null;

		try {

			ClassLoader cld = Thread.currentThread().getContextClassLoader();

			if (cld == null) {
				throw new ClassNotFoundException("Can't get class loader.");
			}

			String path = pckgname;
			Enumeration<URL> resources = cld.getResources(path);
			File directory = null;

			while (resources.hasMoreElements()) {
				String path2 = resources.nextElement().getPath();
				directory = new File(URLDecoder.decode(path2, "UTF-8"));
				directories.add(directory);
			}

			if (packageNames == null) {
				packageNames = new HashMap<File, String>();
			}

			packageNames.put(directory, pckgname);

		} catch (NullPointerException x) {
			throw new ClassNotFoundException(
					pckgname
							+ " does not appear to be a valid package (Null pointer exception)");
		} catch (UnsupportedEncodingException encex) {
			throw new ClassNotFoundException(
					pckgname
							+ " does not appear to be a valid package (Unsupported encoding)");
		} catch (IOException ioex) {
			throw new ClassNotFoundException(
					"IOException was thrown when trying to get all resources for "
							+ pckgname);
		}

		for (File directory : directories) {
			if (directory.exists()) {
				String[] files = directory.list();
				for (String file : files) {
					if (file.endsWith(".class")) {
						try {
							result.add(packageNames.get(directory).toString()
									+ '.'
									+ file.substring(0, file.length() - 6));
						} catch (Throwable e) {
						}
					}
				}
			} else {
				throw new ClassNotFoundException(pckgname + " ("
						+ directory.getPath()
						+ ") does not appear to be a valid package");
			}
		}
		return result;
	}

}