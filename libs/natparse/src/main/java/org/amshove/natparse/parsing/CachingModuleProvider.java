package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalLibrary;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

class CachingModuleProvider implements IModuleProvider
{
	private static Map<NaturalLibrary, Map<String, ParsedModule>> libToReferableNameToFileCache = new HashMap<>();
	private final NaturalFile caller;

	CachingModuleProvider(NaturalFile caller)
	{
		this.caller = caller;
	}

	@Override
	public INaturalModule findNaturalModule(String referableName)
	{
		var callerLib = caller.getLibrary();
		var referableToModule = libToReferableNameToFileCache.computeIfAbsent(callerLib, (l) -> new HashMap<>());

		var foundModule = referableToModule.computeIfAbsent(referableName, n -> new ParsedModule(callerLib.findFileByReferableName(referableName, true), null));
		if(foundModule.file == null)
		{
			return null;
		}

		if(foundModule.module != null)
		{
			return foundModule.module;
		}

		try
		{
			var source = Files.readString(foundModule.file.getPath());
			var tokens = new Lexer().lex(source, foundModule.file.getPath());
			var module = new NaturalParser().parse(foundModule.file, tokens);
			referableToModule.put(referableName, new ParsedModule(foundModule.file, module));
			return module;
		}
		catch (Exception e)
		{
			return null; // Not found
		}
	}

	private static record ParsedModule(NaturalFile file, INaturalModule module){}
}
