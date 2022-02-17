package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ddm.IDataDefinitionModule;

public interface IModuleProvider
{
	INaturalModule findNaturalModule(String referableName);

	IDataDefinitionModule findDdm(String referableName);
}
