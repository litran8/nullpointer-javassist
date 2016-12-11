package ch.unibe.scg.nullSpy.instrumentor.controller.methodInvocation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CsvFileCreator {

	private FileWriter writer;
	private final int dataSize = 11;

	public CsvFileCreator(String path) throws IOException {
		writer = new FileWriter(path);
		generateCsvFile();
	}

	private void generateCsvFile() throws IOException {
		// FIXME: adapt table
		this.writer.append("Nr");
		this.writer.append(",");
		this.writer.append("LineNr");
		this.writer.append(",");
		this.writer.append("VarID");
		this.writer.append(",");
		this.writer.append("VarName");
		this.writer.append(",");
		this.writer.append("VarType");
		this.writer.append(",");
		this.writer.append("IsStatic");
		this.writer.append(",");
		this.writer.append("ClassWhereVarIsUsed");
		this.writer.append(",");
		this.writer.append("Behavior");
		this.writer.append(",");
		this.writer.append("BehaviorSignature");
		this.writer.append(",");

		// localVar
		this.writer.append("LocalVarAttrIndex");
		this.writer.append(",");

		// field
		this.writer.append("FieldDeclaringClassName");
		this.writer.append("\n");

	}

	public void addCsvLine(ArrayList<String> varData) throws IOException {
		assert (varData.size() == dataSize);
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

	public void closeCsvFile() throws IOException {
		writer.flush();
		writer.close();
	}
}
