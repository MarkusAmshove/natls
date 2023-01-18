package org.amshove.natlint.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

import org.amshove.natparse.IDiagnostic;

import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

public class FileStatusSink
{
	public enum MessageType
	{
		FILE_MISSING,
		INDEX_EXCEPTION,
		REPORTING_TYPE,
		FILE_EXCLUDED,
		LEX_EXCEPTION,
		LEX_FAILED,
		PARSE_EXCEPTION,
		PARSE_FAILED,
		LINT_EXCEPTION,
		LINT_FAILED,
		SUCCESS
	}

	public static FileStatusSink create()
	{
		return new FileStatusSink(Paths.get("filestatuses.csv"));
	}

	public static FileStatusSink dummy()
	{
		return new FileStatusSink();
	}

	protected final CharSink sink;

	protected HashSet<String> filenames = new HashSet<String>();

	protected boolean isEnabled = true;

	public FileStatusSink(Path filePath)
	{
		filePath.toFile().delete();
		this.sink = Files.asCharSink(filePath.toFile(), StandardCharsets.UTF_8, FileWriteMode.APPEND);
		try
		{
			sink.write("FilePath;Type;Message;Count%n".formatted());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private FileStatusSink()
	{
		this.sink = null;
		this.isEnabled = false;
	}

	public boolean isEnabled()
	{
		return isEnabled;
	}

	public void print(Path filePath, MessageType messageType, String message, int count)
	{
		if (!this.isEnabled)
		{
			return;
		}

		try
		{
			// var filename = filePath.toString().replace('\\', '/');
			if (filenames.contains(filePath.toString()))
			{
				return;
			}

			sink.write("%s;%s;%s;%d%n".formatted(filePath, messageType.toString(), message, count));
			filenames.add(filePath.toString());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void printStatus(Path filePath, MessageType messageType)
	{
		print(filePath, messageType, "", 1);
	}

	public void printError(Path filePath, MessageType messageType, Exception e)
	{
		print(filePath, messageType, e.getMessage(), 1);
	}

	public void printDiagnostics(
		Path filePath, MessageType messageType,
		List<? extends IDiagnostic> diagnostics
	)
	{
		if (diagnostics.isEmpty())
		{
			return;
		}
		print(filePath, messageType, diagnostics.get(0).message(), diagnostics.size());
	}
}
