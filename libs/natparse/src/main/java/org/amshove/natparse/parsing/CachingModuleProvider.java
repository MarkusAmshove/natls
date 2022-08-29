package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ddm.IDataDefinitionModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalLibrary;
import org.amshove.natparse.parsing.ddm.DdmParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class CachingModuleProvider implements IModuleProvider
{
	private static final Map<NaturalLibrary, Map<String, ParsedModule>> libToReferableNameToFileCache = new ConcurrentHashMap<>();
	private final NaturalFile caller;

	CachingModuleProvider(NaturalFile caller)
	{
		this.caller = caller;
	}

	@Override
	public IDataDefinitionModule findDdm(String referableName)
	{
		var calledFile = caller.getLibrary().findFileByReferableName(referableName, true);
		if(calledFile == null)
		{
			return null;
		}

		try
		{
			return new DdmParser().parseDdm(Files.readString(calledFile.getPath()));
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public INaturalModule findNaturalModule(String referableName)
	{
		if (referableName.startsWith("USR") && referableName.endsWith("N"))
		{
			return null; // built-in user exits
		}

		var callerLib = caller.getLibrary();
		var referableToModule = libToReferableNameToFileCache.computeIfAbsent(callerLib, (l) -> new ConcurrentHashMap<>());

		var foundModule = referableToModule.computeIfAbsent(referableName, n -> new ParsedModule(callerLib.findFileByReferableName(referableName, true), null));
		if (foundModule.file == null)
		{
			return null;
		}

		if (foundModule.module != null)
		{
			return foundModule.module;
		}

		try
		{
			// Parsing only the DEFINE DATA should be enough for everything except COPYCODEs
			// If we'd parse more, we would have to handle cyclomatic dependencies
			var source = Files.readString(foundModule.file.getPath());
			var tokens = new Lexer().lex(source, foundModule.file.getPath());
			var result = new DefineDataParser(this).parse(tokens);
			var module = new NaturalModule(foundModule.file);
			module.setDefineData(result.result());
			if(shouldCache(foundModule.file))
			{
				referableToModule.put(referableName, new ParsedModule(foundModule.file, module));
			}
			return module;
		}
		catch (Exception e)
		{
			return null; // Not found
		}
	}

	private boolean shouldCache(NaturalFile file)
	{
		return switch (file.getFiletype())
			{
				default -> false;
			};
	}

	private record ParsedModule(NaturalFile file, INaturalModule module)
	{
	}
}
