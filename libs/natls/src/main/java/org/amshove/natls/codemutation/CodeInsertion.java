package org.amshove.natls.codemutation;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;

/**
 * Contains the {@code Range} where to insert code. Also contains a {@code insertionPrefix} if e.g. the scope needs to
 * be inserted and a {@code insertionSuffix} if something needs to be added after.
 * 
 * @param insertionPrefix A prefix which needs to be prepended to the code
 * @param range The range where the prefix + code + suffix name can be inserted
 * @param insertionSuffix A suffix which needs to be appended to the code
 */
public record CodeInsertion(Path filePath, String insertionPrefix, Range range, String insertionSuffix)
{
	public CodeInsertion(Path filePath, String prefix, Range range)
	{
		this(filePath, prefix, range, "");
	}

	public CodeInsertion(Path filePath, Range range, String suffix)
	{
		this(filePath, "", range, suffix);
	}

	public CodeInsertion(Path filePath, Range range)
	{
		this(filePath, "", range, "");
	}

	/**
	 * Creates the text that needs to be inserted.<br/>
	 * It is constructed of prefix, code and suffix
	 */
	public String insertionText(String code)
	{
		return String.format("%s%s%s", insertionPrefix, code, insertionSuffix);
	}

	public FileEdit toFileEdit(String code)
	{
		return new FileEdit(
			filePath,
			new TextEdit(
				range,
				insertionText(code)
			)
		);
	}
}
