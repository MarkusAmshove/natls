package org.amshove.natqube.ruletranslator;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public record DiagnosticRule(String id, String name, String description, String priority, List<String> tags, String type)
{
	public String toSonarRuleXml()
	{
		var tagsXml = tags.stream().map("<tag>%s</tag>"::formatted).collect(Collectors.joining("\n"));
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
			id,
			name,
			description,
			priority,
			tagsXml,
			type
		);
	}

	public String toWebsiteDocumentation()
	{
		var builder = new StringBuilder();
		builder.append("---").append("\n");
		builder.append("id: ").append(id).append("\n");
		builder.append("type: ").append(type.toLowerCase(Locale.ROOT)).append("\n");
		builder.append("priority: ").append(priority.toLowerCase(Locale.ROOT)).append("\n");
		builder.append("tags:").append("\n");
		tags.stream().map("- %s \n"::formatted).forEach(builder::append);
		builder.append("---").append("\n");
		builder.append("\n");
		builder.append("### ").append(id.toUpperCase(Locale.ROOT)).append(": ").append(name).append("\n");
		builder.append(description);

		return builder.toString();
	}
}
