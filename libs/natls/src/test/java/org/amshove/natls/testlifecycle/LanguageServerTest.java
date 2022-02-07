package org.amshove.natls.testlifecycle;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

@LspTest
public abstract class LanguageServerTest
{
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
			if(existed)
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

	protected Range singleCharacterPosition(int line, int column)
	{
		var position = new Position(line, column);
		return new Range(
			position,
			position
		);
	}
}
