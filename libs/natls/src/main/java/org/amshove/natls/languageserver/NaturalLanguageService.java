package org.amshove.natls.languageserver;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.LanguageServerProject;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.natparse.natural.project.NaturalProjectFileIndexer;
import org.amshove.natparse.parsing.DefineDataParser;
import org.amshove.natparse.parsing.NaturalModule;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NaturalLanguageService implements LanguageClientAware
{

	private static final Hover EMPTY_HOVER = new Hover(new MarkupContent(MarkupKind.PLAINTEXT, ""));
	private NaturalProject project; // TODO: Replace
	private LanguageServerProject languageServerProject;
	private LanguageClient client;

	public void indexProject(Path workspaceRoot)
	{
		var project = new BuildFileProjectReader().getNaturalProject(workspaceRoot.resolve("_naturalBuild"));
		var indexer = new NaturalProjectFileIndexer();
		indexer.indexProject(project);
		this.project = project;
		languageServerProject = LanguageServerProject.fromProject(project);
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

		var filepath = LspUtil.uriToPath(textDocument.getUri());
		var symbolToSearchFor = findTokenAtPosition(filepath, position);
		if (symbolToSearchFor == null)
		{
			// No position found where we can provide hover for
			System.err.println("No hover source found");
			return EMPTY_HOVER;
		}

		System.err.println("Hover for %s".formatted(symbolToSearchFor.source()));

		if (symbolToSearchFor.kind() == SyntaxKind.STRING)
		{
			return hoverCallnat(symbolToSearchFor);
		}

		var defineData = parseDefineData(lexPath(filepath)); // TODO: perf: double lex
		return defineData.variables().stream()
			.filter(v -> v.declaration().symbolName().equals(symbolToSearchFor.symbolName()))
			.map(v ->
				new Hover(
					new MarkupContent(
						MarkupKind.MARKDOWN,
						createHoverMarkdownText(v)
					)
				)
			)
			.findFirst()
			.orElseGet(() -> EMPTY_HOVER);
	}

	private Hover hoverCallnat(SyntaxToken symbolToSearchFor)
	{
		var module = project.findModule(symbolToSearchFor.stringValue());
		if (module == null)
		{
			return EMPTY_HOVER;
		}

		// TODO: Use LanguageServerFile
		var tokens = lexPath(module.getPath());
		var defineData = parseDefineData(tokens);
		if (defineData == null)
		{
			return EMPTY_HOVER;
		}

		var hoverText = "**%s.%s**".formatted(module.getLibrary().getName(), module.getReferableName());
		hoverText += "\n\nParameter:\n```natural\n";

		// TODO: Order is not correct. DefineData should have .parameter() which have USINGs and non-USINGS
		//  mixed in definition order
		hoverText += defineData.parameterUsings().stream()
			.map(using -> "PARAMETER USING %s".formatted(using.target().source())) // TODO: Maybe add the PDA content nested?
			.collect(Collectors.joining("\n"));
		hoverText += "\n```\n";
		hoverText += defineData.variables().stream()
			.filter(v -> v.scope() == VariableScope.PARAMETER)
			.map(this::formatVariableHover).collect(Collectors.joining("\n"));

		return new Hover(
			new MarkupContent(
				MarkupKind.MARKDOWN,
				hoverText
			)
		);
	}

	private SyntaxToken findTokenAtPosition(Path filePath, Position position)
	{
		var tokens = lexPath(filePath);

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

		return tokens.peek();
	}

	private SyntaxToken findPreviousTokenOfPosition(Path filePath, Position position)
	{
		var tokens = lexPath(filePath);

		while (!tokens.isAtEnd())
		{
			var token = tokens.peek();
			if (token.line() < position.getLine())
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

		return tokens.peek(-2); // TODO: Sometimes -1..
	}

	private String createHoverMarkdownText(IVariableNode v)
	{
		var hoverText = formatVariableHover(v);
		hoverText += "\n";

		if (v.level() > 1)
		{
			var groupOwner = v.parent();
			while (!(groupOwner instanceof IGroupNode) && ((IGroupNode) groupOwner).level() == 1)
			{
				groupOwner = ((ISyntaxNode) groupOwner).parent();
			}

			var group = ((IGroupNode) groupOwner);
			hoverText += "\n\n*member of:*";
			hoverText += "%n ```natural%n %s %d %s%n```".formatted(group.scope().name(), group.level(), group.name());
		}

		hoverText += "\n\n*source:*";
		hoverText += "\n- *TODO*";

		return hoverText;
	}

	private String formatVariableHover(IVariableNode v)
	{
		var hoverText = "```natural%n%s %d %s".formatted(v.scope().name(), v.level(), v.name());
		if (v instanceof ITypedVariableNode typedVariable)
		{
			hoverText += " (%c".formatted(typedVariable.type().format().identifier());
			if (typedVariable.type().length() > 0.0)
			{
				hoverText += "%s)".formatted(DataFormat.formatLength(typedVariable.type().length()));
			}
			if (typedVariable.type().hasDynamicLength())
			{
				hoverText += ") DYNAMIC";
			}
			if (typedVariable.type().isConstant())
			{
				hoverText += " CONST<";
			}
			if (typedVariable.type().initialValue() != null)
			{
				if (!typedVariable.type().isConstant())
				{
					hoverText += " INIT<";
				}

				hoverText += "%s>".formatted(typedVariable.type().initialValue().source());
			}
		}
		hoverText += "\n```";
		if (v.isArray())
		{
			hoverText += "\n\n*dimensions:*";
			hoverText += "%n ```%n%s%n```".formatted(v.dimensions().stream().map(IArrayDimension::displayFormat).collect(Collectors.joining(",")));
		}

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

	private TokenList lexSource(String source, Path path)
	{
		return new Lexer().lex(source, path);
	}

	private TokenList lexPath(Path path)
	{
		return lexSource(readSource(path), path);
	}

	// TODO: Remove
	private IDefineData parseDefineData(TokenList tokens)
	{
		var parser = new DefineDataParser(null);
		return parser.parse(tokens).result();
	}

	public List<Location> gotoDefinition(DefinitionParams params)
	{
		var fileUri = params.getTextDocument().getUri();
		var filePath = LspUtil.uriToPath(fileUri);
		var position = params.getPosition();

		var tokenUnderCursor = findTokenAtPosition(filePath, position); // TODO: Double lexing
		System.err.println(tokenUnderCursor);
		if (tokenUnderCursor == null)
		{
			return List.of();
		}

		var file = findNaturalFile(filePath);
		var module = file.module();
		IDefineData defineData;
		if (!(module instanceof IHasDefineData hasDefineData))
		{
			return List.of();
		}
		defineData = hasDefineData.defineData();
		return defineData.variables().stream()
			.filter(v -> v.name().equals(tokenUnderCursor.source()))
			.map(LspUtil::toLocation)
			.toList();
	}

	public List<Location> findReferences(ReferenceParams params)
	{
		var fileUri = params.getTextDocument().getUri();
		var filePath = LspUtil.uriToPath(fileUri);
		var position = params.getPosition();

		var tokenUnderCursor = findTokenAtPosition(filePath, position);
		if (tokenUnderCursor == null)
		{
			return List.of();
		}
		var tokens = lexPath(filePath);

		return tokens.stream()
			.filter(t -> t.kind().isIdentifier() && t.symbolName().equals(tokenUnderCursor.symbolName()))
			.map(t -> LspUtil.toLocation(fileUri, t))
			.toList();
	}

	public SignatureHelp signatureHelp(TextDocumentIdentifier textDocument, Position position)
	{
		var filePath = LspUtil.uriToPath(textDocument.getUri());

		var token = findTokenAtPosition(filePath, position);
		if (token == null || token.kind() != SyntaxKind.STRING)
		{
			return null;
		}

		var calledModule = token.stringValue();
		var calledFile = languageServerProject.findFileByReferableName(calledModule);
		var module = (NaturalModule) calledFile.module();
		if (module.defineData() == null)
		{
			return null;
		}

		var parameter = module.defineData().variables().stream()
			.filter(v -> v.scope().isParameter())
			.filter(v -> v.level() == 1)
			.map(v -> (ITypedVariableNode) v)
			.toList();

		var help = new SignatureHelp();
		var signatureInformation = new SignatureInformation(module.name());
		var parameterInfos = new ArrayList<ParameterInformation>();
		for (var p : parameter)
		{
			var parameterInfo = new ParameterInformation();
			parameterInfo.setLabel(p.name());
			parameterInfo.setDocumentation(new MarkupContent(MarkupKind.MARKDOWN, formatVariableHover(p)));
			parameterInfos.add(parameterInfo);
		}
		signatureInformation.setParameters(parameterInfos);
		help.setSignatures(List.of(signatureInformation));
		// TODO: This needs to actually set the correct current parameter position, once we have a callnat statement
		return help;
	}

	public List<CompletionItem> complete(CompletionParams completionParams)
	{
		var fileUri = completionParams.getTextDocument().getUri();
		var filePath = LspUtil.uriToPath(fileUri);

		// TODO: Use position to filter what stuff to complete (variables, subroutines, ...)
		// var position = completionParams.getPosition();

		var file = findNaturalFile(filePath);
		var module = file.module();
		IDefineData defineData;
		if (!(module instanceof IHasDefineData hasDefineData))
		{
			return List.of();
		}
		defineData = hasDefineData.defineData();

		var token = findPreviousTokenOfPosition(filePath, completionParams.getPosition());
		System.err.println(token.kind());
		if (token != null && token.kind() == SyntaxKind.CALLNAT)
		{
			return findNaturalFile(filePath).getLibrary().getModulesOfType(NaturalFileType.SUBPROGRAM, true)
				.stream()
				.map(f -> (NaturalModule) f.module())
				.map(calledModule -> {
					var completionItem = new CompletionItem(calledModule.name());
					completionItem.setKind(CompletionItemKind.Class);
					completionItem.setInsertTextFormat(InsertTextFormat.Snippet);
					var insertText = "'${0:%s}'".formatted(calledModule.name());

					if (calledModule.defineData() == null)
					{
						return completionItem;
					}

					var parameter = calledModule.defineData().variables().stream().filter(v -> v.scope() == VariableScope.PARAMETER && v.level() == 1).toList();
					var parameterIndex = 1;
					for (var aParameter : parameter)
					{
						insertText += " ${%d:%s}".formatted(parameterIndex++, aParameter.name());
					}
					completionItem.setInsertText(insertText);
					return completionItem;
				})
				.toList();
		}

		var completionItems = defineData.variables().stream()
			.filter(v -> !(v instanceof IRedefinitionNode)) // this is the `REDEFINE #VAR`, which results in the variable being doubled in completion
			.map(v -> {
				var item = new CompletionItem();
				item.setKind(CompletionItemKind.Variable);

				item.setLabel(v.name());
				if (v instanceof ITypedVariableNode typedNode)
				{
					item.setLabel(v.name() + " : " + typedNode.type().toShortString());
					item.setInsertText(v.name());
				}
				if (v instanceof IGroupNode groupNode)
				{
					item.setLabel(v.name() + " : Group");
					item.setInsertText(v.name());
				}

				item.setDocumentation(new MarkupContent(MarkupKind.MARKDOWN, createHoverMarkdownText(v)));

				return item;
			})
			.toList();
		return completionItems;
	}

	public LanguageServerFile findNaturalFile(String library, String name)
	{
		var naturalFile = project.findModule(library, name);
		return languageServerProject.findFile(naturalFile);
	}

	public LanguageServerFile findNaturalFile(Path path)
	{
		var naturalFile = project.findModule(path);
		return languageServerProject.findFile(naturalFile);
	}

	public void publishDiagnostics(LanguageServerFile file)
	{
		client.publishDiagnostics(new PublishDiagnosticsParams(file.getUri(), file.allDiagnostics()));
	}

	@Override
	public void connect(LanguageClient client)
	{
		this.client = client;
	}

	public void fileSaved(Path path)
	{
		var file = findNaturalFile(path);
		if (file == null)
		{
			// TODO: Handle new file
			return;
		}

		file.save();
		publishDiagnostics(file);
	}

	public void fileClosed(Path path)
	{
		var file = findNaturalFile(path);
		if (file == null)
		{
			return;
		}

		file.close();
		publishDiagnostics(file);
	}

	public void fileOpened(Path path)
	{
		var file = findNaturalFile(path);
		if (file == null)
		{
			return;
		}

		file.open();
		publishDiagnostics(file);
	}

	public void fileChanged(Path path, String newSource)
	{
		var file = findNaturalFile(path);
		if (file == null)
		{
			return;
		}

		file.changed(newSource);
		publishDiagnostics(file);
	}
}
