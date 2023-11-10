package org.amshove.natparse.lexing;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Stack;

import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.parsing.IModuleProvider;

public class CopyCodeResolver
{
	private static final IFilesystem fs = new ActualFilesystem();
	private Stack<Path> includeStack = new Stack<>();

	public TokenList resolve(TokenList tokens, IModuleProvider moduleProvider)
	{
		includeStack.push(tokens.filePath());
		var newTokens = new ArrayList<SyntaxToken>();
		var hiddenTokens = new ArrayList<SyntaxToken>();
		var diagnostics = new ArrayList<>(tokens.diagnostics);
		while (!tokens.isAtEnd())
		{
			if (tokens.peek().kind() == SyntaxKind.INCLUDE)
			{
				handleInclude(newTokens, hiddenTokens, diagnostics, tokens, moduleProvider);
			}
			else
			{
				newTokens.add(tokens.advance());
			}
		}

		includeStack.pop();
		return TokenList.withResolvedIncludes(tokens, newTokens, hiddenTokens, diagnostics);
	}

	private void handleInclude(ArrayList<SyntaxToken> newTokens, ArrayList<SyntaxToken> hiddenTokens, ArrayList<LexerDiagnostic> diagnostics, TokenList tokens, IModuleProvider moduleProvider)
	{
		hiddenTokens.add(tokens.advance()); // INCLUDE
		var copycodeNameToken = tokens.advance();
		// TODO: Assert Identifier etc.
		hiddenTokens.add(copycodeNameToken);
		var moduleName = copycodeNameToken.symbolName();

		var parameter = new ArrayList<SyntaxToken>();
		while (tokens.peek().kind() == SyntaxKind.STRING_LITERAL)
		{
			parameter.add(tokens.advance());
		}

		var lexer = new Lexer();
		var copycode = moduleProvider.findNaturalModule(moduleName);
		// TODO: Module not found
		// TODO: Wrong Module Type
		var path = copycode.file().getPath();
		if (includeStack.contains(path))
		{
			// TODO: Diagnostic for recursive copycodes and bail out
		}
		var source = fs.readFile(path);
		lexer.relocateDiagnosticPosition(copycodeNameToken);
		var lexedTokens = lexer.lex(source, path);
		var nestedResolved = resolve(lexedTokens, moduleProvider);
		diagnostics.addAll(nestedResolved.diagnostics);
		while (!nestedResolved.isAtEnd())
		{
			newTokens.add(nestedResolved.advance());
		}
	}
}
