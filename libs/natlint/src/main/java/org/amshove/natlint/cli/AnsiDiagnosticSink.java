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

	private static final String ADDITIONAL_INFO_INDENT = "       ";

	private static final Comparator<IDiagnostic> byLineNumber = Comparator.comparingInt(IPosition::line);
	private static final ActualFilesystem filesystem = new ActualFilesystem();

	private static final Object PRINT_LOCK = new Object();

	@Override
	public void printDiagnostics(int currentFileCount, Path filePath, List<IDiagnostic> diagnostics)
	{
		if (diagnostics.isEmpty())
		{
			return;
		}

		var sortedDiagnostics = diagnostics.stream().sorted(byLineNumber).toList();

		var printed = 0;
		synchronized (PRINT_LOCK)
		{
			for (var diagnostic : sortedDiagnostics)
			{
				System.out.println(pathWithLineInformation(diagnostic));

				System.out.println();
				System.out.println(readDiagnosticSourceLine(diagnostic, diagnostic));
				System.out.println(squiggle(diagnostic, diagnostic.severity()));
				System.out.println(message(diagnostic));

				for (var additionalDiagnosticInfo : diagnostic.additionalInfo())
				{
					System.out.println(indented("= ") + pathWithLineInformation(additionalDiagnosticInfo.position()));
					System.out.println(indented(readDiagnosticSourceLine(diagnostic, additionalDiagnosticInfo.position())));
					System.out.println(indented(squiggle(additionalDiagnosticInfo.position(), diagnostic.severity())));
					System.out.println(indented(message(diagnostic, additionalDiagnosticInfo.position(), additionalDiagnosticInfo.message(), true)));
				}

				System.out.println();

				printed++;
			}

			if (printed > 0)
			{
				System.out.println();
			}
		}
	}

	private String indented(String message)
	{
		var result = new StringBuilder();
		var lines = message.split("\n");
		var remainingLines = lines.length;
		for (var l : lines)
		{
			result.append(ADDITIONAL_INFO_INDENT).append(l);
			if (remainingLines > 1)
			{
				result.append(System.lineSeparator());
			}
			remainingLines--;
		}
		return result.toString();
	}

	private String message(IDiagnostic diagnostic)
	{
		return message(diagnostic, diagnostic.message());
	}

	private String message(IDiagnostic diagnostic, String diagnosticMessage)
	{
		return message(diagnostic, diagnostic, diagnosticMessage, false);
	}

	private String message(IDiagnostic diagnostic, IPosition position, String diagnosticMessage, boolean isAdditionalInfo)
	{
		var offsetInLine = position.offsetInLine();
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
		if (!isAdditionalInfo)
		{
			message.append(colored(diagnostic.severity().toString(), severity));
			message.append(colored(": ", severity));
		}
		message.append(colored(splitMessage(diagnosticMessage, offsetInLine), severity));
		if (!isAdditionalInfo)
		{
			message.append(colored(" [%s]".formatted(diagnostic.id()), severity));
		}
		return message.toString();
	}

	private String splitMessage(String message, int offset)
	{
		var splitMessage = message.split("\n");
		return Arrays.stream(splitMessage).collect(Collectors.joining("\n" + " ".repeat(offset)));
	}

	private String readDiagnosticSourceLine(IDiagnostic diagnostic, IPosition position)
	{
		return readSourcePosition(position, diagnostic.severity());
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

	private String squiggle(IPosition position, DiagnosticSeverity severity)
	{
		return " ".repeat(Math.max(0, position.offsetInLine())) +
			colored("~".repeat(Math.max(0, position.length())), severity);
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
