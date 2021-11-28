package org.amshove.natls.languageserver;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.natparse.natural.project.NaturalProjectFileIndexer;
import org.amshove.natparse.parsing.DefineDataParser;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
		var defineData = parseDefineData(tokens);
		if (defineData != null)
		{
			return defineData.variables().stream()
				.map(variable -> convertToSymbolInformation(variable.declaration(), filepath))
				.map(Either::<SymbolInformation, DocumentSymbol>forLeft)
				.toList();
		}

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

	public ReadOnlyList<IDiagnostic> parseFile(Path filepath)
	{
		var source = readSource(filepath);
		return parseSource(source);
	}

	public ReadOnlyList<IDiagnostic> parseSource(String source)
	{
		var tokens = lexSource(source);
		var parseResult = new DefineDataParser().parse(tokens);
		var allDiagnostics = new ArrayList<IDiagnostic>();
		allDiagnostics.addAll(tokens.diagnostics().stream().toList()); // TODO: Perf
		allDiagnostics.addAll(parseResult.diagnostics().stream().toList()); // TODO: Perf
		return ReadOnlyList.from(allDiagnostics);
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
			token.source(),
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
		var emptyHover = new Hover(new MarkupContent(MarkupKind.PLAINTEXT, ""));

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

			if (token.offsetInLine() <= position.getCharacter() && token.offsetInLine() + token.length() >= position.getCharacter())
			{
				break;
			}

			tokens.advance();
		}

		if (tokens.isAtEnd())
		{
			// No position found where we can provide hover for
			System.err.println("No hover source found");
			return emptyHover;
		}

		var variableToSearchFor = tokens.peek();
		System.err.println("Hover for %s".formatted(variableToSearchFor.source()));

		// TODO: perf: we should use the parsed source to find the hover position once we parse most stuff
		// 	because we can then find the position which we should provide the hover for and use parsed references from there on
		tokens = tokens.newResetted();

		var defineData = new DefineDataParser().parse(tokens).result(); // TODO: check result
		return defineData.variables().stream()
			.filter(v -> v.declaration().source().equals(variableToSearchFor.source()))
			.map(v ->
				new Hover(
					new MarkupContent(
						MarkupKind.MARKDOWN,
						createHoverMarkdownText(v)
					)
				)
			)
			.findFirst()
			.orElseGet(() -> emptyHover);
	}

	private String createHoverMarkdownText(IVariableNode v)
	{
		var hoverText = "**(%s)** **%d** **%s**".formatted(v.scope().name(), v.level(), v.name());
		if (v instanceof ITypedNode)
		{
			var typedVariable = (ITypedNode) v;
			hoverText += " (%c".formatted(typedVariable.type().format().identifier());
			if (typedVariable.type().length() > 0.0)
			{
				hoverText += "%f)".formatted(typedVariable.type().length());
			}
			if (typedVariable.type().hasDynamicLength())
			{
				hoverText += ") **DYNAMIC**";
			}
			if (typedVariable.type().isConstant())
			{
				hoverText += " CONST<";
			}
			if (typedVariable.type().initialValue() != null)
			{
				if(!typedVariable.type().isConstant())
				{
					hoverText += " INIT<";
				}

				hoverText += "**%s**>".formatted(typedVariable.type().initialValue().source());
			}
		}

		if (v.level() > 1)
		{
			var groupOwner = v.parent();
			while (!(groupOwner instanceof IGroupNode) && ((IGroupNode) groupOwner).level() == 1)
			{
				groupOwner = ((ISyntaxNode) groupOwner).parent();
			}

			var group = ((IGroupNode) groupOwner);
			hoverText += "\n\nMember of group:";
			hoverText += "\n\n**%d** **%s**".formatted(group.level(), group.name());
		}

		hoverText += "\n\nFrom module:";
		hoverText += "\n\n*TODO*";

		return hoverText;
	}

	private String readSource(Path path)
	{
		try
		{
			return Files.readString(path);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private TokenList lexSource(String source)
	{
		return new Lexer().lex(source);
	}

	private TokenList lexPath(Path path)
	{
		return lexSource(readSource(path));
	}

	private IDefineData parseDefineData(TokenList tokens)
	{
		var parser = new DefineDataParser();
		return parser.parse(tokens).result();
	}
}
