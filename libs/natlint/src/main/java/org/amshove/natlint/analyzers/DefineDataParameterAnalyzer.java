package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ITokenNode;
import org.amshove.natparse.natural.VariableScope;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.natural.IDefineData;
import org.amshove.natparse.natural.IHasDefineData;
import org.amshove.natparse.natural.IModuleWithBody;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.IScopeNode;
import org.amshove.natparse.lexing.SyntaxKind;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefineDataParameterAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription USE_OF_INLINE_PARAMETER_IS_DISCOURAGED = DiagnosticDescription.create(
		"NL031",
		"Use of inline parameter is discouraged.",
		DiagnosticSeverity.WARNING
	);

	public static final DiagnosticDescription PDA_STRUCTURE_DIAGNOSTIC = DiagnosticDescription.create(
		"NL041",
		"%s",
		DiagnosticSeverity.WARNING
	);

	private enum GroupName
	{
		IN("-IN"),
		INOUT("-INOUT"),
		OUT("-OUT");

		final String suffix;

		GroupName(String suffix)
		{
			this.suffix = suffix;
		}

		String fullName(String prefix)
		{
			return prefix + suffix;
		}
	}

	private static <E extends Enum<E>> Map<String, E> buildCaseInsensitiveLookup(E[] values, java.util.function.Function<E, String> nameFunc)
	{
		Map<String, E> map = new HashMap<>(values.length * 2);
		for (E val : values)
		{
			map.put(nameFunc.apply(val).toUpperCase(), val);
		}
		return map;
	}

	private boolean isInlineParameterAnalyserOff;
	private boolean isInOutGroupsAnalyserOff;

	private IScopeNode findParameterScopeIfEndDefine(ISyntaxNode node)
	{
		// Is this an END-DEFINE token?
		if (!(node instanceof ITokenNode tokenNode) || !(tokenNode.token().kind() == SyntaxKind.END_DEFINE))
		{
			return null;
		}

		// Parent *is* DefineData by assumption
		ISyntaxNode parent = node.parent();
		if (!(parent instanceof IDefineData dd))
		{
			return null;
		}

		// Fetch the PARAMETER scope from this DEFINE DATA
		return dd.findFirstScopeNode(VariableScope.PARAMETER);
	}

	private void reportPdaError(IAnalyzeContext context, ISyntaxNode node, String message)
	{
		context.report(PDA_STRUCTURE_DIAGNOSTIC.createFormattedDiagnostic(node.position(), message));
	}

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(USE_OF_INLINE_PARAMETER_IS_DISCOURAGED, PDA_STRUCTURE_DIAGNOSTIC);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IScopeNode.class, this::analyzeInlineParameter);
		//context.registerNodeAnalyzer(ISyntaxNode.class, this::analyzeParameterStructure);
		context.registerModuleAnalyzer(this::analyzeModule);
	}

	@Override
	public void beforeAnalyzing(IAnalyzeContext context)
	{
		isInlineParameterAnalyserOff = !context.getConfiguration(context.getModule().file(), "natls.style.discourage_inlineparameters", OPTION_FALSE).equalsIgnoreCase(OPTION_TRUE);
		isInOutGroupsAnalyserOff = !context.getConfiguration(context.getModule().file(), "natls.style.in_out_groups", OPTION_FALSE).equalsIgnoreCase(OPTION_TRUE);
	}

	private void analyzeInlineParameter(ISyntaxNode node, IAnalyzeContext context)
	{
		if (isInlineParameterAnalyserOff)
		{
			return;
		}

		if (UNWANTED_FILETYPES.contains(context.getModule().file().getFiletype()))
		{
			return;
		}

		if (!NodeUtil.moduleContainsNode(context.getModule(), node))
		{
			return;
		}

		var scope = (IScopeNode) node;
		if (scope.scope().isParameter())
		{
			context.report(USE_OF_INLINE_PARAMETER_IS_DISCOURAGED.createDiagnostic(node));
		}
	}

	private void analyzeModule(INaturalModule module, IAnalyzeContext context)
	{
		if (isInOutGroupsAnalyserOff)
		{
			return;
		}

		if (!module.file().getFiletype().canHaveDefineData())
		{
			return;
		}

		if (module instanceof IHasDefineData hasDD)
		{
			analyzeParameterStructure(hasDD.defineData(), context);
		}
		return;
	}

	private void analyzeParameterStructure(ISyntaxNode node, IAnalyzeContext context)
	{
		if (isInOutGroupsAnalyserOff)
		{
			return;
		}

		if (context.getModule().file().getFiletype() != NaturalFileType.PDA)
		{
			return;
		}

		if (!NodeUtil.moduleContainsNode(context.getModule(), node))
		{
			return;
		}

		var scope = findParameterScopeIfEndDefine(node);
		if (scope == null)
		{
			System.out.println("Skipping PDA structure analysis for node: " + node);
			return; // Not an END-DEFINE or no parameter scope found
		}

		for (var variable : scope.variables())
		{
			System.out.printf(
				"L1: name='%s' level=%d isGroup=%s isArray=%s%n",
				variable.name(), variable.level(), variable.isGroup(), variable.isArray()
			);
		}

		var pdaname = context.getModule().file().getOriginalName().trim().toUpperCase();
		// Case-insensitive lookup: full name -> Group
		Map<String, GroupName> lookup = buildCaseInsensitiveLookup(GroupName.values(), g -> g.fullName(pdaname));
		// Tracks if a given GroupName has already been found
		boolean[] found = new boolean[GroupName.values().length];
		// Keeps the order of found groups for later order validation
		List<GroupName> seenOrder = new ArrayList<>();
		var lvl1Cnt = 0;

		for (var variable : scope.variables())
		{
			// Count level 1 elements
			if (variable.level() == 1)
			{
				lvl1Cnt++;
				continue;
			}

			// Only check level 2 (parser validates other levels)
			if (variable.level() != 2)
			{
				continue;
			}

			// Must be a group, and not an array group
			if (!variable.isGroup() || variable.isArray())
			{
				reportPdaError(context, node, "Level 2 must be a group, and not an array group: " + variable.name());
				return;
			}

			// Lookup allowed group. Trim and uppercase the variable name to match the lookup keys
			GroupName groupName = lookup.get(variable.name().trim().toUpperCase());
			if (groupName == null)
			{
				String allowed = Arrays.stream(GroupName.values())
					.map(x -> x.fullName(pdaname))
					.collect(Collectors.joining(", "));
				reportPdaError(context, node, "Invalid level 2 group: " + variable.name() + ". Allowed are: " + allowed + ".");
				return;
			}

			// Check for duplicates
			int idx = groupName.ordinal();
			if (found[idx])
			{
				reportPdaError(context, node, "Duplicate level 2 group: " + variable.name());
				return;
			}
			found[idx] = true;

			// Record the order for later order check
			seenOrder.add(groupName);
		} // end-for (each variable)

		// Exactly one level-1 element
		if (lvl1Cnt != 1)
		{
			reportPdaError(context, node, "There must be exactly one level 1 element. Found: " + lvl1Cnt);
			return;
		}

		// Order check among present groups only
		for (int i = 1; i < seenOrder.size(); i++)
		{
			if (seenOrder.get(i).ordinal() < seenOrder.get(i - 1).ordinal())
			{
				reportPdaError(
					context, node,
					"Incorrect order at: " + seenOrder.get(i).fullName(pdaname)
						+ ". Correct order is: " + GroupName.IN.fullName(pdaname)
						+ " > " + GroupName.INOUT.fullName(pdaname) + " (optional) > "
						+ GroupName.OUT.fullName(pdaname) + "."
				);
				return;
			}
		}

		// Rule:
		// If INOUT is present → IN and OUT are optional
		// If INOUT is absent → IN/OUT must be present
		boolean hasIN = found[GroupName.IN.ordinal()];
		boolean hasINOUT = found[GroupName.INOUT.ordinal()];
		boolean hasOUT = found[GroupName.OUT.ordinal()];

		/* If INOUT is absent, IN is mandatory */
		if (!hasINOUT && !hasIN)
		{
			reportPdaError(
				context, node,
				"Missing group: " + GroupName.IN.fullName(pdaname)
			);
			return;
		}

		/* IN and OUT must either both be present or both be absent (xor) */
		if (hasIN ^ hasOUT)
		{
			reportPdaError(
				context, node,
				GroupName.IN.fullName(pdaname) + " and " + GroupName.OUT.fullName(pdaname)
					+ " must either both be present or both be absent"
			);
			return;
		}
	}
}
