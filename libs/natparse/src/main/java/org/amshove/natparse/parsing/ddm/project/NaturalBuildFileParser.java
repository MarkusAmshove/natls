package org.amshove.natparse.parsing.ddm.project;

import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.infrastructure.IFilesystem;
import org.dom4j.io.SAXReader;

import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;

public class NaturalBuildFileParser
{

	private final IFilesystem filesystem;

	public NaturalBuildFileParser()
	{
		this(new ActualFilesystem());
	}

	public NaturalBuildFileParser(IFilesystem filesystem)
	{
		this.filesystem = filesystem;
	}

	public List<XmlNaturalLibrary> parseLibraries(Path path)
	{
		try
		{
			var visitor = new XmlLibraryVisitor();
			var content = filesystem.readFile(path);
			var document = new SAXReader().read(new StringReader(content));
			document.accept(visitor);

			return visitor.getLibraries();
		}
		catch (Exception e)
		{
			throw new BuildFileParserException(e);
		}
	}

}
