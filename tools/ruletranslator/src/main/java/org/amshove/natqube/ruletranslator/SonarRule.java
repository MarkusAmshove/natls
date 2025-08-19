package org.amshove.natqube.ruletranslator;

import java.util.List;
import java.util.stream.Collectors;

public record SonarRule(String key, String name, String description, String priority, List<String> tags, String type)
{

	public String toSonarRuleXml()
	{
		var tagsXml = tags.stream().map(t -> "<tag>%s</tag>".formatted(t)).collect(Collectors.joining("\n"));
		return """
			<rule>
			    <key>%s</key>
			    <name><![CDATA[%s]]></name>
			    <description><![CDATA[%s]]></description>
			    <descriptionFormat>MARKDOWN</descriptionFormat>
			    <priority>%s</priority>
			    %s
			    <type>%s</type>
			</rule>
			""".formatted(
			key,
			name,
			description,
			priority,
			tagsXml,
			type
		);
	}

	public String toDiagnosticDoc()
	{
		return """
			---
			id: %s
			type: %s
			priority: %s
			tags:
			%s
			---
			
			### %s
			
			%s
			""".formatted(key, type, priority, tags.stream().map(t -> "- " + t).collect(Collectors.joining("\n")), name, description);
	}
}
