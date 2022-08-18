package org.amshove.natqube.ruletranslator;

import java.util.List;
import java.util.stream.Collectors;

public record SonarRule(String key, String name, String description, String priority, List<String> tags, String type)
{

	public String toXml()
	{
		var tagsXml = tags.stream().map(t -> "<tag>%s</tag>".formatted(t)).collect(Collectors.joining("\n"));
		return """
			<rule>
			    <key>%s</key>
			    <name><![CDATA[%s]]></name>
			    <description><![CDATA[%s]]></description>
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
}
