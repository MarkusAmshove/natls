package org.amshove.natls.codelens;

import org.amshove.natls.config.IConfigChangedSubscriber;
import org.amshove.natls.config.LSConfiguration;
import org.amshove.natls.project.LanguageServerFile;
import org.eclipse.lsp4j.CodeLens;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CodeLensService implements IConfigChangedSubscriber
{
	private final Set<ICodeLensProvider> registeredProviders = Collections.synchronizedSet(new HashSet<>());

	public CodeLensService(LSConfiguration config)
	{
		registeredProviders.add(new ModuleReferencesCodeLens());
		registeredProviders.add(new InternalSubroutineReferencesCodeLens());
		registeredProviders.add(new NatUnitCodeLensProvider());
		if (config.getMaps() != null && config.getMaps().isPreviewEnabled())
		{
			registeredProviders.add(new InputPreviewCodeLensProvider());
		}
	}

	public List<CodeLens> provideCodeLens(LanguageServerFile file)
	{
		return registeredProviders
			.stream()
			.flatMap(p -> p.provideCodeLens(file).stream())
			.toList();
	}

	@Override
	public void configChanged(LSConfiguration newConfig)
	{
		var previewProvider = registeredProviders.stream().filter(p -> p instanceof InputPreviewCodeLensProvider).findAny();
		if (newConfig.getMaps().isPreviewEnabled() && previewProvider.isEmpty())
		{
			registeredProviders.add(new InputPreviewCodeLensProvider());
		}

		if (previewProvider.isPresent() && !newConfig.getMaps().isPreviewEnabled())
		{
			registeredProviders.remove(previewProvider.get());
		}
	}
}
