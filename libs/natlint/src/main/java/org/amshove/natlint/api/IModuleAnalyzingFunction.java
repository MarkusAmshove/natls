package org.amshove.natlint.api;

import org.amshove.natparse.natural.INaturalModule;

@FunctionalInterface
public interface IModuleAnalyzingFunction
{
	void analyze(INaturalModule module, IAnalyzeContext context);
}
