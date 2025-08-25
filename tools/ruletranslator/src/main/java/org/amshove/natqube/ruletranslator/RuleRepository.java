package org.amshove.natqube.ruletranslator;

import org.amshove.testhelpers.ResourceHelper;

import java.util.*;

public class RuleRepository
{
	private RuleRepository()
	{}

	private static final Map<String, DiagnosticRule> rules = new HashMap<>();

	static
	{
		var ruleFiles = ResourceHelper.findAbsoluteResourceFiles("/rules/", RuleRepository.class);
		for (var ruleFile : ruleFiles)
		{
			var ruleBuilder = new RuleBuilder();
			var segments = ruleFile.split("/");
			ruleBuilder.key = segments[segments.length - 1].trim();
			var content = ResourceHelper.readResourceFile(ruleFile, RuleRepository.class);
			var lines = content.lines().toList();

			var readingDescription = false;
			for (var line : lines)
			{
				if (line.startsWith("name:"))
				{
					ruleBuilder.name = extractField("name", line);
				}

				if (line.startsWith("priority:"))
				{
					ruleBuilder.priority = extractField("priority", line);
				}

				if (line.startsWith("type:"))
				{
					ruleBuilder.type = extractField("type", line);
				}

				if (line.startsWith("tags:"))
				{
					var tags = extractField("tags", line);
					ruleBuilder.tags.addAll(Arrays.stream(tags.split(",")).map(String::trim).toList());
				}

				if (line.startsWith("description:"))
				{
					readingDescription = true;
					continue;
				}

				if (readingDescription)
				{
					ruleBuilder.description.append(line.trim()).append("\n");
				}
			}

			rules.put(ruleBuilder.key, ruleBuilder.build());
		}
	}

	private static String extractField(String fieldName, String line)
	{
		var split = line.split("%s:".formatted(fieldName));
		return split.length > 1 ? split[1].trim() : "";
	}

	public static Collection<DiagnosticRule> getRules()
	{
		return rules.values();
	}

	public static Map<String, DiagnosticRule> getRuleMappings()
	{
		return rules;
	}

	private static class RuleBuilder
	{
		String key;
		String name;
		String priority;
		List<String> tags = new ArrayList<>();
		String type;
		StringBuilder description = new StringBuilder();

		DiagnosticRule build()
		{
			return new DiagnosticRule(
				key,
				name,
				description.toString(),
				priority,
				tags,
				type
			);
		}
	}
}
