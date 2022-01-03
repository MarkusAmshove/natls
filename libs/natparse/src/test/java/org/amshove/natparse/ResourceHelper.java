package org.amshove.natparse;

import com.google.common.io.Resources;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ResourceHelper
{
	public static String readResourceFile(String path)
	{
		var resource = Resources.getResource(path);
		try
		{
			return Resources.toString(resource, UTF_8);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static String readRelativeResourceFile(String relativePath, Class<?> caller)
	{
		var startPath = caller.getPackage().getName().replace(".", "/");
		return readResourceFile(String.format("%s/%s", startPath, relativePath));
	}

	public static List<String> findRelativeResourceFiles(String relativePath, Class<?> caller)
	{
		var startPath = caller.getPackage().getName().replace(".", "/");
		var targetPath = "%s/%s/".formatted(startPath, relativePath);
		try
		{
			var str = Resources.toString(Resources.getResource(targetPath), UTF_8);
			return Arrays.stream(str.split("\n")) // always has linux line endings
				.map(filename -> targetPath + filename)
				.collect(Collectors.toList());
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
