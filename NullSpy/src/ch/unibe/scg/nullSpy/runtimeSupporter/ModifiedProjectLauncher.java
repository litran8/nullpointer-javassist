package ch.unibe.scg.nullSpy.runtimeSupporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ModifiedProjectLauncher {

	public static void main(String[] args) throws IOException,
			InterruptedException {
		String modifiedProjectJarPath = "\"" + args[0] + "\"";
		String mainClassName = args[1];

		String cmdLine = "java -cp " + modifiedProjectJarPath + " "
				+ mainClassName;

		Runtime runtime = Runtime.getRuntime();

		Process p = runtime.exec(cmdLine);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		BufferedReader readerError = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));
		p.waitFor();

		while (reader.ready())
			System.out.println(reader.readLine());

		while (readerError.ready()) {
			System.out.println(readerError.readLine());
		}
	}
}
