package org.amshove.natlint.api;

import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;

public interface IAnalyzeContext
{
	INaturalModule getModule();

	void report(LinterDiagnostic diagnostic);

	/**
	 * Returns the .editorconfig setting for the given property matching the files path
	 * otherwise returns given defaultValue.
	 */
	String getConfiguration(NaturalFile forFile, String property, String defaultValue);
}
