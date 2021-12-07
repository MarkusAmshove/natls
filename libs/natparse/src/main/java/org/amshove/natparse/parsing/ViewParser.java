package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;

class ViewParser extends AbstractParser<ViewNode>
{
	@Override
	protected ViewNode parseInternal()
	{
		try
		{
			var viewVariable = new VariableNode();
			var level = consumeMandatory(viewVariable, SyntaxKind.NUMBER).intValue();
			viewVariable.setLevel(level);

			var identifier = consumeMandatoryIdentifier(viewVariable);
			viewVariable.setDeclaration(identifier);

			var view = new ViewNode(viewVariable);

			consumeMandatory(view, SyntaxKind.VIEW);
			consumeOptionally(view, SyntaxKind.OF);

			var targetDdm = consumeMandatoryIdentifier(view);
			view.setDdmNameToken(targetDdm);

			// Magic

			return view;
		}
		catch (ParseError e)
		{
			return null;
		}
	}
}
