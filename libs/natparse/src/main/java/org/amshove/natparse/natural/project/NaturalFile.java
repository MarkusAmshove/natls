package org.amshove.natparse.natural.project;

import java.nio.file.Path;

public class NaturalFile
{
	private String referableName; // TODO: Might change when file is renamed. What should we do then?
	private String originalName;
	private final Path path;
	private final NaturalFileType filetype;
	private NaturalLibrary library;
	private NaturalHeader header;
	private Exception initException;

	public NaturalFile(String referableName, Path path, NaturalFileType filetype, NaturalHeader header)
	{
		this.referableName = referableName;
		if (filetype == NaturalFileType.SUBROUTINE && referableName.length() > 32)
		{
			originalName = referableName;
			this.referableName = originalName.substring(0, 32);
		}
		this.path = path;
		this.filetype = filetype;
		this.header = header;
	}

	public NaturalFile(String referableName, Path path, NaturalFileType filetype, NaturalLibrary library, NaturalHeader header)
	{
		this(referableName, path, filetype, header);
		this.library = library;
	}

	public NaturalFile(Path path, NaturalFileType filetype, Exception e)
	{
		this.path = path;
		this.filetype = filetype;
		this.initException = e;
	}

	public boolean isFailedOnInit()
	{
		return initException != null;
	}

	public String getReferableName()
	{
		return referableName;
	}

	/**
	 * Returns the original name. This only differs from getReferableName if the file type is subroutine and the
	 * declared name is longer than 32 characters. Then this returns the full name.
	 */
	public String getOriginalName()
	{
		return originalName != null ? originalName : referableName;
	}

	public Path getPath()
	{
		return path;
	}

	public Path getProjectRelativePath()
	{
		return library.getSourcePath().getParent().getParent().relativize(path);
	}

	public String getFilenameWithoutExtension()
	{
		var fileName = path.getFileName().toString();
		if (!fileName.contains("."))
		{
			return fileName;
		}

		var extensionIndex = fileName.lastIndexOf('.');
		return fileName.substring(0, extensionIndex);
	}

	public NaturalLibrary getLibrary()
	{
		return library;
	}

	public NaturalFileType getFiletype()
	{
		return filetype;
	}

	public NaturalHeader getNaturalHeader()
	{
		return header;
	}

	public Exception getInitException()
	{
		return initException;
	}

	public boolean isStructured()
	{
		return header.getProgrammingMode() == NaturalProgrammingMode.STRUCTURED;
	}

	public boolean isReporting()
	{
		return header.getProgrammingMode() == NaturalProgrammingMode.REPORTING;
	}

	/* package */ void setLibrary(NaturalLibrary library)
	{
		this.library = library;
	}

}
