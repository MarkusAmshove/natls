package org.amshove.natdoc;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IHasDefineData;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ITypedVariableNode;
import org.amshove.natparse.natural.IVariableNode;
import org.amshove.natparse.natural.VariableScope;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModuleDocumentationGenerator
{
	private StringBuilder documentation = null;

	public String generateDocumentation(LanguageServerFile file, INaturalModule module) throws IOException
	{
		documentation = new StringBuilder();
		documentation.append("# %s".formatted(file.getReferableName()));
		var firstLineOfCode = findFirstLineOfCode(file.getPath());

		appendNewLine();
		documentation.append(extractDocumentation(file.comments().toList(), firstLineOfCode));

		appendNewLine();
		// returns

		appendNewLine();
		appendParameter(module);

		var author = extractAuthor(file.comments().toList(), firstLineOfCode);
		if(author.isPresent())
		{
			appendNewLine();
			documentation.append("## Author");
			appendNewLine();
			documentation.append(author.get());
		}

		var since = extractSince(file.comments().toList(), firstLineOfCode);
		if(since.isPresent())
		{
			appendNewLine();
			documentation.append("## Since");
			appendNewLine();
			documentation.append(since.get());
		}

		appendNewLine();
		appendUsages(file);
		// usages

		appendNewLine();
		// examples


		return documentation.toString();
	}

	private void appendUsages(LanguageServerFile file)
	{
		documentation.append("## Used by");
		file.getIncomingReferences().stream().forEach(r -> {
			documentation.append("\n- [%s.%s](../%s/%s.md)".formatted(r.getLibrary().name(), r.getReferableName(), r.getLibrary().name(), r.getReferableName()));
		});
	}

	private void appendParameter(INaturalModule module)
	{
		if(!(module instanceof IHasDefineData hasDefineData) || hasDefineData.defineData() == null)
		{
			return;
		}

		var defineData = hasDefineData.defineData();
		documentation.append("## Parameter");
		appendNewLine();
		documentation.append("```\n");
		defineData.parameterUsings().stream().forEach(u -> documentation.append("USING " + u.target().symbolName() + "\n"));
		defineData.variables().stream()
			.filter(v -> v.scope() == VariableScope.PARAMETER)
			.filter(v -> v.position().isSameFileAs(defineData.position()))
			.forEach(v -> documentation.append(convertVariable(v)));

		documentation.append("\n```");
		appendNewLine();
	}

	private static String convertVariable(IVariableNode v)
	{
		if(v instanceof ITypedVariableNode typed)
		{
			return "%d %s %s".formatted(typed.level(), typed.name(), typed.type().toShortString());
		}

		return "%d %s".formatted(v.level(), v.name());
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

	private static Optional<String> extractAuthor(List<SyntaxToken> allComments, int firstLineOfCode)
	{
		return allComments.stream()
			.takeWhile(t -> t.line() < firstLineOfCode)
			.map(SyntaxToken::source)
			.filter(l -> l.contains(":author"))
			.map(l -> l.split(":author")[1].trim())
			.findFirst();
    }

	private static Optional<String> extractSince(List<SyntaxToken> allComments, int firstLineOfCode)
	{
		return allComments.stream()
			.takeWhile(t -> t.line() < firstLineOfCode)
			.map(SyntaxToken::source)
			.filter(l -> l.contains(":since"))
			.map(l -> l.split(":since")[1].trim())
			.findFirst();
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

	record Metadata(String author, String since) {}
}
