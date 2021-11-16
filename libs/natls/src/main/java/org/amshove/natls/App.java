package org.amshove.natls;

import org.amshove.natls.languageserver.NaturalLanguageServer;
import org.eclipse.lsp4j.launch.LSPLauncher;

public class App
{
	public static void main(String[] args)
	{
		var server = new NaturalLanguageServer();
		var launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
		launcher.startListening();
	}
}
