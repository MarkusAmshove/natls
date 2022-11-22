package org.amshove.natlint.editorconfig;

import org.amshove.natparse.ReadOnlyList;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

public final class EditorConfigSection
{
	private final String filePattern;
	private final ReadOnlyList<EditorConfigProperty> properties;
	private final PathMatcher matcher;

	public EditorConfigSection(String filePattern, ReadOnlyList<EditorConfigProperty> properties)
	{
		this.filePattern = filePattern;
		this.properties = properties;
		this.matcher = FileSystems.getDefault().getPathMatcher("glob:**/%s".formatted(filePattern));
	}

	public String filePattern()
	{
		return filePattern;
	}

	public ReadOnlyList<EditorConfigProperty> properties()
	{
		return properties;
	}

	public boolean matches(Path path)
	{
		return matcher.matches(path);
	}
}
