package ch.unibe.scg.nullSpy.instrumentator.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CsvFileCreator {

	private static FileWriter writer;
	private final int dataSize = 8;

	public CsvFileCreator(String path) throws IOException {
		writer = new FileWriter(path);
		generateCsvFile();
	}

	public FileWriter getWriter() {
		return this.writer;
	}

	private void generateCsvFile() throws IOException {

		this.writer.append("VarLineNr");
		this.writer.append(",");
		this.writer.append("VarName");
		this.writer.append(",");
		this.writer.append("VarFullName");
		this.writer.append(",");
		this.writer.append("VarType");
		this.writer.append(",");
		this.writer.append("ClassNameInWhichVarIsUsed");
		this.writer.append(",");
		this.writer.append("ClassNameInWhichVarIsInstantiated");
		this.writer.append(",");
		this.writer.append("MethodNameInWhichVarIsUsed");
		this.writer.append(",");
		this.writer.append("MethodSignatureInWhichVarIsUsed");
		this.writer.append("\n");

	}

	public void addCsvLine(ArrayList<String> varData) throws IOException {
		assert (varData.size() == 8);
		int lastElementIndex = varData.size() - 1;

		for (int i = 0; i < varData.size(); i++) {
			this.writer.append(varData.get(i));

			if (i != lastElementIndex) {
				this.writer.append(",");
			} else {
				this.writer.append("\n");
			}
		}
	}

	public static void closeCsvFile() throws IOException {
		writer.flush();
		writer.close();
	}
}
