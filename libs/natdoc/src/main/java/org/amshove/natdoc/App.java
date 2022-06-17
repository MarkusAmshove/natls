package org.amshove.natdoc;

import org.amshove.natls.languageserver.NaturalLanguageService;
import org.amshove.natls.progress.StdOutProgressMonitor;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.LanguageServerProject;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.project.NaturalFileType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class App
{
	private static final List<NaturalFileType> RELEVANT_FILE_TYPES = List.of(NaturalFileType.FUNCTION, NaturalFileType.SUBROUTINE, NaturalFileType.SUBPROGRAM);
	public static void main(String[] args) throws IOException
	{
		var nls = new NaturalLanguageService();
		var workspaceRoot = Paths.get(".");
		System.out.printf("Using natural project root: %s%n", workspaceRoot.toAbsolutePath());
		nls.indexProject(workspaceRoot, new StdOutProgressMonitor());

		var relevantFiles = findRelevantFiles(nls.getProject());
		var targetFolder = workspaceRoot.resolve("docs").resolve("docs");
		if(!targetFolder.toFile().exists())
		{
			targetFolder.toFile().mkdirs();
		}

		var totalProcessed = 0;
		var totalFiles = relevantFiles.size();
		var progress = new StdOutProgressMonitor();
		var generator = new ModuleDocumentationGenerator();
		for (var naturalFile : relevantFiles)
		{
			totalProcessed++;
			progress.progress("Generating documentation for %s".formatted(naturalFile.getReferableName()), 100 * totalProcessed / totalFiles);
			var module = naturalFile.parseDefineDataOnly();
			var documentation = generator.generateDocumentation(naturalFile, module);
			var libraryFolder = targetFolder.resolve(naturalFile.getLibrary().name());
			libraryFolder.toFile().mkdirs();
			var targetFile = libraryFolder.resolve("%s.md".formatted(naturalFile.getReferableName()));
			Files.writeString(targetFile, documentation);
		}
	}

	private static List<LanguageServerFile> findRelevantFiles(LanguageServerProject project)
	{
		return project.provideAllFiles()
			.filter(f -> RELEVANT_FILE_TYPES.contains(f.getNaturalFile().getFiletype()))
			.toList();
	}
}
