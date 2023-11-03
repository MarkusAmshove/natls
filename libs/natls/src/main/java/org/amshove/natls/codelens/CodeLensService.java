package org.amshove.natls.codelens;

import org.amshove.natls.project.LanguageServerFile;
import org.eclipse.lsp4j.CodeLens;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CodeLensService
{
	private final Set<ICodeLensProvider> registeredProviders = Collections.synchronizedSet(new HashSet<>());

	public CodeLensService()
	{
		registeredProviders.add(new ModuleReferencesCodeLens());
		registeredProviders.add(new InternalSubroutineReferencesCodeLens());
		registeredProviders.add(new NatUnitCodeLensProvider());
	}

	public List<CodeLens> provideCodeLens(LanguageServerFile file)
	{
		return registeredProviders
			.stream()
			.flatMap(p -> p.provideCodeLens(file).stream())
			.toList();
	}
}
