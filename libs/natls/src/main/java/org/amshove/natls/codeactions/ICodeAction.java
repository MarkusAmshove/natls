package org.amshove.natls.codeactions;

import org.eclipse.lsp4j.CodeAction;

import java.util.List;

public interface ICodeAction
{
	boolean isApplicable(CodeActionContext context);
	List<CodeAction> createCodeAction(CodeActionContext context);
}
