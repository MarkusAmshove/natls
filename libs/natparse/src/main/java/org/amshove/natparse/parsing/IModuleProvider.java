package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ddm.IDataDefinitionModule;
import org.amshove.natparse.natural.project.NaturalFileType;

import javax.annotation.Nullable;

public interface IModuleProvider
{
	// TODO: Deprecated?
	INaturalModule findNaturalModule(String referableName);

	INaturalModule findNaturalModule(String referableName, @Nullable NaturalFileType requestedType);

	IDataDefinitionModule findDdm(String referableName);
}
