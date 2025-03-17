package org.amshove.natqube;

import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.ArrayList;
import java.util.List;

public class NaturalProperties
{
	private static final List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

	static
	{
		propertyDefinitions.add(
			PropertyDefinition.builder("sonar.natural.file.suffixes")
				.category(Natural.NAME)
				.defaultValue(String.join(",", Natural.fileSuffixes()))
				.name("Natural file suffixes")
				.description("File suffixes to analyze")
				.multiValues(true)
				.onQualifiers(Qualifiers.APP, Qualifiers.PROJECT)
				.build()
		);
	}

	public static List<PropertyDefinition> getProperties()
	{
		return propertyDefinitions;
	}
}
