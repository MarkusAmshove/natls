package org.amshove.natdoc;

import org.amshove.natls.languageserver.NaturalLanguageService;
import org.amshove.natls.progress.StdOutProgressMonitor;

import java.nio.file.Paths;

public class App
{
	public static void main(String[] args)
	{
		var nls = new NaturalLanguageService();
		nls.indexProject(Paths.get("."), new StdOutProgressMonitor());
	}
}
