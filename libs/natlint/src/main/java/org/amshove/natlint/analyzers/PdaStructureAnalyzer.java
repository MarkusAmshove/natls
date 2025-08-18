package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IHasDefineData;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFileType;

import java.util.List;

public class PdaStructureAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription PDA_STRUCTURE_DIAGNOSTIC = DiagnosticDescription.create(
		"NL041",
		"%s",
		DiagnosticSeverity.WARNING
	);

	private static final List<String> ALLOWED_GROUP_SUFFIXES = List.of("-OUT", "-IN", "-INOUT");
	private boolean isInOutGroupsAnalyserOff;

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(PDA_STRUCTURE_DIAGNOSTIC);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerModuleAnalyzer(this::analyzePda);
	}

	@Override
	public void beforeAnalyzing(IAnalyzeContext context)
	{
		isInOutGroupsAnalyserOff = !context.getConfiguration(context.getModule().file(), "natls.style.in_out_groups", OPTION_FALSE).equalsIgnoreCase(OPTION_TRUE);
	}

	private void analyzePda(INaturalModule module, IAnalyzeContext context)
	{
		if (isInOutGroupsAnalyserOff)
		{
			return;
		}

		if (module.file().getFiletype() != NaturalFileType.PDA)
		{
			return;
		}

		if (!(module instanceof IHasDefineData defineData) || defineData.defineData() == null)
		{
			return;
		}

		var pdaName = context.getModule().file().getOriginalName();
		var levelOneVariables = defineData.defineData().variables().stream().filter(v -> v.level() == 1).count();

		if (levelOneVariables > 1)
		{
			context.report(
				PDA_STRUCTURE_DIAGNOSTIC.createFormattedDiagnostic(
					defineData.defineData()
						.diagnosticPosition(),
					"PDAs should only have one Level 1 group"
				)
			);
			return;
		}

		for (var variable : defineData.defineData().variables())
		{
			if (variable.level() == 1 && !variable.name().equalsIgnoreCase(pdaName))
			{
				System.out.println("Level 1 variable name mismatch: " + variable.name() + " vs " + pdaName);
				context.report(
					PDA_STRUCTURE_DIAGNOSTIC.createFormattedDiagnostic(
						variable.diagnosticPosition(), "Level 1 group name should match the PDA name"
					)
				);
			}

			if (variable.level() == 2)
			{
				if (!variable.isGroup() || variable.isArray())
				{
					context.report(
						PDA_STRUCTURE_DIAGNOSTIC.createFormattedDiagnostic(
							variable.diagnosticPosition(),
							"Level 2 must be a group, and not an array group"
						)
					);
				}
				else
				{
					if (!variable.name().toUpperCase().startsWith(pdaName.toUpperCase()))
					{
						context.report(
							PDA_STRUCTURE_DIAGNOSTIC.createFormattedDiagnostic(
								variable.diagnosticPosition(),
								"Level 2 group name should start with PDA name: " + pdaName
							)
						);
					}
				}
			}

			var hasSuffix = ALLOWED_GROUP_SUFFIXES.stream().anyMatch(suffix -> variable.name().endsWith(suffix));
			if (variable.level() == 2 && variable.isGroup() && !hasSuffix)
			{
				context.report(
					PDA_STRUCTURE_DIAGNOSTIC.createFormattedDiagnostic(
						variable.diagnosticPosition(), "Level 2 groups should have one of these suffixes: " + String.join(", ", ALLOWED_GROUP_SUFFIXES)
					)
				);
				continue;
			}
		}
	}
}
