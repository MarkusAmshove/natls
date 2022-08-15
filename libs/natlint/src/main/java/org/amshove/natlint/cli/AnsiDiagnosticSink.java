package org.amshove.natlint.cli;

import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.IPosition;
import org.amshove.natparse.infrastructure.ActualFilesystem;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnsiDiagnosticSink implements IDiagnosticSink
{
	private static final Map<DiagnosticSeverity, String> SEVERITY_COLOR_MAP = Map.of(
		DiagnosticSeverity.ERROR, "31",
		DiagnosticSeverity.WARNING, "33",
		DiagnosticSeverity.INFO, "34"
	);

	private static final Comparator<IDiagnostic> byLineNumber = Comparator.comparingInt(IPosition::line);
	private static final ActualFilesystem filesystem = new ActualFilesystem();

	@Override
	public void printDiagnostics(Path filePath, List<IDiagnostic> diagnostics)
	{
		if (diagnostics.isEmpty())
		{
			return;
		}

		var sortedDiagnostics = diagnostics.stream().sorted(byLineNumber).toList();

		var printed = 0;
		for (var diagnostic : sortedDiagnostics)
		{
			System.out.println(pathWithLineInformation(diagnostic));

			System.out.println();
			System.out.println(readDiagnosticSourceLine(diagnostic));
			System.out.println(squiggle(diagnostic));
			System.out.println(message(diagnostic));
			System.out.println();

			printed++;
		}

		if(printed > 0)
		{
			System.out.println();
		}
	}

	private String message(IDiagnostic diagnostic)
	{
		var offsetInLine = diagnostic.originalPosition().isSamePositionAs(diagnostic) ? diagnostic.offsetInLine() : diagnostic.offsetInLine() + diagnostic.originalPosition().offsetInLine() + 2;
		var severity = diagnostic.severity();
		var message = new StringBuilder();
		message.append(" ".repeat(offsetInLine));
		message.append(colored("|", severity));
		message.append(System.lineSeparator());
		message.append(" ".repeat(offsetInLine));
		message.append(colored("|", severity));
		message.append(System.lineSeparator());
		message.append(" ".repeat(offsetInLine));
		message.append(colored("= ", severity));
		message.append(colored(diagnostic.severity().toString(), severity));
		message.append(colored(": ", severity));
		message.append(colored(splitMessage(diagnostic.message(), offsetInLine), severity));
		message.append(colored(" [%s]".formatted(diagnostic.id()), severity));
		return message.toString();
	}

	private String splitMessage(String message, int offset)
	{
		var splitMessage = message.split("\n");
		return Arrays.stream(splitMessage).collect(Collectors.joining("\n" + " ".repeat(offset)));
	}

	private String readDiagnosticSourceLine(IDiagnostic diagnostic)
	{
		var diagnosticLocationLine = readSourcePosition(diagnostic, diagnostic.severity());
		if(!diagnostic.originalPosition().filePath().equals(diagnostic.filePath()))
		{
			var originalLocationLine = readSourcePosition(diagnostic.originalPosition(), diagnostic.severity());
			return new StringBuilder()
				.append(diagnosticLocationLine)
				.append("\n")
				.append(" ".repeat(diagnostic.offsetInLine()))
				.append(colored("^\n", diagnostic.severity()))
				.append(" ".repeat(diagnostic.offsetInLine()))
				.append(colored("|\n", diagnostic.severity()))
				.append(" ".repeat(diagnostic.offsetInLine()))
				.append(pathWithLineInformation(diagnostic.originalPosition()))
				.append("\n")
				.append(" ".repeat(diagnostic.offsetInLine()))
				.append(colored("|\n", diagnostic.severity()))
				.append(" ".repeat(diagnostic.offsetInLine()))
				.append(colored("= ", diagnostic.severity()))
				.append(originalLocationLine)
				.toString();
		}

		return diagnosticLocationLine;
	}

	private String readSourcePosition(IPosition position, DiagnosticSeverity severity)
	{
		var source = filesystem.readFile(position.filePath());
		var split = source.split("\n");

		if (split.length < position.line())
		{
			throw new RuntimeException("File <%s> does not contain line number (0-based): %d".formatted(position.filePath(), position.line()));
		}

		var line = split[position.line()];
		var coloredLine = new StringBuilder();
		for (var i = 0; i < line.length(); i++)
		{
			if (i == position.offsetInLine())
			{
				coloredLine
					.append((char) 27 + "[")
					.append(SEVERITY_COLOR_MAP.get(severity))
					.append("m");
			}

			if (i == position.offsetInLine() + position.length())
			{
				coloredLine.append((char) 27 + "[0m");
			}

			coloredLine.append(line.charAt(i));
		}

		coloredLine.append((char) 27 + "[0m");
		return coloredLine.toString();
	}

	private String squiggle(IDiagnostic diagnostic)
	{
		var offsetInLine = diagnostic.originalPosition().isSamePositionAs(diagnostic) ? diagnostic.offsetInLine() : diagnostic.offsetInLine() + diagnostic.originalPosition().offsetInLine() + 2;
		return " ".repeat(Math.max(0, offsetInLine)) +
			colored("~".repeat(Math.max(0, diagnostic.originalPosition().length())), diagnostic.severity());
	}

	private String colored(String message, DiagnosticSeverity severity)
	{
		var coloredMessage = (char) 27 + "[" + SEVERITY_COLOR_MAP.get(severity) + "m";
		coloredMessage += message;
		coloredMessage += (char) 27 + "[0m";
		return coloredMessage;
	}

	private String pathWithLineInformation(IPosition position)
	{
		return "%s:%d:%d".formatted(position.filePath(), position.line() + 1, position.offsetInLine());
	}
}
