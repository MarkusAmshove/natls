package org.amshove.natqube.ruletranslator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App
{
	public static void main(String[] args) throws IOException
	{
		var sonarRulePath = Paths.get(args[0]);
		var websiteDiagnosticFolder = Paths.get(args[1]);

		ensureFolderExists(sonarRulePath.getParent());
		ensureFolderExists(websiteDiagnosticFolder);

		var rules = RuleRepository.getRules();
		var sonarRulesXml = new StringBuilder();
		sonarRulesXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<rules>\n");

		for (var rule : rules)
		{
			sonarRulesXml.append(rule.toSonarRuleXml()).append("\n");
			Files.writeString(websiteDiagnosticFolder.resolve(rule.id() + ".md"), rule.toWebsiteDocumentation(), StandardCharsets.UTF_8);
		}

		sonarRulesXml.append("\n</rules>");

		Files.writeString(sonarRulePath, sonarRulesXml.toString(), StandardCharsets.UTF_8);
	}

	private static void ensureFolderExists(Path path)
	{
		if (!path.toFile().exists())
		{
			var ignored = path.toFile().mkdirs();
		}
	}
}
