package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IStatementListNode;

class StatementListParser extends AbstractParser<IStatementListNode>
{

	private StatementListNode statementList;

	StatementListParser(IModuleProvider moduleProvider)
	{
		super(moduleProvider);
	}

	@Override
	protected IStatementListNode parseInternal()
	{
		statementList = new StatementListNode();

		while(!tokens.isAtEnd())
		{
			try
			{
				switch(tokens.peek().kind())
				{
					case CALLNAT:
						callnat();
						break;
					case INCLUDE:
						include();
						break;
					default:
						// While the parser is incomplete, we just skip over everything we don't know yet
						tokens.advance();
				}
			}
			catch(ParseError e)
			{
				// Currently just skip over the token
				// TODO: Add a diagnostic. For this move the method from DefineDataParser up into AbstractParser
				tokens.advance();
			}
		}

		return statementList;
	}

	private void callnat() throws ParseError
	{
		var callnat = new CallnatNode();

		consumeMandatory(callnat, SyntaxKind.CALLNAT);

		if(consumeOptionally(callnat, SyntaxKind.STRING))
		{
			callnat.setReferencingToken(previousToken());
			var referencedModule = sideloadModule(callnat.referencingToken().stringValue().toUpperCase(), previousTokenNode());
			callnat.setReferencedModule((NaturalModule) referencedModule);
		}

		statementList.addStatement(callnat);
	}

	private void include() throws ParseError
	{
		var include = new IncludeNode();

		consumeMandatory(include, SyntaxKind.INCLUDE);

		var referencingToken = consumeMandatoryIdentifier(include);
		include.setReferencingToken(referencingToken);

		var referencedModule = sideloadModule(referencingToken.symbolName(), previousTokenNode());
		include.setReferencedModule((NaturalModule) referencedModule);

		statementList.addStatement(include);
	}
}
