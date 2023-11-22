package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.IncludeResolvingLexer;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.testhelpers.IntegrationTest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@IntegrationTest
public abstract class ParserIntegrationTest
{
	protected INaturalModule parse(NaturalFile file)
	{
		try
		{
			var source = Files.readString(file.getPath());
			var tokens = new IncludeResolvingLexer().lex(source, file.getPath(), file);
			return new NaturalParser().parse(file, tokens);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	protected INaturalModule assertParsesWithoutAnyDiagnostics(NaturalFile file)
	{
		var module = parse(file);
		assertThat(module.diagnostics()).isEmpty();
		return module;
	}

	@SuppressWarnings("unchecked")
	protected <T extends INaturalModule> T assertFileParsesAs(NaturalFile file, Class<T> moduleType)
	{
		var module = parse(file);
		assertThat(module).isInstanceOfAny(moduleType);
		return (T) module;
	}
}
