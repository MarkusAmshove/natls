package org.amshove.natparse.parsing.project;

import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.infrastructure.IFilesystem;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NaturalBuildFileParser
{

	private final IFilesystem filesystem;
	private XPath xPath;

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
		xPath = XPathFactory.newInstance().newXPath();
		try
		{
			var docB = DocumentBuilderFactory.newInstance();
			var builder = docB.newDocumentBuilder();
			var doc = builder.parse(new ByteArrayInputStream(filesystem.readFile(path).getBytes(StandardCharsets.UTF_8)));
			var xmlStepLibs = (NodeList) xPath.compile("//LibrarySteplib").evaluate(doc, XPathConstants.NODESET);
			var libraries = new ArrayList<XmlNaturalLibrary>();

			for(var i = 0; i < xmlStepLibs.getLength(); i++)
			{
				var libraryNode = xmlStepLibs.item(i);
				var libraryName = xPath.compile(".//LibrarySteplibName/text()").evaluate(libraryNode);
				var library = new XmlNaturalLibrary(libraryName);
				addStepLibExtensions(library, libraryNode);
				addNsvLibrary(library, libraryNode);
				libraries.add(library);
			}

			return libraries;
		}
		catch (Exception e)
		{
			throw new BuildFileParserException(e);
		}
	}

	private void addNsvLibrary(XmlNaturalLibrary library, Node nsvNode) throws XPathExpressionException
	{
		var nsv = xPath.compile(".//LibrarySteplibNSV/text()").evaluate(nsvNode);
		if(!nsv.isEmpty())
		{
			library.addSteplib(nsv);
		}
	}

	private void addStepLibExtensions(XmlNaturalLibrary library, Node libraryNode) throws XPathExpressionException
	{
		var extensions = xPath.compile(".//LibrarySteplibExtensions/text()").evaluate(libraryNode);
		extensions = extensions.replaceAll("\\d", "");
		extensions = extensions.replaceAll("\\[.*?]", "");
		Arrays.stream(extensions.split(";"))
			.filter(lib -> !lib.isEmpty())
			.forEach(library::addSteplib);
	}
}
