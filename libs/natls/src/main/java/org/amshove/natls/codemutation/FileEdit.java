package org.amshove.natls.codemutation;

import org.amshove.natls.languageserver.LspUtil;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;

public class FileEdit
{
	private final Path filePath;
	private final TextEdit textEdit;

	public FileEdit(Path filePath, TextEdit textEdit)
	{
		this.filePath = filePath;
		this.textEdit = textEdit;
	}

	public String fileUri()
	{
		return LspUtil.pathToUri(filePath);
	}

	public TextEdit textEdit()
	{
		return textEdit;
	}
}
