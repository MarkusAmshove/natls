package org.amshove.natls.quickfixes;

import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.LanguageServerLibrary;
import org.amshove.natls.project.ParseStrategy;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IHasDefineData;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.IVariableNode;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.parsing.ParserError;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class UnresolvedReferenceQuickFix extends AbstractQuickFix
{
	@Override
	protected void registerQuickfixes()
	{
		registerMultipleQuickFixes(ParserError.UNRESOLVED_REFERENCE.id(), this::fixUnresolvedReference);
	}

	private List<CodeAction> fixUnresolvedReference(QuickFixContext context)
	{
		var tokenUnderCursor = context.tokenUnderCursor();
		if(tokenUnderCursor == null)
		{
			return List.of();
		}

		var unresolvedReference = tokenUnderCursor.symbolName();

		var residingLibrary = context.file().getLibrary();

		var candidates = new HashSet<>(findVariableCandidatesInLibrary(unresolvedReference, residingLibrary));
		residingLibrary.getStepLibs().stream().map(l -> findVariableCandidatesInLibrary(unresolvedReference, l)).forEach(candidates::addAll);

		return candidates.stream()
			.map(c -> new CodeActionBuilder("Add USING to %s (from %s)".formatted(c.module.name(), c.module.file().getPath()), CodeActionKind.QuickFix)
				.fixesDiagnostic(context.diagnostic())
				.appliesWorkspaceEdit(new WorkspaceEditBuilder()
					.addsUsing(context.file(), c.module.name())
				)
				.build())
			.toList();
	}

	private List<VariableCandidate> findVariableCandidatesInLibrary(String variableName, LanguageServerLibrary library)
	{
		return library.files().stream()
			.filter(f -> f.getType() == NaturalFileType.LDA || f.getType() == NaturalFileType.PDA)
			.map(languageServerFile -> languageServerFile.module(ParseStrategy.WITHOUT_CALLERS))
			.map(m -> {
				try
				{
					return new VariableCandidate(m, ((IHasDefineData) m).defineData().findVariable(variableName));
				}
				catch (Exception e)
				{
					return new VariableCandidate(null, null);
				}
			})
			.filter(c -> c.variableNode != null)
			.distinct()
			.toList();
	}

	private record VariableCandidate(INaturalModule module, IVariableNode variableNode) {
		@Override
		public boolean equals(Object obj)
		{
			if(!(obj instanceof VariableCandidate other))
			{
				return false;
			}

			return other.module.file().getPath().equals(module.file().getPath());
		}

		@Override
		public int hashCode()
		{
			return module.file().getPath().hashCode();
		}
	}
}
