package ch.unibe.scg.nullSpy.instrumentator.model;

public class PcLine {
	int pc;
	int line;

	public PcLine(int pc, int line) {
		this.pc = pc;
		this.line = line;
	}

	public int getPc() {
		return pc;
	}

	public int getLine() {
		return line;
	}

	public String toString() {
		String res = "Pc: " + pc + ", Line: " + line;
		return res;
	}
}
