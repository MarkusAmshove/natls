package org.amshove.natparse.parsing;

import org.amshove.natparse.NaturalProjectResourceResolver;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(NaturalProjectResourceResolver.class)
public abstract class ParserIntegrationTest
{
	protected INaturalModule parse(NaturalFile file)
	{
		try
		{
			var source = Files.readString(file.getPath());
			var tokens = new Lexer().lex(source, file.getPath());
			return new NaturalParser().parse(file, tokens);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T extends INaturalModule> T assertFileParsesAs(NaturalFile file, Class<T> moduleType)
	{
		var module = parse(file);
		assertThat(module).isInstanceOfAny(moduleType);
		return (T)module;
	}
}
