package org.amshove.natls.inlayhints;

import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.languageserver.NaturalLanguageService;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintKind;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.List;

public class InlayHintProvider
{
	public List<InlayHint> provideInlayHints(INaturalModule module, Range range)
	{
		var hints = new ArrayList<InlayHint>();

		module.syntaxTree().acceptNodeVisitor(n ->
		{
			if (!n.isInFile(module.file().getPath()))
			{
				return;
			}

			if (n.diagnosticPosition().line() < range.getStart().getLine() || n.diagnosticPosition().line() > range.getEnd().getLine())
			{
				return;
			}

			if (NaturalLanguageService.getConfig().getInlayhints().isShowAssignmentTargetType() && n instanceof IAssignmentStatementNode assignment && assignment.target()instanceof IVariableReferenceNode reference && reference.reference()instanceof ITypedVariableNode typedRef && typedRef.type() != null)
			{
				var hint = new InlayHint();
				hint.setPosition(LspUtil.toPositionAfter(reference.diagnosticPosition()));
				hint.setLabel(typedRef.formatTypeForDisplay());
				hint.setKind(InlayHintKind.Type);
				hint.setPaddingLeft(true);
				hints.add(hint);
				return;
			}

			if (n instanceof ISubroutineNode subroutineNode)
			{
				var endSubroutine = subroutineNode.findDescendantToken(SyntaxKind.END_SUBROUTINE);
				if (endSubroutine == null)
				{
					return;
				}

				var hint = new InlayHint();
				hint.setPosition(LspUtil.toPositionAfter(endSubroutine.diagnosticPosition()));
				hint.setLabel(subroutineNode.declaration().symbolName());
				hint.setKind(InlayHintKind.Type);
				hint.setPaddingLeft(true);
				hints.add(hint);
				return;
			}

			if (n instanceof IInternalPerformNode internalPerform && !internalPerform.reference().isInFile(module.file().getPath()))
			{
				var hint = new InlayHint();
				hint.setPosition(LspUtil.toPositionAfter(internalPerform.position()));
				hint.setLabel(internalPerform.reference().position().fileNameWithoutExtension());
				hint.setKind(InlayHintKind.Type);
				hint.setPaddingLeft(true);
				hints.add(hint);
				return;
			}
		});

		return hints;
	}
}
