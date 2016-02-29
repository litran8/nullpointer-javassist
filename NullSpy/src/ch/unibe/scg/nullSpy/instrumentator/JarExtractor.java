package ch.unibe.scg.nullSpy.instrumentator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarExtractor {

	public static void main(String[] args) throws java.io.IOException {
		JarFile jarfile = new JarFile(new File("E:/sqljdbc4.jar")); // jar
																	// file
																	// path(here
																	// sqljdbc4.jar)
		Enumeration<JarEntry> enu = jarfile.entries();
		while (enu.hasMoreElements()) {
			String destdir = "E:/abc/"; // abc is my destination directory
			java.util.jar.JarEntry je = enu.nextElement();

			System.out.println(je.getName());

			File fl = new java.io.File(destdir, je.getName());
			if (!fl.exists()) {
				fl.getParentFile().mkdirs();
				fl = new java.io.File(destdir, je.getName());
			}
			if (je.isDirectory()) {
				continue;
			}
			InputStream is = jarfile.getInputStream(je);
			FileOutputStream fo = new java.io.FileOutputStream(fl);

			while (is.available() > 0) {
				fo.write(is.read());
			}
			fo.close();
			is.close();
		}

	}

}
