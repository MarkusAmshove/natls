package org.amshove.natlint.editorconfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class EditorConfig
{
	private final List<EditorConfigSection> sections = new ArrayList<>();

	void addSection(EditorConfigSection section)
	{
		sections.add(section);
	}

	public List<EditorConfigSection> sections()
	{
		return this.sections;
	}

	public String getProperty(Path path, String property, String defaultValue)
	{
		return sections.stream()
			.filter(s -> s.matches(path))
			.flatMap(s -> s.properties().stream())
			.filter(p -> p.name().equals(property))
			.reduce((e, last) -> last)
			.map(EditorConfigProperty::value)
			.orElse(defaultValue);
	}
}
