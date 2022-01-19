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
			callnat.setCalledModule(previousToken());
			var referencedModule = sideloadModule(callnat.referencingToken().stringValue(), previousTokenNode());
			callnat.setReferencedModule((NaturalModule) referencedModule);
		}

		statementList.addStatement(callnat);
	}
}
