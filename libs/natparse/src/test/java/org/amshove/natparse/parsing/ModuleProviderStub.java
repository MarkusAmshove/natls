package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.INaturalModule;

import java.util.HashMap;
import java.util.Map;

public class ModuleProviderStub implements IModuleProvider
{
	private final Map<String, INaturalModule> referableModules = new HashMap<>();

	public ModuleProviderStub addModule(String referableName, INaturalModule module)
	{
		referableModules.put(referableName, module);
		return this;
	}

	@Override
	public INaturalModule findNaturalModule(String referableName)
	{
		return referableModules.get(referableName);
	}
}
