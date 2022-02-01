package org.amshove.natls.codeactions;

import org.eclipse.lsp4j.CodeAction;

public interface ICodeAction
{
	boolean isApplicable(CodeActionContext context);
	CodeAction createCodeAction(CodeActionContext context);
}
