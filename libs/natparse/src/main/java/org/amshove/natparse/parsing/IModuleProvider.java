package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.INaturalModule;

public interface IModuleProvider
{
	INaturalModule findNaturalModule(String referableName);
}
