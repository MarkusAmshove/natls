package org.amshove.natparse.lexing;

import org.amshove.natparse.AdditionalDiagnosticInfo;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.parsing.DefaultModuleProvider;
import org.amshove.natparse.parsing.IModuleProvider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

	public TokenList lex(String source, Path path, NaturalFile file)
	{
		return lex(source, path, new DefaultModuleProvider(file));
	}

	public TokenList lex(String source, Path path, IModuleProvider moduleProvider)
	{
		return lexSourceReplace(source, path, moduleProvider, null, List.of());
	}

	private TokenList lex(String source, Path path, IModuleProvider moduleProvider, IPosition relocatedDiagnosticPosition, List<TokenList> parameter)
	{
		var lexer = new Lexer();
		if (relocatedDiagnosticPosition != null)
		{
			lexer.relocateDiagnosticPosition(relocatedDiagnosticPosition);
		}
		var tokenList = lexer.lex(source, path);
		if (!parameter.isEmpty()) // TODO: includeStack.size() > 1 better condition?
		{
			tokenList = substituteCopyCodeParameterInNestedInclude(tokenList, parameter);
		}
		return resolve(tokenList, moduleProvider);
	}

	private TokenList lexSourceReplace(String source, Path path, IModuleProvider moduleProvider, IPosition relocatedDiagnosticPosition, List<String> parameter)
	{
		var lexer = new Lexer(parameter);
		if (relocatedDiagnosticPosition != null)
		{
			lexer.relocateDiagnosticPosition(relocatedDiagnosticPosition);
		}
		return resolve(lexer.lex(source, path), moduleProvider);
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
				handleIncludeSourceReplace(newTokens, hiddenTokens, diagnostics, tokens, moduleProvider);
			}
			else
			{
				newTokens.add(tokens.advance());
			}
		}

		includeStack.pop();
		return TokenList.withResolvedIncludes(tokens, newTokens, hiddenTokens, diagnostics);
	}

	private TokenList substituteCopyCodeParameterInNestedInclude(TokenList tokens, List<TokenList> parameter)
	{
		var newTokens = new ArrayList<SyntaxToken>();
		var diagnostics = new ArrayList<>(tokens.diagnostics);
		while (!tokens.isAtEnd())
		{
			if (tokens.peekKind(SyntaxKind.COPYCODE_PARAMETER))
			{
				var parameterToken = tokens.advance();
				var parameterPosition = parameterToken.copyCodeParameterPosition();
				if (!validateParameterPosition(parameterPosition, parameterToken.diagnosticPosition(), parameterToken, parameter, diagnostics))
				{
					newTokens.add(parameterToken);
					continue;
				}

				substituteParameter(newTokens, parameterPosition, parameter, tokens, parameterToken);
			}
			else
			{
				newTokens.add(tokens.advance());
			}
		}
		return new TokenList(tokens.filePath(), newTokens, diagnostics, tokens.comments().toList(), tokens.sourceHeader());
	}

	private void handleIncludeSourceReplace(ArrayList<SyntaxToken> tokensToAppendCopyCodeTo, ArrayList<SyntaxToken> hiddenTokens, ArrayList<LexerDiagnostic> diagnostics, TokenList outerTokens, IModuleProvider moduleProvider)
	{
		hiddenTokens.add(outerTokens.advance()); // INCLUDE
		var copyCodeNameToken = outerTokens.advance();
		// TODO: Assert Identifier etc.
		hiddenTokens.add(copyCodeNameToken);
		var copyCodeName = copyCodeNameToken.symbolName();

		var parameterToCopyCode = new ArrayList<String>();
		while (!outerTokens.isAtEnd() && outerTokens.peekKind(SyntaxKind.STRING_LITERAL))
		{
			var theParameterToken = outerTokens.advance();
			parameterToCopyCode.add(theParameterToken.stringValue());
		}

		var copycode = moduleProvider.findNaturalModule(copyCodeName);
		if (!validateCopyCodeExists(copycode, diagnostics, copyCodeNameToken))
		{
			return;
		}

		if (!validateModuleIsCopyCode(copycode, diagnostics, copyCodeNameToken))
		{
			return;
		}

		var path = copycode.file().getPath();
		if (isCyclomaticInclude(path, diagnostics, copyCodeNameToken))
		{
			return;
		}

		var source = fs.readFile(path);
		var copyCodeTokens = lexSourceReplace(source, path, moduleProvider, copyCodeNameToken, parameterToCopyCode);
		diagnostics.addAll(copyCodeTokens.diagnostics);

		var unprovidedParameterUsageDiagnostics = new HashMap<Integer, LexerDiagnostic>();
		while (!copyCodeTokens.isAtEnd())
		{
			var theToken = copyCodeTokens.advance();
			if (theToken.kind() == SyntaxKind.COPYCODE_PARAMETER)
			{
				addUnprovidedParameterDiagnostic(copyCodeNameToken, theToken, unprovidedParameterUsageDiagnostics);
			}

			tokensToAppendCopyCodeTo.add(theToken);
		}
		diagnostics.addAll(unprovidedParameterUsageDiagnostics.values());
	}

	private void handleInclude(ArrayList<SyntaxToken> newTokens, ArrayList<SyntaxToken> hiddenTokens, ArrayList<LexerDiagnostic> diagnostics, TokenList tokens, IModuleProvider moduleProvider)
	{
		hiddenTokens.add(tokens.advance()); // INCLUDE
		var copyCodeNameToken = tokens.advance();
		// TODO: Assert Identifier etc.
		hiddenTokens.add(copyCodeNameToken);
		var moduleName = copyCodeNameToken.symbolName();

		var parameter = new ArrayList<TokenList>();
		var parameterLexer = new Lexer();
		while (!tokens.isAtEnd() && (tokens.peekKind(SyntaxKind.STRING_LITERAL)))
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
			parameter.add(new TokenList(theParameterToken.filePath(), positionedParameterTokens));
			hiddenTokens.add(theParameterToken);
		}

		var copycode = moduleProvider.findNaturalModule(moduleName);
		if (!validateCopyCodeExists(copycode, diagnostics, copyCodeNameToken))
		{
			return;
		}

		if (!validateModuleIsCopyCode(copycode, diagnostics, copyCodeNameToken))
		{
			return;
		}

		var path = copycode.file().getPath();
		if (isCyclomaticInclude(path, diagnostics, copyCodeNameToken))
		{
			return;
		}

		var source = fs.readFile(path);
		var lexedTokens = lex(source, path, moduleProvider, copyCodeNameToken, parameter);
		diagnostics.addAll(lexedTokens.diagnostics);
		while (!lexedTokens.isAtEnd())
		{
			var token = lexedTokens.advance();
			newTokens.add(token);
		}
	}

	private boolean validateCopyCodeExists(INaturalModule copycode, List<LexerDiagnostic> diagnostics, SyntaxToken copyCodeNameToken)
	{
		if (copycode == null || copycode.file() == null)
		{
			var diagnostic = LexerDiagnostic.create(
				"Unresolved copy code %s".formatted(copyCodeNameToken.symbolName()),
				copyCodeNameToken.offset(),
				copyCodeNameToken.offsetInLine(),
				copyCodeNameToken.line(),
				copyCodeNameToken.length(),
				copyCodeNameToken.filePath(),
				LexerError.UNRESOLVED_COPYCODE
			);
			diagnostics.add(diagnostic);
			return false;
		}

		return true;
	}

	private boolean validateModuleIsCopyCode(INaturalModule module, List<LexerDiagnostic> diagnostics, SyntaxToken copyCodeNameToken)
	{
		if (module.file().getFiletype() != NaturalFileType.COPYCODE)
		{
			diagnostics.add(
				LexerDiagnostic.create(
					"Module type %s can't be used with INCLUDE".formatted(module.file().getFiletype()),
					copyCodeNameToken.offset(),
					copyCodeNameToken.offsetInLine(),
					copyCodeNameToken.line(),
					copyCodeNameToken.length(),
					copyCodeNameToken.filePath(),
					LexerError.INVALID_INCLUDE_TYPE
				)
			);
			return false;
		}

		return true;
	}

	private boolean isCyclomaticInclude(Path path, List<LexerDiagnostic> diagnostics, SyntaxToken copyCodeNameToken)
	{
		if (includeStack.contains(path))
		{
			diagnostics.add(
				LexerDiagnostic.create(
					"Cyclomatic include found. %s is recursively included multiple times".formatted(copyCodeNameToken.symbolName()),
					copyCodeNameToken.offset(),
					copyCodeNameToken.offsetInLine(),
					copyCodeNameToken.line(),
					copyCodeNameToken.length(),
					copyCodeNameToken.filePath(),
					LexerError.CYCLOMATIC_INCLUDE
				)
			);
			return true;
		}

		return false;
	}

	private void addUnprovidedParameterDiagnostic(SyntaxToken diagnosticPosition, SyntaxToken parameterToken, HashMap<Integer, LexerDiagnostic> diagnostics)
	{
		var diagnostic = diagnostics.computeIfAbsent(
			parameterToken.copyCodeParameterPosition(), position -> LexerDiagnostic.create(
				"Copy code parameter with position %d not provided".formatted(position),
				diagnosticPosition.offset(),
				diagnosticPosition.offsetInLine(),
				diagnosticPosition.line(),
				diagnosticPosition.length(),
				diagnosticPosition.filePath(),
				LexerError.MISSING_COPYCODE_PARAMETER
			)
		);

		diagnostic.addAdditionalInfo(new AdditionalDiagnosticInfo("Parameter is used here", parameterToken));
	}

	/**
	 * Substitute the given copy code parameter and add it to the result list of tokens.
	 */
	private static void substituteParameter(ArrayList<SyntaxToken> newTokens, int parameterPosition, List<TokenList> parameter, TokenList originalTokens, SyntaxToken parameterToken)
	{
		var parameterIndex = parameterPosition - 1;
		var tokensFromParameterToInsert = parameter.get(parameterIndex);
		tokensFromParameterToInsert.rollback(); // Always make sure we restart at offset 0 because the parameter's TokenList is reused
		while (!tokensFromParameterToInsert.isAtEnd())
		{
			var tokenToInsert = tokensFromParameterToInsert.advance();
			if (tokenToInsert.kind().isIdentifier() && originalTokens.peekKind(SyntaxKind.DOT) && originalTokens.peekKindSafe(1).canBeIdentifier())
			{
				// Build qualified name for e.g. &1&.#VAR
				tokenToInsert = tokenToInsert.combine(originalTokens.advance(), SyntaxKind.IDENTIFIER);
				tokenToInsert = tokenToInsert.combine(originalTokens.advance(), SyntaxKind.IDENTIFIER);
			}

			// Combine tokens that are meant to be constructed TO the parameter.
			// Example: &1&_ASD
			// Is meant to be one token for an identifier
			var nextInOriginal = originalTokens.peek();
			if (nextInOriginal != null && needsToBeAppended(parameterToken, nextInOriginal))
			{
				tokenToInsert = tokenToInsert.combine(originalTokens.advance(), tokenToInsert.kind());
				var combinedInOriginal = nextInOriginal;
				while ((nextInOriginal = originalTokens.peek()) != null)
				{
					if (needsToBeAppended(combinedInOriginal, nextInOriginal))
					{
						tokenToInsert = tokenToInsert.combine(originalTokens.advance(), tokenToInsert.kind());
						combinedInOriginal = nextInOriginal;
					}
					else
					{
						break;
					}
				}
			}

			if (tokenToInsert.source().endsWith("."))
			{
				tokenToInsert = tokenToInsert.withKind(SyntaxKind.LABEL_IDENTIFIER);
			}

			newTokens.add(tokenToInsert);
		}
	}

	private static boolean needsToBeAppended(SyntaxToken appendedTo, SyntaxToken toBeAppended)
	{
		if (appendedTo.totalEndOffset() != toBeAppended.offset())
		{
			return false;
		}

		return switch (toBeAppended.kind())
		{
			case LPAREN, RPAREN, COMMA -> false;
			default -> true;
		};
	}

	private boolean validateParameterPosition(int position, IPosition diagnosticPosition, IPosition additionalPosition, List<TokenList> passedParameter, List<LexerDiagnostic> diagnostics)
	{
		var parameterIndex = position - 1;
		if (parameterIndex >= passedParameter.size())
		{
			var diagnostic = LexerDiagnostic.create(
				"Copy code parameter with position %d not provided".formatted(position),
				diagnosticPosition.offset(),
				diagnosticPosition.offsetInLine(),
				diagnosticPosition.line(),
				diagnosticPosition.length(),
				diagnosticPosition.filePath(),
				LexerError.MISSING_COPYCODE_PARAMETER
			);
			diagnostic.addAdditionalInfo(
				new AdditionalDiagnosticInfo(
					"Parameter is used here",
					additionalPosition
				)
			);
			diagnostics.add(diagnostic);
			return false;
		}

		return true;
	}

	private void report(SyntaxToken toToken, String message, LexerError error, List<LexerDiagnostic> diagnostics)
	{

	}
}
