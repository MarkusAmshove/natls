package org.amshove.natls.languageserver;

import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.natparse.natural.project.NaturalProjectFileIndexer;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class NaturalLanguageService
{

	private final NaturalProject project;

	private NaturalLanguageService(NaturalProject project)
	{
		this.project = project;
	}

	/***
	 * Creates the language service wrapping all LSP functionality.
	 * All project files will be indexed during creation.
	 * @param workspaceRoot Path to the workspace folder
	 */
	public static NaturalLanguageService createService(Path workspaceRoot)
	{
		var project = new BuildFileProjectReader().getNaturalProject(workspaceRoot.resolve("_naturalBuild"));
		var indexer = new NaturalProjectFileIndexer();
		indexer.indexProject(project);
		return new NaturalLanguageService(project);
	}

	public List<Either<SymbolInformation, DocumentSymbol>> findSymbolsInFile(TextDocumentIdentifier textDocument)
	{
		var filepath = LspUtil.uriToPath(textDocument.getUri());
		var tokens = lexPath(filepath);
		return getVariableDeclarationTokens(tokens)
			.filter(t -> t.kind() == SyntaxKind.IDENTIFIER_OR_KEYWORD || t.kind() == SyntaxKind.IDENTIFIER)
			.map(token -> convertToSymbolInformation(token, filepath))
			.map(Either::<SymbolInformation, DocumentSymbol>forLeft)
			.toList();
	}

	private Stream<SyntaxToken> getVariableDeclarationTokens(TokenList tokens)
	{
		return tokens.tokensUntilNext(SyntaxKind.END_DEFINE).stream();
	}

	public List<? extends SymbolInformation> findWorkspaceSymbols(String query, CancelChecker cancelChecker)
	{
		return project.getLibraries().stream()
			.flatMap(l -> {
				cancelChecker.checkCanceled();
				return l.files().stream();
			})
			.filter(f -> f.getReferableName().toLowerCase().contains(query.toLowerCase()))
			.limit(100)
			.map(f -> {
				cancelChecker.checkCanceled();
				return convertToSymbolInformation(f);
			})
			.toList();
	}

	private SymbolInformation convertToSymbolInformation(NaturalFile file)
	{
		return new SymbolInformation(
			file.getReferableName(),
			SymbolKind.Class,
			new Location(
				file.getPath().toUri().toString(),
				new Range(
					new Position(0, 0),
					new Position(0, 0)
				)
			),
			file.getLibrary().getName()
		);
	}

	private SymbolInformation convertToSymbolInformation(SyntaxToken token, Path filepath)
	{
		return new SymbolInformation(
			token.escapedSource(),
			SymbolKind.Variable,
			new Location(
				filepath.toUri().toString(),
				new Range(
					new Position(token.line(), token.offsetInLine()),
					new Position(token.line(), token.offsetInLine() + token.length())
				)
			)
		);
	}

	public Hover hoverSymbol(TextDocumentIdentifier textDocument, Position position)
	{
		var filepath = LspUtil.uriToPath(textDocument.getUri());
		var tokens = lexPath(filepath);
		while (!tokens.isAtEnd())
		{
			var token = tokens.peek();
			if (token.line() != position.getLine())
			{
				tokens.advance();
				continue;
			}

			if(token.offsetInLine() <= position.getCharacter() && token.offsetInLine() + token.length() >= position.getCharacter())
			{
				break;
			}

			tokens.advance();
		}

		var variableToSearchFor = tokens.peek().source();
		return getVariableDeclarationTokens(tokens.newResetted())
			.filter(t -> t.escapedSource().toLowerCase().contains(variableToSearchFor.toLowerCase()))
			.map(t -> new Hover(new MarkupContent(MarkupKind.PLAINTEXT, t.escapedSource())))
			.findFirst()
			.orElseGet(() -> new Hover());
	}

	private TokenList lexPath(Path filepath)
	{
		try
		{
			return new Lexer().lex(Files.readString(filepath));
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
