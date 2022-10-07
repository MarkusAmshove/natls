package org.amshove.natls;

import org.amshove.natls.languageserver.NaturalLanguageServer;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App
{
	private static final String APP_NAME = App.class.getPackage().getImplementationTitle();
	private static final String APP_VERSION = App.class.getPackage().getImplementationVersion();

	private static final Logger log = LoggerFactory.getLogger(App.class);

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
		try(var logFileStream = new PrintStream(new FileOutputStream(logFile.toFile())))
		{
			System.setErr(logFileStream); // never redirect stdout, that breaks the lsp protocol as it communicates over stdin and stdout

			log.info("Starting {} {}", APP_NAME, APP_VERSION);

			var server = new NaturalLanguageServer();
			var launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
			server.connect(launcher.getRemoteProxy());
			launcher.startListening();
		}
	}
}
