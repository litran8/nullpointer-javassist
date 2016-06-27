package ch.unibe.scg.nullSpy.tests;

import java.io.IOException;
import java.util.ArrayList;

import ch.unibe.scg.nullSpy.instrumentator.controller.methodInvocation.CsvFileCreator;

public class CsvTester {

	private static CsvFileCreator csv;

	public static void main(String[] args) throws IOException {
		String path = "C:\\Users\\Lina Tran\\Desktop\\blub.csv";
		csv = new CsvFileCreator(path);

		ArrayList<String> varData = new ArrayList<String>();
		varData.add("8");
		varData.add("Lina");
		varData.add("Lina Tran");
		varData.add("Female");
		varData.add("classUsed");
		varData.add("classInstantiated");
		varData.add("method");
		varData.add("methodSign");

		csv.addCsvLine(varData);

		csv.closeCsvFile();
	}
}
