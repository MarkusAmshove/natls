package org.amshove.natparse.parsing.project;

import org.dom4j.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class XmlLibraryVisitor implements Visitor
{

	private final List<XmlNaturalLibrary> libraries = new ArrayList<>();
	private XmlNaturalLibrary currentLibrary;

	@Override
	public void visit(Element node)
	{
		if (node.getName().equalsIgnoreCase("LibrarySteplibName"))
		{
			beginNewSteplib(node);
		}
		if (node.getName().equalsIgnoreCase("LibrarySteplibNSV"))
		{
			addSteplib(node);
		}
		if (node.getName().equalsIgnoreCase("LibrarySteplibExtensions"))
		{
			addSteplib(node);
		}
	}

	List<XmlNaturalLibrary> getLibraries()
	{
		if (currentLibrary != null)
		{
			libraries.add(currentLibrary);
			currentLibrary = null;
		}
		return libraries;
	}

	private void addSteplib(Element node)
	{
		if (currentLibrary != null)
		{
			var steplibs = node.getStringValue();
			steplibs = steplibs.replaceAll("\\d", "");
			steplibs = steplibs.replaceAll("\\[.*?]", "");
			Arrays.stream(steplibs.split(";"))
				.filter(lib -> !lib.isEmpty())
				.forEach(currentLibrary::addSteplib);
		}
	}

	private void beginNewSteplib(Element node)
	{
		if (currentLibrary != null)
		{
			finishCurrentSteplib();
		}
		currentLibrary = new XmlNaturalLibrary(node.getStringValue());
	}

	private void finishCurrentSteplib()
	{
		libraries.add(currentLibrary);
		currentLibrary = null;
	}

	@Override
	public void visit(Document document)
	{
		// Ignored
	}

	@Override
	public void visit(DocumentType documentType)
	{
		// Ignored
	}

	@Override
	public void visit(Attribute node)
	{
		// Ignored
	}

	@Override
	public void visit(CDATA node)
	{
		// Ignored
	}

	@Override
	public void visit(Comment node)
	{
		// Ignored
	}

	@Override
	public void visit(Entity node)
	{
		// Ignored
	}

	@Override
	public void visit(Namespace namespace)
	{
		// Ignored
	}

	@Override
	public void visit(ProcessingInstruction node)
	{
		// Ignored
	}

	@Override
	public void visit(Text node)
	{
		// Ignored
	}
}

