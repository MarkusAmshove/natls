package org.amshove.natdoc;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IHasDefineData;
import org.amshove.natparse.natural.INaturalModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleDocumentationGenerator
{
	private StringBuilder documentation = null;

	public String generateDocumentation(LanguageServerFile file, INaturalModule module) throws IOException
	{
		documentation = new StringBuilder();
		documentation.append("# %s".formatted(file.getReferableName()));

		appendNewLine();
		documentation.append(extractDocumentation(file.comments().toList(), findFirstLineOfCode(file.getPath())));

		appendNewLine();
		// returns

		appendNewLine();
		// parameter

		appendNewLine();
		// examples

		appendNewLine();
		// usages

		return documentation.toString();
	}

	private static int findFirstLineOfCode(Path filePath) throws IOException
	{
		var sourceLines = Files.readAllLines(filePath);
		for(var lineNumber = 0; lineNumber < sourceLines.size(); lineNumber++)
		{
			var line = sourceLines.get(lineNumber).trim();
			if(line.startsWith("*") || line.startsWith("/*"))
			{
				continue;
			}

			return lineNumber;
		}

		return 0;
	}

	private static String extractDocumentation(List<SyntaxToken> allComments, int firstLineOfCode)
	{
		return allComments.stream()
			.takeWhile(t -> t.line() < firstLineOfCode)
			.map(SyntaxToken::source)
			.filter(l -> !l.startsWith("* >") && !l.startsWith("* <") && !l.startsWith("* :"))
			.filter(l -> !l.startsWith("/*****"))
			.filter(l -> l.startsWith("/**"))
			.filter(l -> !l.contains(":author"))
			.filter(l -> !l.contains(":since"))
			.map(l -> l.substring(l.indexOf("/**") + "/**".length()).trim())
			.filter(l -> !l.isEmpty() && !l.isBlank())
			.collect(Collectors.joining(System.lineSeparator()));
	}

	private void appendNewLine()
	{
		documentation.append("\n\n");
	}
}
