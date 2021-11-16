package org.amshove.natls.languageserver;

import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.natparse.natural.project.NaturalProjectFileIndexer;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.nio.file.Path;
import java.util.List;

public class NaturalLanguageService
{

	private final NaturalProject project;

	private NaturalLanguageService(NaturalProject project)
	{
		this.project = project;
	}

	/***
	 * Creates the language service wrapping all LSP functionality.
	 * All project files will be indexed during creation.
	 * @param workspaceRoot Path to the workspace folder
	 */
	public static NaturalLanguageService createService(Path workspaceRoot)
	{
		var project = new BuildFileProjectReader().getNaturalProject(workspaceRoot.resolve("_naturalBuild"));
		var indexer = new NaturalProjectFileIndexer();
		indexer.indexProject(project);
		return new NaturalLanguageService(project);
	}

	public List<? extends SymbolInformation> findWorkspaceSymbols(String query, CancelChecker cancelChecker)
	{
		return project.getLibraries().stream()
			.flatMap(l -> {
				cancelChecker.checkCanceled();
				return l.files().stream();
			})
			.filter(f -> f.getReferableName().toLowerCase().contains(query.toLowerCase()))
			.limit(100)
			.map(f -> {
				cancelChecker.checkCanceled();
				return convertToSymbolInformation(f);
			})
			.toList();
	}

	private SymbolInformation convertToSymbolInformation(NaturalFile file)
	{
		var symbolInformation = new SymbolInformation(
			file.getReferableName(),
			SymbolKind.Class,
			new Location(
				file.getPath().toUri().toString(),
				new Range(
					new Position(0, 0),
					new Position(0, 0)
				)
			),
			file.getLibrary().getName()
		);
		return symbolInformation;
	}
}
