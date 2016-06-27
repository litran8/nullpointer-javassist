package ch.unibe.scg.nullSpy.runtimeSupporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class DataMatcher {

	public static void printLocationOnMatch(String csvPath,
			HashMap<LocalVarKey, LocalVariable> localVarMap,
			HashMap<FieldKey, Field> fieldMap) throws FileNotFoundException {

		System.out.println("It works!");

		String receiverEntry;
		Scanner fileScan, receiverScan;

		fileScan = new Scanner(new File(csvPath));

		while (fileScan.hasNext()) {
			receiverEntry = fileScan.nextLine();
			// System.out.println("ReceiverEntry: " + receiverEntry);

			receiverScan = new Scanner(receiverEntry);
			receiverScan.useDelimiter(",");

			while (receiverScan.hasNext()) {
				// System.out.println("\t" + receiverScan.next());
			}

			System.out.println("");
		}

		fileScan.close();
	}

}
