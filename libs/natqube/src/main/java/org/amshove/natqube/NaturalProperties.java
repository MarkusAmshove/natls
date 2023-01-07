package org.amshove.natqube;

import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.ArrayList;
import java.util.List;

public class NaturalProperties
{
	private static final String CATEGORY_ANALYZER = "Analyzer";
	public static final String NATLINT_ISSUE_FILE_KEY = "org.amshove.natqube.natlintIssueFile";
	private static final String DEFAULT_NATLINT_ISSUE_FILE = "diagnostics.csv";

	private static final List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

	static
	{
		propertyDefinitions.add(
			PropertyDefinition.builder(NATLINT_ISSUE_FILE_KEY)
				.category(Natural.NAME)
				.subCategory(CATEGORY_ANALYZER)
				.defaultValue(DEFAULT_NATLINT_ISSUE_FILE)
				.name("Diagnostics file")
				.description("Relative path to CSV file with diagnostics")
				.onQualifiers(Qualifiers.APP, Qualifiers.PROJECT)
				.build()
		);
	}

	static List<PropertyDefinition> getProperties()
	{
		return propertyDefinitions;
	}
}
