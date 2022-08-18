package org.amshove.testhelpers;

import com.google.common.io.Resources;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ResourceHelper
{
	public static String readResourceFile(String path, Class<?> caller)
	{
		var resource = Resources.getResource(caller, path);
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
		return readResourceFile(String.format("/%s/%s", startPath, relativePath), caller);
	}

	public static List<String> findRelativeResourceFiles(String relativePath, Class<?> caller)
	{
		var startPath = caller.getPackage().getName().replace(".", "/");
		var targetPath = "/%s/%s/".formatted(startPath, relativePath);
		return findAbsoluteResourceFiles(targetPath, caller);
	}

	public static List<String> findAbsoluteResourceFiles(String resourcePath, Class<?> caller)
	{
		try
		{
			var str = Resources.toString(Resources.getResource(caller, resourcePath), UTF_8);
			return Arrays.stream(str.split("\n")) // always has linux line endings
				.map(filename -> resourcePath + filename)
				.toList();
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
