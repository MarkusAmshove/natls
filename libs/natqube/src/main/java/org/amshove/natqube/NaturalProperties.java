package org.amshove.natqube;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public class NaturalProperties
{
	private static final List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

	static
	{
		propertyDefinitions.add(
			PropertyDefinition.builder("sonar.natural.file.suffixes")
				.category(Natural.NAME)
				.defaultValue(Arrays.stream(Natural.fileSuffixes()).collect(Collectors.joining(",")))
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
