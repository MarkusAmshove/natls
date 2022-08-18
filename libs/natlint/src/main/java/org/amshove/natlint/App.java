package org.amshove.natlint;

import org.amshove.natlint.cli.AnalyzeCommand;
import org.amshove.natlint.linter.LinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.parsing.ParserError;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class App
{
	public static void main(String[] args) throws IOException
	{
		if (args.length == 1 && args[0].equals("export-rules"))
		{
			var xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<rules>\n";

			xml += Arrays.stream(ParserError.values()).map(e -> new SonarRule(e.id(), e.errorName(), e.errorName(), "BLOCKER"))
				.map(SonarRule::toString)
				.collect(Collectors.joining(System.lineSeparator()));

			xml += LinterContext.INSTANCE.registeredAnalyzers().stream().flatMap(a -> a.getDiagnosticDescriptions().stream())
				.map(dd -> new SonarRule(dd.getId(), dd.getMessage(), dd.getMessage(), mapPriority(dd.getSeverity())))
				.map(SonarRule::toString)
				.collect(Collectors.joining(System.lineSeparator()));

			xml += "\n</rules>";
			Files.writeString(Paths.get("rules.xml"), xml, StandardCharsets.UTF_8);
			System.out.println(xml);
			return;
		}

		System.exit(new CommandLine(new AnalyzeCommand()).execute(args));
	}

	static String mapPriority(DiagnosticSeverity severity)
	{
		return switch (severity) {
			case INFO -> "MINOR";
			case WARNING -> "MAJOR";
			case ERROR -> "CRITICAL";
		};
	}

	record SonarRule(String key, String name, String description, String priority)
	{
		@Override
		public String toString()
		{
			return """
				<rule>
					<key>%s</key>
					<name><![CDATA[%s]]></name>
					<description><![CDATA[%s]]></description>
					<priority>%s</priority>
					<tag>clutter</tag>
					<type>CODE_SMELL</type>
				</rule>
				""".formatted(
				key,
				name.split(System.lineSeparator())[0],
				description,
				priority
			);
		}
	}
}
