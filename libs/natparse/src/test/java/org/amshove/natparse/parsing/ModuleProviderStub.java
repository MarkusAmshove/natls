package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ddm.IDataDefinitionModule;
import org.amshove.natparse.natural.project.NaturalFileType;

import java.util.HashMap;
import java.util.Map;

public class ModuleProviderStub implements IModuleProvider
{
	private final Map<String, INaturalModule> referableModules = new HashMap<>();
	private final Map<String, IDataDefinitionModule> ddms = new HashMap<>();

	public ModuleProviderStub addModule(String referableName, INaturalModule module)
	{
		referableModules.put(referableName, module);
		return this;
	}

	@Override
	public INaturalModule findNaturalModule(String referableName, NaturalFileType requestedType)
	{
		return referableModules.get(referableName);
	}

	@Override
	public IDataDefinitionModule findDdm(String referableName)
	{
		return ddms.get(referableName);
	}

	public void addDdm(String ddmName, IDataDefinitionModule ddm)
	{
		ddms.put(ddmName, ddm);
	}
}
