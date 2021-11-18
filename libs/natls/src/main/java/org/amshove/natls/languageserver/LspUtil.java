package org.amshove.natls.languageserver;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LspUtil
{
	public static Path uriToPath(String uri)
	{
		return Paths.get(URI.create(uri));
	}
}
