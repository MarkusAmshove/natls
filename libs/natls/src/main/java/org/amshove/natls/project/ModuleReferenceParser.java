package org.amshove.natls.project;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.TokenList;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

/**
 * A light parser that just looks for outgoing references of a {@link LanguageServerFile} based on tokens. It does not
 * analyze anything else and is intended to fastly build the dependency tree of a project.
 */
public class ModuleReferenceParser
{
	public void parseReferences(LanguageServerFile file)
	{
		var lexer = new Lexer();
		var path = file.getPath();
		try
		{
			var tokens = lexer.lex(Files.readString(path), path);
			var calledModules = processReferences(tokens);
			for (var calledModule : calledModules)
			{
				if (calledModule != null)
				{
					var calledFile = file.getLibrary().provideNaturalModule(calledModule.referredModule, true);
					if (calledFile != null)
					{
						ModuleReferenceCache.addEntry(calledFile, calledModule.referencingPosition);
						calledFile.addIncomingReference(file);
						file.addOutgoingReference(calledFile);
					}
				}
			}
		}
		catch (IOException e)
		{
			// do not interrupt project indexing
		}
	}

	private Set<FoundReference> processReferences(TokenList tokens)
	{
		var calledModules = new HashSet<FoundReference>();
		var definedSubroutines = new HashSet<String>();
		var calledSubroutines = new HashSet<FoundReference>();
		while (!tokens.isAtEnd())
		{
			switch (tokens.peek().kind())
			{
				case USING -> calledModules.add(processUsingOrPerform(tokens));
				case DEFINE ->
				{
					if (tokens.peek(1).kind() != SyntaxKind.DATA)
					{
						definedSubroutines.add(processSubroutine(tokens));
					}
				}
				case PERFORM -> calledSubroutines.add(processUsingOrPerform(tokens));
				case CALLNAT -> calledModules.add(processCallnat(tokens));
				case FETCH -> calledModules.add(processFetch(tokens));
				case INCLUDE -> calledModules.add(processCopycode(tokens));
				case IDENTIFIER ->
				{
					if (tokens.peekKinds(SyntaxKind.IDENTIFIER, SyntaxKind.LPAREN, SyntaxKind.LESSER_SIGN)
						|| tokens.peekKinds(SyntaxKind.IDENTIFIER, SyntaxKind.LPAREN, SyntaxKind.LESSER_GREATER, SyntaxKind.RPAREN)
					)
					{
						calledModules.add(processFunction(tokens));
					}
				}
				default ->
				{}
			}

			tokens.advance();
		}

		for (var calledSubroutine : calledSubroutines)
		{
			if (!definedSubroutines.contains(calledSubroutine.referredModule))
			{
				calledModules.add(calledSubroutine);
			}
		}

		return calledModules;
	}

	private FoundReference processFunction(TokenList tokens)
	{
		if (tokens.peek().kind().isIdentifier())
		{
			return new FoundReference(tokens.peek().symbolName(), tokens.peek());
		}

		return null;
	}

	private FoundReference processCopycode(TokenList tokens)
	{
		tokens.advance(); // include
		if (tokens.peek().kind().isIdentifier())
		{
			return new FoundReference(tokens.peek().symbolName(), tokens.peek());
		}

		return null;
	}

	private FoundReference processFetch(TokenList tokens)
	{
		tokens.advance(); // fetch
		tokens.advance(); // repeat/return
		if (tokens.peek().kind() == SyntaxKind.STRING_LITERAL)
		{
			return new FoundReference(tokens.peek().stringValue().toUpperCase(), tokens.peek());
		}
		return null; // variable
	}

	private FoundReference processCallnat(TokenList tokens)
	{
		tokens.advance(); // callnat
		if (tokens.peek().kind() == SyntaxKind.STRING_LITERAL)
		{
			return new FoundReference(tokens.peek().stringValue().toUpperCase(), tokens.peek());
		}
		return null; // variable
	}

	private String processSubroutine(TokenList tokens)
	{
		tokens.advance(); // define
		if (tokens.peek().kind() == SyntaxKind.SUBROUTINE)
		{
			tokens.advance(); // subroutine
		}
		return tokens.peek().symbolName();
	}

	private FoundReference processUsingOrPerform(TokenList tokens)
	{
		tokens.advance(); // using/perform
		return new FoundReference(tokens.peek().symbolName(), tokens.peek());
	}

	record FoundReference(String referredModule, IPosition referencingPosition)
	{}
}
