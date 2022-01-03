package org.amshove.natls;

import org.amshove.natls.languageserver.NaturalLanguageServer;
import org.eclipse.lsp4j.launch.LSPLauncher;

public class App
{
	private static final String APP_NAME = App.class.getPackage().getImplementationTitle();
	private static final String APP_VERSION = App.class.getPackage().getImplementationVersion();

	public static void main(String[] args)
	{
		if(args.length > 0)
		{
			System.out.printf("%s - Version %s%n", APP_NAME, APP_VERSION);
			return;
		}

		var server = new NaturalLanguageServer();
		var launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
		server.connect(launcher.getRemoteProxy());
		launcher.startListening();
	}
}
