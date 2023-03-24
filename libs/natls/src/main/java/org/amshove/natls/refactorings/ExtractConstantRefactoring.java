package org.amshove.natls.refactorings;

import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.codeactions.RefactoringContext;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.quickfixes.CodeActionBuilder;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.VariableScope;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.List;

public class ExtractConstantRefactoring implements ICodeActionProvider
{
	@Override
	public boolean isApplicable(RefactoringContext context)
	{
		return context.tokenUnderCursor().kind().isLiteralOrConst();
	}

	@Override
	public List<CodeAction> createCodeAction(RefactoringContext context)
	{
		var constantName = "#C-NEW-CONSTANT";
		var literalToken = context.tokenUnderCursor();

		var additionalChanges = new ArrayList<Range>();
		if (literalToken.kind() == SyntaxKind.STRING_LITERAL)
		{
			context
				.file()
				.tokens()
				.stream()
				.filter(t -> t.kind() == SyntaxKind.STRING_LITERAL)
				.filter(t -> !t.isSamePositionAs(literalToken))
				.filter(t -> t.stringValue().equals(literalToken.stringValue()))
				.map(LspUtil::toRange)
				.forEach(additionalChanges::add);
		}

		var workspaceEdits = new WorkspaceEditBuilder()
			.changesText(context.fileUri(), LspUtil.toRange(literalToken), constantName)
			.addsVariable(context.file(), constantName, getLiteralType(literalToken), VariableScope.LOCAL);

		for (var additionalChange : additionalChanges)
		{
			workspaceEdits.changesText(context.fileUri(), additionalChange, constantName);
		}

		return List.of(
			new CodeActionBuilder("Extract constant", CodeActionKind.RefactorExtract)
				.appliesWorkspaceEdit(workspaceEdits)
				.build()
		);
	}

	private String getLiteralType(SyntaxToken literal)
	{
		var type = switch (literal.kind())
		{
			case STRING_LITERAL -> "(A%d)".formatted(literal.stringValue().length());
			case NUMBER_LITERAL -> "(N12,7)";
			case TRUE, FALSE -> "(B)";
			default -> "(A) DYNAMIC";
		};

		return "%s CONST<%s>".formatted(type, literal.source());
	}
}
