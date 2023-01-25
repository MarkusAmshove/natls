package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ddm.IDataDefinitionModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.parsing.ddm.DdmParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

class DefaultModuleProvider implements IModuleProvider
{
	private final NaturalFile caller;

	DefaultModuleProvider(NaturalFile caller)
	{
		this.caller = caller;
	}

	@Override
	public IDataDefinitionModule findDdm(String referableName)
	{
		var possibleFiles = caller.getLibrary().getModulesOfType(NaturalFileType.DDM, true);
		if (possibleFiles.isEmpty())
		{
			return null;
		}

		var possibleFile = possibleFiles.stream().filter(f -> f.getReferableName().equals(referableName)).findAny();
		if (possibleFile.isEmpty())
		{
			return null;
		}

		try
		{
			return new DdmParser().parseDdm(Files.readString(possibleFile.get().getPath()));
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
		if (callerLib == null)
		{
			return null;
		}
		var foundFile = callerLib.findFileByReferableName(referableName, true);
		if (foundFile == null)
		{
			return null;
		}

		try
		{
			// Parsing only the DEFINE DATA should be enough for everything except COPYCODEs
			// If we'd parse more, we would have to handle cyclomatic dependencies
			var source = Files.readString(foundFile.getPath());
			var tokens = new Lexer().lex(source, foundFile.getPath());
			var result = new DefineDataParser(this).parse(tokens);
			var module = new NaturalModule(foundFile);
			module.setDefineData(result.result());
			return module;
		}
		catch (Exception e)
		{
			return null; // Not found
		}
	}
}
