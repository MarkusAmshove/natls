package org.amshove.natparse.lexing;

import org.amshove.natparse.IPosition;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.parsing.IModuleProvider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Stack;

public class IncludeResolvingLexer
{
	private final IFilesystem fs;
	private Stack<Path> includeStack = new Stack<>();

	public IncludeResolvingLexer()
	{
		this(new ActualFilesystem());
	}

	IncludeResolvingLexer(IFilesystem filesystem)
	{
		fs = filesystem;
	}

	public TokenList lex(String source, Path path, IModuleProvider moduleProvider)
	{
		return lex(source, path, moduleProvider, null);
	}

	private TokenList lex(String source, Path path, IModuleProvider moduleProvider, IPosition relocatedDiagnosticPosition)
	{
		var lexer = new Lexer();
		if (relocatedDiagnosticPosition != null)
		{
			lexer.relocateDiagnosticPosition(relocatedDiagnosticPosition);
		}
		var tokenList = lexer.lex(source, path);
		return resolve(tokenList, moduleProvider);
	}

	private TokenList resolve(TokenList tokens, IModuleProvider moduleProvider)
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

		var parameter = new ArrayList<TokenList>();
		var parameterLexer = new Lexer();
		while (!tokens.isAtEnd() && tokens.peek().kind() == SyntaxKind.STRING_LITERAL)
		{
			var theParameterToken = tokens.advance();
			var parameterTokens = parameterLexer.lex(theParameterToken.stringValue(), theParameterToken.diagnosticPosition().filePath());
			diagnostics.addAll(parameterTokens.diagnostics);
			var positionedParameterTokens = new ArrayList<SyntaxToken>();
			while (!parameterTokens.isAtEnd())
			{
				var originalToken = parameterTokens.advance();
				var positionedToken = new SyntaxToken(
					originalToken.kind(),
					theParameterToken.offset() + originalToken.offset() + 1, // +1 because of the string literal quote char
					theParameterToken.offsetInLine() + originalToken.offsetInLine() + 1,
					theParameterToken.line(),
					originalToken.source(),
					originalToken.filePath()
				);
				positionedParameterTokens.add(positionedToken);
			}
			parameter.add(new TokenList(positionedParameterTokens.get(0).filePath(), positionedParameterTokens));
			hiddenTokens.add(theParameterToken);
		}

		var copycode = moduleProvider.findNaturalModule(moduleName);
		// TODO: Module not found
		// TODO: Wrong Module Type
		var path = copycode.file().getPath();
		if (includeStack.contains(path))
		{
			// TODO: Diagnostic for recursive copycodes and bail out
		}
		var source = fs.readFile(path);
		var lexedTokens = lex(source, path, moduleProvider, copycodeNameToken);
		diagnostics.addAll(lexedTokens.diagnostics);
		while (!lexedTokens.isAtEnd())
		{
			var token = lexedTokens.advance();
			if (token.kind() == SyntaxKind.COPYCODE_PARAMETER)
			{
				var parameterPosition = token.copyCodeParameterPosition();
				// TODO: Parameter not provided diagnostic
				var tokensToInsert = parameter.get(parameterPosition - 1);
				while (!tokensToInsert.isAtEnd())
				{
					var tokenToInsert = tokensToInsert.advance();
					if (tokenToInsert.kind().isIdentifier() && lexedTokens.peekKinds(SyntaxKind.DOT, SyntaxKind.IDENTIFIER))
					{
						// Build qualified name for e.g. &1&.#VAR
						tokenToInsert = tokenToInsert.combine(lexedTokens.advance(), SyntaxKind.IDENTIFIER);
						tokenToInsert = tokenToInsert.combine(lexedTokens.advance(), SyntaxKind.IDENTIFIER);
					}
					newTokens.add(tokenToInsert);
				}
			}
			else
			{
				newTokens.add(token);
			}
		}
	}
}
