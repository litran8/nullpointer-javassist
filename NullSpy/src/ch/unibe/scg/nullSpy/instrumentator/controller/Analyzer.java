package ch.unibe.scg.nullSpy.instrumentator.controller;

import javassist.CtClass;

public abstract class Analyzer {
	protected CtClass cc;

	public Analyzer(CtClass cc) {
		this.cc = cc;
	}

	public abstract void searchAndAdapt(ByteCodeAdapter byteCodeAdaptor);
}