package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.ddm.IDataDefinitionModule;

public interface IViewNode extends IGroupNode
{
	SyntaxToken ddmNameToken();

	IDataDefinitionModule ddm();
}
