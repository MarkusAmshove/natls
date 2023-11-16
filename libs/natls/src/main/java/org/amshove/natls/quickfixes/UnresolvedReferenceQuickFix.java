package org.amshove.natls.quickfixes;

import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.amshove.natls.project.LanguageServerLibrary;
import org.amshove.natls.project.ParseStrategy;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.parsing.ParserError;
import org.amshove.natparse.parsing.TypeInference;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

public class UnresolvedReferenceQuickFix extends AbstractQuickFix
{
	@Override
	protected void registerQuickfixes()
	{
		registerMultipleQuickFixes(ParserError.UNRESOLVED_REFERENCE.id(), this::fixUnresolvedReference);
	}

	@Override
	protected boolean canFixInForeignFiles()
	{
		return true;
	}

	private List<CodeAction> fixUnresolvedReference(QuickFixContext context)
	{
		var unresolvedReference = context.diagnostic().getMessage().replace("Unresolved reference:", "").trim().toUpperCase();

		return Stream.concat(
			createUsingImportCandidates(context, unresolvedReference),
			createDeclareVariableEdit(context, unresolvedReference)
		)
			.toList();
	}

	private Stream<CodeAction> createDeclareVariableEdit(QuickFixContext context, String unresolvedReference)
	{
		var inferredType = TypeInference.inferTypeForTokenInStatement(context.tokenUnderCursor(), context.statementAtPosition())
			.map(IDataType::toShortString)
			.orElse("(A) DYNAMIC");
		/*
		if (context.statementAtPosition() != null)
		{
			var inferredTypeBasedOnPositionInStatement = inferTypeInStatement(context.statementAtPosition(), context.tokenUnderCursor());
			if (inferredTypeBasedOnPositionInStatement)
		}
		var nodeToInferTypeFor = NodeUtil.findTokenNodeForToken(context.tokenUnderCursor(), context.nodeAtPosition());
		if (nodeToInferTypeFor != null)
		{
			if (context.nodeAtPosition() instanceof IAssignmentStatementNode assignment)
			{
				if (assignment.target() == nodeToInferTypeFor)
				{
					var type = TypeInference.inferType(assignment.operand());
					if (type.isPresent())
					{
						inferredType = type.get().toShortString();
					}
				}
			}
			else
				if (context.nodeAtPosition() instanceof IBasicMathStatementNode mathNode)
				{
					if (nodeToInferTypeFor == mathNode.target())
					{
						inferredType = "(I4)";
					}
				}
		}*/

		return Stream.of(
			new CodeActionBuilder("Declare local variable %s".formatted(unresolvedReference), CodeActionKind.QuickFix)
				.fixesDiagnostic(context.diagnostic())
				.appliesWorkspaceEdit(
					new WorkspaceEditBuilder()
						.addsVariable(context.file(), unresolvedReference, inferredType, VariableScope.LOCAL)
				)
				.build(),
			new CodeActionBuilder("Declare parameter %s".formatted(unresolvedReference), CodeActionKind.QuickFix)
				.fixesDiagnostic(context.diagnostic())
				.appliesWorkspaceEdit(
					new WorkspaceEditBuilder()
						.addsVariable(context.file(), unresolvedReference, inferredType, VariableScope.PARAMETER)
				)
				.build()
		);
	}

	private Stream<CodeAction> createUsingImportCandidates(QuickFixContext context, String unresolvedReference)
	{
		var residingLibrary = context.file().getLibrary();

		var candidates = new HashSet<>(findVariableCandidatesInLibrary(unresolvedReference, residingLibrary));
		residingLibrary.getStepLibs().stream().map(l -> findVariableCandidatesInLibrary(unresolvedReference, l)).forEach(candidates::addAll);

		return candidates.stream()
			.flatMap(
				c ->
				{
					var codeActions = new ArrayList<CodeAction>();
					codeActions.add(
						new CodeActionBuilder("Add LOCAL USING to %s (from %s)".formatted(c.module.name(), c.module.file().getLibrary().getName()), CodeActionKind.QuickFix)
							.fixesDiagnostic(context.diagnostic())
							.appliesWorkspaceEdit(
								new WorkspaceEditBuilder()
									.addsUsing(context.file(), c.module.name(), VariableScope.LOCAL)
							)
							.build()
					);

					if (c.module.file().getFiletype() == NaturalFileType.PDA)
					{
						codeActions.add(
							new CodeActionBuilder("Add PARAMETER USING to %s (from %s)".formatted(c.module.name(), c.module.file().getLibrary().getName()), CodeActionKind.QuickFix)
								.fixesDiagnostic(context.diagnostic())
								.appliesWorkspaceEdit(
									new WorkspaceEditBuilder()
										.addsUsing(context.file(), c.module.name(), VariableScope.PARAMETER)
								)
								.build()
						);
					}

					return codeActions.stream();
				}
			);
	}

	private List<VariableCandidate> findVariableCandidatesInLibrary(String variableName, LanguageServerLibrary library)
	{
		return library.files().stream()
			.filter(f -> f.getType() == NaturalFileType.LDA || f.getType() == NaturalFileType.PDA)
			.map(languageServerFile -> languageServerFile.module(ParseStrategy.WITHOUT_CALLERS))
			.map(m ->
			{
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

	private record VariableCandidate(INaturalModule module, IVariableNode variableNode)
	{
		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof VariableCandidate other))
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
