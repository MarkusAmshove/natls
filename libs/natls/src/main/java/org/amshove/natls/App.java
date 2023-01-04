package org.amshove.natls;

import org.amshove.natls.languageserver.NaturalLanguageServer;
import org.eclipse.lsp4j.launch.LSPLauncher;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App
{
	private static final String APP_NAME = App.class.getPackage().getImplementationTitle();
	private static final String APP_VERSION = App.class.getPackage().getImplementationVersion();

	public static void main(String[] args) throws IOException
	{
		if (args.length > 0)
		{
			System.out.printf("%s - Version %s%n", APP_NAME, APP_VERSION);
			return;
		}

		LogManager.getLogManager().readConfiguration(App.class.getResourceAsStream("/logging.properties"));

		var log = Logger.getAnonymousLogger();
		log.info(() -> "Starting %s %s".formatted(APP_NAME, APP_VERSION));

		var server = new NaturalLanguageServer();
		var launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
		server.connect(launcher.getRemoteProxy());
		launcher.startListening();
	}
}
