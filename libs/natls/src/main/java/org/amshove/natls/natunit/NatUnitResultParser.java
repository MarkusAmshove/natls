package org.amshove.natls.natunit;

import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;

public class NatUnitResultParser
{
	public NatUnitResult parse(Path filepath)
	{
		var result = new NatUnitResult();

		try
		{
			var factory = DocumentBuilderFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			var documentBuilder = factory.newDocumentBuilder();
			var doc = documentBuilder.parse(filepath.toFile());
			doc.getDocumentElement().normalize();

			var nodes = doc.getElementsByTagName("testcase");
			for (var i = 0; i < nodes.getLength(); i++)
			{
				var item = (Element) nodes.item(i);
				var name = item.getAttribute("name");
				var childNodes = item.getElementsByTagName("failure");
				if (childNodes.getLength() == 0)
				{
					result.addTestResult(new NatUnitTestResult(name, false, ""));
				}
				for (var j = 0; j < childNodes.getLength(); j++)
				{
					var childNode = (Element) childNodes.item(j);
					var message = childNode.getAttribute("message");
					result.addTestResult(new NatUnitTestResult(name, true, message));
				}
			}
		}
		catch (Exception e)
		{
			// empty result is okay
		}

		return result;
	}
}
