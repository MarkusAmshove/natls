package org.amshove.natls.snippets;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.LanguageServerProject;
import org.amshove.natparse.natural.IHasDefineData;
import org.amshove.natparse.natural.IVariableNode;
import org.amshove.natparse.natural.VariableScope;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;

public class NatUnitSnippetProvider implements ISnippetProvider // TODO: untested
{
	private static final List<NaturalSnippet> snippets = new ArrayList<>();

	public NatUnitSnippetProvider(LanguageServerProject project)
	{
		var assertionFiles = project.provideAllFiles()
			.filter(f -> f.getType() == NaturalFileType.SUBROUTINE)
			.filter(f -> f.getNaturalFile().getFilenameWithoutExtension().startsWith("ASS"))
			.toList();

		for (var assertionFile : assertionFiles)
		{
			var module = assertionFile.module();
			if (!(module instanceof IHasDefineData hasDefineData) || hasDefineData.defineData() == null)
			{
				continue;
			}

			if (hasDefineData.defineData().parameterUsings().stream().noneMatch(u -> u.target().symbolName().equals("NUASSP")))
			{
				continue;
			}

			var assertionName = assertionFile.getReferableName();
			var parameter = hasDefineData.defineData().variables().stream()
				.filter(v -> v.scope() == VariableScope.PARAMETER)
				.filter(v -> v.level() == 1)
				.map(IVariableNode::name)
				.filter(name -> !name.equals("NUASSP"))
				.toList();
			var placeholderPosition = 1;
			var parameterPlaceholder = new StringBuilder();
			for (var name : parameter)
			{
				parameterPlaceholder.append("${%d:%s} ".formatted(placeholderPosition, name));
				placeholderPosition++;
			}

			var snippetName = new StringBuilder(assertionName.substring(0, 1));
			var nextUpper = false;
			for (var i = 1; i < assertionName.length(); i++)
			{
				if (assertionName.charAt(i) == '-')
				{
					nextUpper = true;
					continue;
				}

				if (nextUpper)
				{
					snippetName.append(assertionName.charAt(i));
					nextUpper = false;
				}
				else
				{
					snippetName.append(Character.toLowerCase(assertionName.charAt(i)));
				}
			}

			snippets.add(new NaturalSnippet(snippetName.toString())
				.insertsText("ASSERT-LINE := *LINE; PERFORM %s NUASSP %s%n${0}".formatted(assertionName, parameterPlaceholder))
				.needsLocalUsing("NUASSP")
				.needsLocalUsing("NUCONST")
				.needsParameterUsing("NUTESTP")
			);
		}

		snippets.add(new NaturalSnippet("TestCase")
			.insertsText("""
				/***********************************************************************
				IF NUTESTP.TEST = '${1:Testcase name}'
				/***********************************************************************
				${0:IGNORE}
				END-IF
				""")
			.needsLocalUsing("NUASSP")
			.needsLocalUsing("NUCONST")
			.needsParameterUsing("NUTESTP")
		);
	}

	@Override
	public List<CompletionItem> provideSnippets(LanguageServerFile file)
	{
		if (file.getType() != NaturalFileType.SUBPROGRAM || !file.getReferableName().startsWith("TC"))
		{
			return List.of();
		}

		return snippets.stream().map(s -> s.createCompletion(file)).toList();
	}
}
