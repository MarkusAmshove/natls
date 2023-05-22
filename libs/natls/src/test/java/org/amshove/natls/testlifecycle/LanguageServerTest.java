package org.amshove.natls.testlifecycle;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.LogManager;

@LspTest
public abstract class LanguageServerTest
{
	public LanguageServerTest()
	{
		LogManager.getLogManager().reset();
	}

	protected abstract LspTestContext getContext();

	protected TextDocumentIdentifier textDocumentIdentifier(String library, String name)
	{
		var uri = getContext().languageService().findNaturalFile(library, name).getUri();
		return new TextDocumentIdentifier(uri);
	}

	protected TextDocumentIdentifier createOrSaveFile(String libraryName, String name, SourceWithCursor source)
	{
		return createOrSaveFile(libraryName, name, source.source());
	}

	protected TextDocumentIdentifier createOrSaveFileWithCursor(String libraryName, String name, String sourceWithCursor)
	{
		return createOrSaveFile(libraryName, name, SourceWithCursor.fromSourceWithCursor(sourceWithCursor));
	}

	protected void configureEditorConfig(String editorConfig)
	{
		getContext().languageService().loadEditorConfig(createFileRelativeToProjectRoot(".editorconfig", editorConfig));
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
				getContext().languageService().fileSaved(filePath);
			}
			else
			{
				getContext().languageService().createdFile(fileUri);
			}

			return new TextDocumentIdentifier(fileUri);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
