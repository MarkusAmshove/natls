package org.amshove.natls;

import org.amshove.natls.languageserver.NaturalLanguageServer;
import org.eclipse.lsp4j.launch.LSPLauncher;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App
{
	private static final String APP_NAME = App.class.getPackage().getImplementationTitle();
	private static final String APP_VERSION = App.class.getPackage().getImplementationVersion();

	public static void main(String[] args) throws IOException
	{
		if(args.length > 0)
		{
			System.out.printf("%s - Version %s%n", APP_NAME, APP_VERSION);
			return;
		}

		var logFile = Paths.get(System.getProperty("user.home"), ".natls.log");
		if(!logFile.toFile().exists())
		{
			Files.writeString(logFile, "");
		}
		System.setErr(new PrintStream(new FileOutputStream(logFile.toFile())));

		var server = new NaturalLanguageServer();
		var launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
		server.connect(launcher.getRemoteProxy());
		launcher.startListening();
	}
}
