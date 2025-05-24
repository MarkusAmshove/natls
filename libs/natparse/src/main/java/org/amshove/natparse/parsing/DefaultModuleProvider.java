package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.ddm.IDataDefinitionModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.parsing.ddm.DdmParser;

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
		try
		{
			var calledFile = caller.getLibrary().findDdmByReferableName(referableName, true);
			if (calledFile == null)
			{
				return null;
			}
			return new DdmParser().parseDdm(Files.readString(calledFile.getPath()));
		}
		catch (Exception e)
		{
			return null;
		}
	}

	@Override
	public INaturalModule findNaturalModule(String referableName, NaturalFileType requestedType)
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
		var foundFile = callerLib.findModuleByReferableName(referableName, true, requestedType);
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
			var builder = new NaturalModuleBuilder(foundFile);
			builder.setDefineData(result.result());
			return builder.build();
		}
		catch (Exception e)
		{
			return null; // Not found
		}
	}
}
