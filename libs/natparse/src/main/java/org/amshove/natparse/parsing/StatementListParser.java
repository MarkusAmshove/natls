package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.ISymbolReferenceNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

class StatementListParser extends AbstractParser<IStatementListNode>
{
	private List<ISymbolReferenceNode> unresolvedReferences;

	StatementListParser(IModuleProvider moduleProvider)
	{
		super(moduleProvider);
	}

	public List<ISymbolReferenceNode> getUnresolvedReferences()
	{
		return unresolvedReferences;
	}

	@Override
	protected IStatementListNode parseInternal()
	{
		unresolvedReferences = new ArrayList<>();
		var statementList = statementList();

		var resolvedReferences = new ArrayList<ISymbolReferenceNode>();
		for (var statement : statementList.statements())
		{
			if (!(statement instanceof SubroutineNode subroutine))
			{
				continue;
			}

			unresolvedReferences.stream().filter(rn -> rn.token().symbolName().equals(subroutine.declaration().symbolName()))
				.forEach(node -> {
					subroutine.addReference(node);
					resolvedReferences.add(node);
				});
		}

		unresolvedReferences.removeAll(resolvedReferences);
		return statementList;
	}

	private StatementListNode statementList()
	{
		var statementList = new StatementListNode();
		while (!tokens.isAtEnd())
		{
			try
			{
				switch (tokens.peek().kind())
				{
					case CALLNAT:
						statementList.addStatement(callnat());
						break;
					case INCLUDE:
						statementList.addStatement(include());
						break;
					case FETCH:
						statementList.addStatement(fetch());
						break;
					case IDENTIFIER:
					case IDENTIFIER_OR_KEYWORD:
						statementList.addStatement(identifierReference());
						break;
					case END:
						statementList.addStatement(end());
						break;
					case DEFINE:
						statementList.addStatement(subroutine());
						break;
					case END_SUBROUTINE:
						return statementList;
					case IGNORE:
						statementList.addStatement(ignore());
						break;
					case PERFORM:
						statementList.addStatement(perform());
						break;
					default:
						// While the parser is incomplete, we just skip over everything we don't know yet
						tokens.advance();
				}
			}
			catch (ParseError e)
			{
				// Currently just skip over the token
				// TODO: Add a diagnostic. For this move the method from DefineDataParser up into AbstractParser
				tokens.advance();
			}
		}

		return statementList;
	}

	private StatementNode perform() throws ParseError
	{
		var internalPerform = new InternalPerformNode();
		unresolvedReferences.add(internalPerform);

		consumeMandatory(internalPerform, SyntaxKind.PERFORM);
		var symbol = consumeMandatoryIdentifier(internalPerform);
		internalPerform.setCallToken(symbol);

		return internalPerform;
	}

	private StatementNode ignore() throws ParseError
	{
		var ignore = new IgnoreNode();
		consumeMandatory(ignore, SyntaxKind.IGNORE);
		return ignore;
	}

	private StatementNode subroutine() throws ParseError
	{
		var subroutine = new SubroutineNode();
		consumeMandatory(subroutine, SyntaxKind.DEFINE);
		consumeOptionally(subroutine, SyntaxKind.SUBROUTINE);
		var nameToken = consumeMandatoryIdentifier(subroutine);
		subroutine.setName(nameToken);

		subroutine.setBody(statementList());

		consumeMandatory(subroutine, SyntaxKind.END_SUBROUTINE);

		return subroutine;
	}

	private StatementNode end() throws ParseError
	{
		var endNode = new EndNode();
		consumeMandatory(endNode, SyntaxKind.END);
		return endNode;
	}

	private SyntheticVariableStatementNode identifierReference() throws ParseError
	{
		var token = identifier();
		var node = new SymbolReferenceNode(token);
		unresolvedReferences.add(node);
		return new SyntheticVariableStatementNode(node);
	}

	private CallnatNode callnat() throws ParseError
	{
		var callnat = new CallnatNode();

		consumeMandatory(callnat, SyntaxKind.CALLNAT);

		if (isNotCallnatOrFetchModule())
		{
			report(ParserErrors.unexpectedToken(List.of(SyntaxKind.STRING, SyntaxKind.IDENTIFIER), peek()));
		}

		if (consumeEitherOptionally(callnat, SyntaxKind.IDENTIFIER, SyntaxKind.IDENTIFIER_OR_KEYWORD))
		{
			callnat.setReferencingToken(previousToken());
		}

		if (consumeOptionally(callnat, SyntaxKind.STRING))
		{
			callnat.setReferencingToken(previousToken());
			var referencedModule = sideloadModule(callnat.referencingToken().stringValue().toUpperCase().trim(), previousTokenNode());
			callnat.setReferencedModule((NaturalModule) referencedModule);
		}

		return callnat;
	}

	private IncludeNode include() throws ParseError
	{
		var include = new IncludeNode();

		consumeMandatory(include, SyntaxKind.INCLUDE);

		var referencingToken = consumeMandatoryIdentifier(include);
		include.setReferencingToken(referencingToken);

		var referencedModule = sideloadModule(referencingToken.symbolName(), previousTokenNode());
		include.setReferencedModule((NaturalModule) referencedModule);

		if (referencedModule != null)
		{
			try
			{
				var includedSource = Files.readString(referencedModule.file().getPath());
				var tokens = new Lexer().lex(includedSource, referencedModule.file().getPath());
				var nestedParser = new StatementListParser(moduleProvider);
				var statementList = nestedParser.parse(tokens);
				for (var diagnostic : statementList.diagnostics())
				{
					report(diagnostic);
				}
				unresolvedReferences.addAll(nestedParser.unresolvedReferences);
				include.addNode((StatementListNode) statementList.result());
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}

		return include;
	}

	private FetchNode fetch() throws ParseError
	{
		var fetch = new FetchNode();

		consumeMandatory(fetch, SyntaxKind.FETCH);

		consumeEitherOptionally(fetch, SyntaxKind.RETURN, SyntaxKind.REPEAT);

		if (isNotCallnatOrFetchModule())
		{
			report(ParserErrors.unexpectedToken(List.of(SyntaxKind.STRING, SyntaxKind.IDENTIFIER), peek()));
		}

		if (consumeEitherOptionally(fetch, SyntaxKind.IDENTIFIER, SyntaxKind.IDENTIFIER_OR_KEYWORD))
		{
			fetch.setReferencingToken(previousToken());
		}

		if (consumeOptionally(fetch, SyntaxKind.STRING))
		{
			fetch.setReferencingToken(previousToken());
			var referencedModule = sideloadModule(fetch.referencingToken().stringValue().toUpperCase().trim(), previousTokenNode());
			fetch.setReferencedModule((NaturalModule) referencedModule);
		}

		return fetch;
	}

	private boolean isNotCallnatOrFetchModule()
	{
		return !peekKind(SyntaxKind.STRING) && !peekKind(SyntaxKind.IDENTIFIER) && !peekKind(SyntaxKind.IDENTIFIER_OR_KEYWORD);
	}
}
