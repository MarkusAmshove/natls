package org.amshove.natls.testlifecycle;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.amshove.natls.config.LSConfiguration;
import org.amshove.natls.project.LanguageServerFile;
import org.eclipse.lsp4j.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.LogManager;

@LspTest
public abstract class LanguageServerTest
{
	public LanguageServerTest()
	{
		LogManager.getLogManager().reset();
	}

	protected abstract LspTestContext getContext();

	protected LanguageServerFile findLanguageServerFile(TextDocumentIdentifier identifier)
	{
		return getContext().languageService().findNaturalFile(identifier);
	}

	protected TextDocumentIdentifier textDocumentIdentifier(String library, String name)
	{
		var uri = getContext().languageService().findNaturalFile(library, name).getUri();
		return new TextDocumentIdentifier(uri);
	}

	protected TextDocumentIdentifier createOrSaveFile(String libraryName, String name, SourceWithCursor source)
	{
		return createOrSaveFile(libraryName, name, source.source());
	}

	protected void configureEditorConfig(String editorConfig)
	{
		getContext().languageService().loadEditorConfig(createFileRelativeToProjectRoot(".editorconfig", editorConfig));
	}

	protected void configureLSConfig(LSConfiguration config)
	{
		var gson = new Gson();
		var jsonObj = new JsonObject();
		var jsonConfig = gson.toJsonTree(config);
		jsonObj.add("natls", jsonConfig);
		getContext().workspaceService().didChangeConfiguration(new DidChangeConfigurationParams(jsonObj));
	}

	private Path createFileRelativeToProjectRoot(String relativePath, String content)
	{
		var path = getContext().project().rootPath().resolve(relativePath);
		try
		{
			Files.writeString(path, content);
			return path;
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	protected LanguageServerFile createOrSaveLanguageServerFile(String libraryName, String name, String source)
	{
		return findLanguageServerFile(createOrSaveFile(libraryName, name, source));
	}

	protected TextDocumentIdentifier createOrSaveFile(String libraryName, String name, String source)
	{
		try
		{
			var library = getContext().project().libraries().stream()
				.filter(l -> l.name().equals(libraryName))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("Could not find library named " + libraryName));
			var librarySourcePath = library.getSourcePath();
			var filePath = librarySourcePath.resolve(name);
			var existed = filePath.toFile().exists();
			Files.writeString(filePath, source);

			var fileUri = filePath.toUri().toString();
			if (existed)
			{
				getContext().documentService().didSave(new DidSaveTextDocumentParams(new TextDocumentIdentifier(fileUri)));
				//				getContext().languageService().fileSaved(filePath);
			}
			else
			{
				getContext().workspaceService().didCreateFiles(new CreateFilesParams(List.of(new FileCreate(fileUri))));
				//				getContext().languageService().createdFile(fileUri);
			}

			return new TextDocumentIdentifier(fileUri);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
