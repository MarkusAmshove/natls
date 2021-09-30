package org.amshove.natlint.natparse;

import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ResourceHelper
{
	public static String readResourceFile(String path)
	{
        var resource = Resources.getResource(path);
		try
		{
			return Resources.toString(resource, StandardCharsets.UTF_8);
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
}
