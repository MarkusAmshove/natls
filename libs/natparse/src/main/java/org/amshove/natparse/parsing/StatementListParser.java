package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IStatementListNode;
import org.amshove.natparse.natural.ISymbolReferenceNode;

import java.util.ArrayList;
import java.util.List;

class StatementListParser extends AbstractParser<IStatementListNode>
{

	private StatementListNode statementList;

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
		statementList = new StatementListNode();
		unresolvedReferences = new ArrayList<>();

		while (!tokens.isAtEnd())
		{
			try
			{
				switch (tokens.peek().kind())
				{
					case CALLNAT:
						callnat();
						break;
					case INCLUDE:
						include();
						break;
					case FETCH:
						fetch();
						break;
					case IDENTIFIER:
					case IDENTIFIER_OR_KEYWORD:
						identifierReference();
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

	private void identifierReference() throws ParseError
	{
		var token = identifier();
		var node = new SymbolReferenceNode(token);
		unresolvedReferences.add(node);
	}

	private void callnat() throws ParseError
	{
		var callnat = new CallnatNode();
		statementList.addStatement(callnat);

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
			var referencedModule = sideloadModule(callnat.referencingToken().stringValue().toUpperCase(), previousTokenNode());
			callnat.setReferencedModule((NaturalModule) referencedModule);
		}
	}

	private void include() throws ParseError
	{
		var include = new IncludeNode();
		statementList.addStatement(include);

		consumeMandatory(include, SyntaxKind.INCLUDE);

		var referencingToken = consumeMandatoryIdentifier(include);
		include.setReferencingToken(referencingToken);

		var referencedModule = sideloadModule(referencingToken.symbolName(), previousTokenNode());
		include.setReferencedModule((NaturalModule) referencedModule);
	}

	private void fetch() throws ParseError
	{
		var fetch = new FetchNode();
		statementList.addStatement(fetch);

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
			var referencedModule = sideloadModule(fetch.referencingToken().stringValue().toUpperCase(), previousTokenNode());
			fetch.setReferencedModule((NaturalModule) referencedModule);
		}
	}

	private boolean isNotCallnatOrFetchModule()
	{
		return !peekKind(SyntaxKind.STRING) && !peekKind(SyntaxKind.IDENTIFIER) && !peekKind(SyntaxKind.IDENTIFIER_OR_KEYWORD);
	}
}
